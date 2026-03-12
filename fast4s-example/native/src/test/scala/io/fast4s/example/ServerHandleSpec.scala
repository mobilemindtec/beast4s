package io.fast4s.example

import fast4s.{*, given}
import fast4s.backend.beast.sync
import org.scalatest.funsuite.AnyFunSuite

class ServerHandleSpec extends AnyFunSuite:

  test("request test") {

    fast.get("/user") { _ =>
      Ok("pong")
    }

    fast.post("/user") { req =>
      req.body match
        case "ping" => Ok("pong")
        case _      => BadRequest()
    }

    fast.get("/user/:id") { (req: Request) =>
      req.body match
        case "ping" => Ok("pong")
        case _      => BadRequest()
    }

    val server = fast.server.create()

    server
      .handle(Request(HttpMethod.Get, "/user")) match
      case Response(HttpStatus.OK, "pong", _, _, _) =>
      case resp => fail(s"expected pong: $resp")

    server
      .handle(Request(HttpMethod.Get, "/users")) match
      case Response(HttpStatus.NotFound, _, _, _, _) =>
      case resp => fail(s"expected not found: $resp")

    server
      .handle(Request(HttpMethod.Get, "/user", "ping")) match
      case Response(HttpStatus.OK, "pong", _, _, _) =>
      case resp => fail(s"expected pong: $resp")
  }
