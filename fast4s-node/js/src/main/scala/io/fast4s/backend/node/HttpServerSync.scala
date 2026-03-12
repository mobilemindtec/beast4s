package io.fast4s.backend.node

import io.fast4s.core.HttpServer

trait HttpServerSync(val host: String, val port: Int, val workers: Int = 1)
    extends HttpServer:

  override def run: Int = ???
