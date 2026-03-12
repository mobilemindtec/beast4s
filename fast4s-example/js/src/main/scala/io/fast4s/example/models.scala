package io.fast4s.example

import decoda.Decoder.given
import decoda.Encoder.given
import decoda.{*, given}

object models:

  case class Person(id: Int = 0, name: String = "")

  object Person:
    val encoder: Encoder[Person] =
      Encoder.typ[Person]
        |> Encoder.field("id", _.id)
        |> Encoder.field("name", _.name)

    val decoder: Decoder[Person] =
      Decoder.typ[Person]
        |> Decoder.field("id", (p, i: Int) => p.copy(id = i))
        |> Decoder.field("name", (p, s: String) => p.copy(name = s))

    def fromJson(s: String): Person = decoder.parse(s)

    extension (p: Person) def toJson: String = encoder.encodeObject(p)
