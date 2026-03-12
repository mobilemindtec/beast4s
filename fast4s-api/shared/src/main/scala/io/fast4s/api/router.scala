package io.fast4s.api

import io.fast4s.data.*
import io.via.types

object router:
  type Enter = types.Enter[Request, Response]
  type Leave = types.Leave[Request, Response]
  type Route = types.RouteEntry[Request, Response]
  type Controller = types.Controller[Request, Response]
  type Handler = types.HttpHandler[Request, Response]
  type Dispatcher = types.Dispatcher[Request, Response]
  // type MiddlewareEnter = types.MiddlewareEnter[Request, Response]
  // type MiddlewareLeave = types.MiddlewareLeave[Request, Response]
