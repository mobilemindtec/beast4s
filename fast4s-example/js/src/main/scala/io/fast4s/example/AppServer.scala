package io.fast4s.example

import fast4s.*
import io.fast4s.core.HttpServerCreator
import io.fast4s.data.ContentType.Json
import io.fast4s.example.models.Person
import via.*

object AppServer:

  def serve(using HttpServerCreator): Int =

    val logger: Enter = enter(GET): req =>
      println(s"enter in ${req.target}")
      req

    val home: Route = route(GET, root): _ =>
      Response.ok("alive!")

    val ping: Route = route(GET, root / "ping"): _ =>
      Response.ok("pong")

    val person: Route = route(GET, root / "person"): _ =>
      val json = Person(1, "Ricardo").toJson
      Response.ok(json, Json)

    HttpServerBuilder()
      .withRoutes(
        logger ++ home,
        logger ++ ping,
        logger ++ person
      )
      .build
      .serve
