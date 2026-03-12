package io.fast4s.backend.beast

import io.fast4s.*
import Structs.*, Util.*, Ext.*, EasyBeast.*
import io.fast4s.core.*
import io.fast4s.data.*
import via.*

import scala.scalanative.unsafe
import scala.scalanative.unsafe.{CFuncPtr1, CFuncPtr3, Ptr, Zone}
import scala.scalanative.unsigned.UnsignedRichInt

object HttpServerSync:

  def apply(cfg: HttpServerConfigs): Fast4sRequestBuilder ?=> HttpServerSync =
    new HttpServerSync(cfg.host, cfg.port, cfg.workers) {
      def router: Router[Request, Response, RawRequest] = Router(cfg.routes*)

      def recover: Option[Recover] = cfg.recover

      def interceptors: Map[Int, Interceptor] = cfg.interceptors

      def leave: Seq[NSLeave] = cfg.leave

      def enter: Seq[NSEnter] = cfg.enter
    }

  private def handle(req: Ptr[BeastRequest]): Ptr[BeastResponse] = {
    val resp = HttpServer.handle(req.request())
    Zone:
      resp.ptr()
  }

trait HttpServerSync(val host: String, val port: Int, val workers: Int = 1)
    extends HttpServer:

  override def run: Int =
    Zone:
      runSync(
        unsafe.toCString(host),
        port.toUShort,
        workers.toUShort,
        CFuncPtr3.fromScalaFunction(threadStart),
        CFuncPtr1.fromScalaFunction(HttpServerSync.handle)
      )
