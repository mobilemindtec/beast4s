package io.fast4s.core

import io.fast4s.data.{Request, Response}
import via.types.*

type HttpServerCreator = HttpServerConfigs => HttpServer

class HttpServerBuilder:

  private var cfg: HttpServerConfigs = HttpServerConfigs()

  def withPort(port: Int): HttpServerBuilder =
    cfg = cfg.copy(port = port)
    this

  def withHost(host: String): HttpServerBuilder =
    cfg = cfg.copy(host = host)
    this

  def withWorkers(workers: Int): HttpServerBuilder =
    cfg = cfg.copy(workers = workers)
    this

  def withRoutes(routes: RouteEntry[Request, Response]*): HttpServerBuilder =
    cfg = cfg.copy(routes = cfg.routes ++ routes)
    this

  def withRoute(route: => RouteEntry[Request, Response]): HttpServerBuilder =
    cfg = cfg.copy(routes = cfg.routes :+ route)
    this

  def withRecover(recover: Recover): HttpServerBuilder =
    cfg = cfg.copy(recover = Some(recover))
    this

  def withInterceptor(status: Int)(
      interceptor: => Interceptor
  ): HttpServerBuilder =
    cfg = cfg.copy(interceptors = cfg.interceptors + (status -> interceptor))
    this

  def withLeave(leave: Seq[NSLeave]): HttpServerBuilder =
    cfg = cfg.copy(leave = cfg.leave ++ leave)
    this

  def withLeave(leave: => NSLeave): HttpServerBuilder =
    cfg = cfg.copy(leave = cfg.leave :+ leave)
    this

  def withEnter(enter: Seq[NSEnter]): HttpServerBuilder =
    cfg = cfg.copy(enter = cfg.enter ++ enter)
    this

  def withEnter(enter: => NSEnter): HttpServerBuilder =
    cfg = cfg.copy(enter = cfg.enter :+ enter)
    this

  def async: HttpServerBuilder =
    cfg = cfg.copy(async = true)
    this

  def build(using creator: HttpServerCreator): HttpServer =
    creator(cfg).build

object HttpServerBuilder:

  def apply(): HttpServerBuilder = new HttpServerBuilder()
