package io.fast4s.core

import io.fast4s.data.{ContentType, RawRequest, Request, toHttpMethod}
import via.{RequestBuilder, RouteInfo}

type Fast4sRequestBuilder = RequestBuilder[Request, RawRequest]

given requestBuilder: Fast4sRequestBuilder =
  (routeInfo: RouteInfo, rawReq: Option[RawRequest]) =>

    val headers = rawReq.map(_.headers).getOrElse(Map())
    val contentType = headers
      .find(_._1.toLowerCase == "content-type")
      .map(_._2)
      .map(ContentType.make)
      .getOrElse(ContentType.Empty)

    Request(
      routeInfo.method.toHttpMethod,
      routeInfo.target,
      rawReq.map(_.body).getOrElse(""),
      contentType,
      rawReq.map(_.bodyRaw).getOrElse(Nil),
      headers,
      routeInfo.params,
      routeInfo.query,
      routeInfo.matcher
    )
