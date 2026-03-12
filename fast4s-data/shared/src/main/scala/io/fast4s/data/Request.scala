package io.fast4s.data

import via.{Params, Query, RouteMatcher}

case class Request(
    method: HttpMethod,
    target: String,
    body: String = "",
    contentType: ContentType = ContentType.Empty,
    bodyRaw: Seq[Byte] = Nil,
    headers: Headers = Map(),
    params: Params = Params(),
    query: Query = Query(),
    matcher: RouteMatcher = Nil
)
