package io.fast4s.core

import io.fast4s.data.{RawRequest, Request, Response, toMethod}
import via.*
import via.types.*
import scala.annotation.tailrec
import scala.compiletime.uninitialized

type Interceptor = (Request, Response) => Response
type Recover = (Request, Throwable) => Response

type NSLeave = (path: String, leave: Leave[Request, Response])
type NSEnter = (path: String, enter: Enter[Request, Response])

trait HttpServer:

  def router: Router[Request, Response, RawRequest]
  def recover: Option[Recover]
  def interceptors: Map[Int, Interceptor]
  def leave: Seq[NSLeave]
  def enter: Seq[NSEnter]
  def run: Int
  def serve: Int = run
  def handle(req: Request): Response = HttpServer.handle(req)
  def build: HttpServer =
    HttpServer.server = this
    this

private[fast4s] object HttpServer:

  private var server: HttpServer = uninitialized

  /*
  inline def applyEnter(request: Request): Request | Response =
    @tailrec
    def each(req: Request, items: Seq[NSEnter]): Request | Response =
      items match
        case Nil => req
        case x :: xs =>
          val res =
            if Regex(x.path).matches(req.target)
            then
              server
                .router
                .applyEnter(req.method.toMethod, req, Some(x.enter))
            else req
          res match
            case  r: Request => each(r, xs)
            case _ => res
    each(request, server.enter)

  inline def applyLeave(request: Request, response: Response): Response =
    @tailrec
    def each(resp: Response, items: Seq[NSLeave]): Response =
      items match
        case Nil => resp
        case x :: xs =>
          val res =
            if Regex(x.path).matches(request.target)
            then
              server
                .router
                .applyLeave(request.method.toMethod, request, resp, Some(x.leave))
            else resp
          each(res, xs)
    each(response, server.leave)
   */
  private inline def applyInterceptors(request: Request)(
      response: Response
  ): Response =
    @tailrec
    def each(resp: Response, items: Seq[Interceptor]): Response =
      items match
        case Nil     => resp
        case x :: xs => each(x.apply(request, resp), xs)
    val its = server.interceptors
      .filter(_._1 == response.status.code)
      .values
      .toSeq
    each(response, its)

  inline def handle(request: Request): Response =
    try {
      /*
        val resp =
          applyEnter(request) match
            case r: Request => dispatch(r)
            case r: Response => r
        applyLeave(request, resp) |> applyInterceptors(request)
       */
      dispatch(request) |> applyInterceptors(request)
    } catch
      case err: Throwable =>
        println("Error during http server handle: " + err)
        server.recover
          .map(f => f(request, err))
          .getOrElse(Response.serverError())

  inline def dispatch(request: Request): Response =
    val target = request.target
    val method = request.method.toMethod
    val extra = RawRequest(
      request.body,
      request.bodyRaw,
      request.headers
    )

    server.router
      .dispatch(method, target, extra) match
      case Some(resp) => resp
      case _          => Response.notFound("Not Found")
