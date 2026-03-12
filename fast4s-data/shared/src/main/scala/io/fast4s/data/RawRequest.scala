package io.fast4s.data

case class RawRequest(
    body: String = "",
    bodyRaw: Seq[Byte] = Nil,
    headers: Headers = Map()
)
