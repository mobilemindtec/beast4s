package io.fast4s.example

import fast4s.*
import io.fast4s.core.HttpServerCreator
import io.fast4s.example.models.Person

object ExpressLike:

  fast.get("/") { _ =>
    Response.ok("hello, world!")
  }

  fast.get("/user") { (req: Request) =>
    Response.ok(s"hello, ${req.query.str("name").getOrElse("anonymous")}!")
  }

  fast.get("/err") { _ =>
    throw Exception("err")
  }

  fast.get("/person") { _ =>
    Response.ok(Person(id = 1, name = "Ricardo").toJson)
  }

  fast.post("/person") { (req: Request) =>
    println(s"body = ${req.body}")
    Response.ok(Person.fromJson(req.body).toJson)
  }

  fast.recover { (req, err) =>
    Response.ok(s"recovered! ${err.getMessage}")
  }

  fast.intercept(404) { (req, resp) =>
    Response.ok("404")
  }

  fast.enter(Get, "/.*") { req =>
    println(s"ns enter ${req.target}")
    req
  }

  fast.leave(Get, "/.*") { (req, resp) =>
    println(s"ns leave ${req.target}")
    resp
  }

  def serve(using HttpServerCreator): Int =
    fast.serve()
