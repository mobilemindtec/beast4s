package io.fast4s.backend.beast

import Structs.*, Util.*, Ext.*, EasyBeast.*
import io.fast4s.core.*
import io.fast4s.data.*
import via.*

import scala.scalanative.unsafe
import scala.scalanative.unsafe.{CFuncPtr, CFuncPtr2, CFuncPtr3, Ptr, Zone}
import scala.scalanative.unsigned.UnsignedRichInt

object HttpServerAsync:

  def apply(cfg: HttpServerConfigs): Fast4sRequestBuilder ?=> HttpServerAsync =
    new HttpServerAsync(cfg.host, cfg.port, cfg.workers) {
      def router: Router[Request, Response, RawRequest] = Router(cfg.routes*)

      def recover: Option[Recover] = cfg.recover

      def interceptors: Map[Int, Interceptor] = cfg.interceptors

      def leave: Seq[NSLeave] = cfg.leave

      def enter: Seq[NSEnter] = cfg.enter
    }

  def handle(req: Ptr[BeastRequest], handlerPtr: Ptr[Byte]): Unit = {
    val resp = HttpServer.handle(req.request())
    Zone:
      val beastCallback = CFuncPtr.fromPtr[BeastHandlerCallback](handlerPtr)
      beastCallback(req, resp.ptr())
  }

trait HttpServerAsync(val host: String, val port: Int, val workers: Int = 1)
    extends HttpServer:

  override def run: Int =
    Zone:
      runAsync(
        unsafe.toCString(host),
        port.toUShort,
        workers.toUShort,
        CFuncPtr3.fromScalaFunction(threadStart),
        CFuncPtr2.fromScalaFunction(HttpServerAsync.handle)
      )
