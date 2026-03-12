package io.fast4s.core

import io.fast4s.data.{Request, Response}
import via.types.*

case class HttpServerConfigs(
    host: String = "0.0.0.0",
    port: Int = 3000,
    workers: Int = 1,
    routes: Seq[RouteEntry[Request, Response]] = Nil,
    recover: Option[Recover] = None,
    interceptors: Map[Int, Interceptor] = Map(),
    leave: Seq[NSLeave] = Nil,
    enter: Seq[NSEnter] = Nil,
    async: Boolean = false
)
