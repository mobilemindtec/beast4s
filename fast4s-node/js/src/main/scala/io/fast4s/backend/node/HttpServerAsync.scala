package io.fast4s.backend.node

import io.fast4s.core.*
import io.fast4s.data.*
import via.Router

import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js

object Node:

  // Node.js HTTP server facades (Scala.js)
  trait Server extends js.Object:
    def listen(port: Int, host: String, cb: js.Function0[Unit]): Unit

  trait IncomingMessage extends js.Object:
    val method: js.UndefOr[String]
    val url: js.UndefOr[String]
    val headers: js.Dictionary[js.Any]

    def on(event: String, cb: js.Function1[js.Any, Unit]): this.type

  trait ServerResponse extends js.Object:
    def writeHead(statusCode: Int, headers: js.Dictionary[String]): Unit
    def end(data: js.UndefOr[js.Any] = js.undefined): Unit

  @js.native
  @JSImport("node:http", JSImport.Namespace)
  object Http extends js.Object:
    def createServer(
        handler: js.Function2[IncomingMessage, ServerResponse, Unit]
    ): Server = js.native

  extension (req: IncomingMessage)
    /** Build the "complete request" bridge object expected by fast4s core
      * (RawRequest).
      */
    def request(onComplete: Request => Unit): Unit =
      val headersMap: Map[String, String] =
        req.headers.iterator.map { case (k, v) =>
          val valueStr =
            if js.isUndefined(v) || v == null then ""
            else
              v match
                case arr: js.Array[?] =>
                  arr.map(_.toString).mkString(",")
                case other => other.toString
          k -> valueStr
        }.toMap

      val chunks = js.Array[Byte]()

      val onData = (chunkAny: js.Any) =>
        // Best-effort: accept either Buffer-like (array-like bytes) or string
        chunkAny match
          case arr: js.Array[?] =>
            var i = 0
            while i < arr.length do
              val b = arr(i).toString.toDouble.toInt.toByte
              chunks.push(b)
              i += 1
          case other =>
            val bytes = other.toString.getBytes("UTF-8")
            var i = 0
            while i < bytes.length do
              chunks.push(bytes(i))
              i += 1

      val onEnd = (_: js.Any) =>
        val bodyRaw = chunks.toSeq
        val bodyStr =
          try String(bodyRaw.toArray, "UTF-8")
          catch case _: Throwable => ""
        val request = Request(
          target = req.url.getOrElse("/"),
          method =
            req.method.map { s => HttpMethod(s) }.getOrElse(HttpMethod.Get),
          body = bodyStr,
          bodyRaw = bodyRaw,
          headers = headersMap
        )
        onComplete(request)

      req
        .on("data", onData)
        .on("end", onEnd)
      ()

  extension (resp: ServerResponse)
    /** Write the "complete response" bridge object (fast4s Response) into
      * Node's ServerResponse.
      */
    def respond(r: Response): Unit =
      val h = js.Dictionary.empty[String]

      // Always prefer explicit headers from fast4s
      r.headers.foreach { case (k, v) => h.update(k, v) }

      // Ensure content-type exists when fast4s has one
      val hasContentType =
        r.headers.keys.exists(_.equalsIgnoreCase("content-type"))
      if !hasContentType then h.update("content-type", r.contentType.mimeType)

      resp.writeHead(r.status.code, h)

      if r.rawBody.nonEmpty then
        // Write bytes as an array-like; Node will accept Uint8Array/Buffer, but we keep it minimal here.
        val bytes = js.Array(r.rawBody)
        resp.end(bytes)
      else resp.end(r.body)

object HttpServerAsync:

  def apply(cfg: HttpServerConfigs): Fast4sRequestBuilder ?=> HttpServerAsync =
    new HttpServerAsync(cfg.host, cfg.port, cfg.workers) {
      def router: Router[Request, Response, RawRequest] = Router(cfg.routes*)

      def recover: Option[Recover] = cfg.recover

      def interceptors: Map[Int, Interceptor] = cfg.interceptors

      def leave: Seq[NSLeave] = cfg.leave

      def enter: Seq[NSEnter] = cfg.enter
    }

  def handle(
      nodeReq: Node.IncomingMessage,
      nodeResp: Node.ServerResponse
  ): Unit =
    nodeReq.request { req =>
      val resp = HttpServer.handle(req)
      nodeResp.respond(resp)
    }

trait HttpServerAsync(val host: String, val port: Int, val workers: Int = 1)
    extends HttpServer:

  override def run: Int =
    Node.Http
      .createServer { (nodeReq, nodeResp) =>
        HttpServerAsync.handle(nodeReq, nodeResp)
      }
      .listen(port, host, () => println(s"Server listen on http://$host:$port"))
    0
