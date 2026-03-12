package io.fast4s.api

import io.fast4s.core.*
import io.fast4s.data.*
import via.*
import via.types.*
import scala.collection.mutable

object Fast4s:

  // private type ServerResult = RequestBuilder[Request, RawRequest] ?=> Int

  private val _routes = mutable.ListBuffer[RouteEntry[Request, Response]]()

  // ns middlewares
  private val _leave = mutable.ListBuffer[NSLeave]()

  private val _enter = mutable.ListBuffer[NSEnter]()

  private val _interceptors = mutable.Map[Int, Interceptor]()

  private var _recover: Option[Recover] = None

  private def register(route: RouteEntry[Request, Response]) =
    _routes.addOne(route)
    route

  private def buildServer(
      host: String,
      port: Int,
      workers: Int,
      routes: Seq[RouteEntry[Request, Response]]
  )(using creator: HttpServerCreator): HttpServer =
    val cfg = HttpServerConfigs(
      host = host,
      port = port,
      workers = workers,
      routes = routes,
      recover = _recover,
      interceptors = _interceptors.toMap,
      leave = _leave.toSeq,
      enter = _enter.toSeq
    )
    creator(cfg).build

  private def runServer(
      host: String,
      port: Int,
      workers: Int,
      routes: Seq[RouteEntry[Request, Response]]
  )(using creator: HttpServerCreator): Int =
    buildServer(host, port, workers, routes).serve

  object fast:

    // import io.fast4s.core.requestBuilder

    def get(
        path: String,
        c: Controller[Request, Response]
    ): RouteEntry[Request, Response] =
      route(GET, path, c) |> register

    def get(path: String)(
        f: HttpHandler[Request, Response]
    ): RouteEntry[Request, Response] =
      route(GET, path)(f) |> register

    def get(path: String)(
        f: Dispatcher[Request, Response]
    ): RouteEntry[Request, Response] =
      route(GET, path)(f) |> register

    def head(
        path: String,
        c: Controller[Request, Response]
    ): RouteEntry[Request, Response] =
      route(HEAD, path, c) |> register

    def head(path: String)(
        f: HttpHandler[Request, Response]
    ): RouteEntry[Request, Response] =
      route(HEAD, path)(f) |> register

    def head(path: String)(
        f: Dispatcher[Request, Response]
    ): RouteEntry[Request, Response] =
      route(HEAD, path)(f) |> register

    def options(
        path: String,
        c: Controller[Request, Response]
    ): RouteEntry[Request, Response] =
      route(OPTIONS, path, c) |> register

    def options(path: String)(
        f: HttpHandler[Request, Response]
    ): RouteEntry[Request, Response] =
      route(OPTIONS, path)(f) |> register

    def options(path: String)(
        f: Dispatcher[Request, Response]
    ): RouteEntry[Request, Response] =
      route(OPTIONS, path)(f) |> register

    def post(
        path: String,
        c: Controller[Request, Response]
    ): RouteEntry[Request, Response] =
      route(POST, path, c) |> register

    def post(path: String)(
        f: HttpHandler[Request, Response]
    ): RouteEntry[Request, Response] =
      route(POST, path)(f) |> register

    def post(path: String)(
        f: Dispatcher[Request, Response]
    ): RouteEntry[Request, Response] =
      route(POST, path)(f) |> register

    def put(
        path: String,
        c: Controller[Request, Response]
    ): RouteEntry[Request, Response] =
      route(PUT, path, c) |> register

    def put(path: String)(
        f: HttpHandler[Request, Response]
    ): RouteEntry[Request, Response] =
      route(PUT, path)(f) |> register

    def put(path: String)(
        f: Dispatcher[Request, Response]
    ): RouteEntry[Request, Response] =
      route(PUT, path)(f) |> register

    def delete(
        path: String,
        c: Controller[Request, Response]
    ): RouteEntry[Request, Response] =
      route(DELETE, path, c) |> register

    def delete(path: String)(
        f: HttpHandler[Request, Response]
    ): RouteEntry[Request, Response] =
      route(DELETE, path)(f) |> register

    def delete(path: String)(
        f: Dispatcher[Request, Response]
    ): RouteEntry[Request, Response] =
      route(DELETE, path)(f) |> register

    def patch(
        path: String,
        c: Controller[Request, Response]
    ): RouteEntry[Request, Response] =
      route(PATCH, path, c) |> register

    def patch(path: String)(
        f: HttpHandler[Request, Response]
    ): RouteEntry[Request, Response] =
      route(PATCH, path)(f) |> register

    def patch(path: String)(
        f: Dispatcher[Request, Response]
    ): RouteEntry[Request, Response] =
      route(PATCH, path)(f) |> register

    def connect(
        path: String,
        c: Controller[Request, Response]
    ): RouteEntry[Request, Response] =
      route(CONNECT, path, c) |> register

    def connect(path: String)(
        f: HttpHandler[Request, Response]
    ): RouteEntry[Request, Response] =
      route(CONNECT, path)(f) |> register

    def connect(path: String)(
        f: Dispatcher[Request, Response]
    ): RouteEntry[Request, Response] =
      route(CONNECT, path)(f) |> register

    def any(
        path: String,
        c: Controller[Request, Response]
    ): RouteEntry[Request, Response] =
      route(ANY, path, c) |> register

    def any(path: String)(
        f: HttpHandler[Request, Response]
    ): RouteEntry[Request, Response] =
      route(ANY, path)(f) |> register

    def any(path: String)(
        f: Dispatcher[Request, Response]
    ): RouteEntry[Request, Response] =
      route(ANY, path)(f) |> register

    def routes(items: RouteEntry[Request, Response]*): Unit =
      _routes.addAll(items)

    def recover(f: Recover): Unit =
      _recover = Some(f)
      ()

    def intercept(status: Int)(f: Interceptor): Unit =
      _interceptors.addOne(status -> f)

    def enter(method: HttpMethod, path: String)(
        f: MiddlewareEnter[Request, Response]
    ): Unit =
      ns(path, Enter(method.toMethod :: Nil, f))

    def leave(method: HttpMethod, path: String)(
        f: MiddlewareLeave[Request, Response]
    ): Unit =
      ns(path, Leave(method.toMethod :: Nil, f))

    def ns(
        path: String,
        m: Leave[Request, Response] | Enter[Request, Response]
    ): Unit =
      m match
        case leave: Leave[Request, Response] =>
          _leave.addOne((path, leave))
        case enter: Enter[Request, Response] =>
          _enter.addOne((path, enter))

    def serve(
        hostname: String = "0.0.0.0",
        port: Int = 3000,
        workers: Int = 1
    ): HttpServerCreator ?=> Int =
      runServer(hostname, port, workers, _routes.toSeq)

    object server:
      def create(
          hostname: String = "0.0.0.0",
          port: Int = 3000,
          workers: Int = 1
      ): HttpServerCreator ?=> HttpServer =
        Fast4s.buildServer(hostname, port, workers, _routes.toSeq)
