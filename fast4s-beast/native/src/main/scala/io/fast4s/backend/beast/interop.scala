package io.fast4s.backend.beast

import io.fast4s.data.*

import scala.collection.{immutable, mutable}
import scala.scalanative.unsafe.*

object Structs:

  import Util.*

  // name, value
  type BeastHeader = CStruct2[CString, CString]
  object BeastHeader:
    given _tag: Tag[BeastHeader] = Tag.materializeCStruct2Tag[CString, CString]

    def apply()(using Zone): Ptr[BeastHeader] = alloc[BeastHeader](1)

    def apply(name: CString, value: CString)(using Zone): Ptr[BeastHeader] =
      val ___ptr = apply()
      (!___ptr).name = name
      (!___ptr).value = value
      ___ptr

  extension (struct: BeastHeader)
    def name: CString = struct._1
    def name_=(value: CString): Unit = !struct.at1 = value
    def value: CString = struct._2
    def value_=(value: CString): Unit = !struct.at2 = value

  // header start pointer, size
  type BeastHeaders = CStruct2[Ptr[BeastHeader], CInt]
  object BeastHeaders:
    given _tag: Tag[BeastHeaders] =
      Tag.materializeCStruct2Tag[Ptr[BeastHeader], CInt]

    def apply()(using Zone): Ptr[BeastHeaders] = alloc[BeastHeaders](1)

    def apply(header: Ptr[BeastHeader], size: CInt)(using
        Zone
    ): Ptr[BeastHeaders] =
      val ___ptr = apply()
      (!___ptr).header = header
      (!___ptr).size = size
      ___ptr

    def apply(headers: Map[String, String])(using Zone): Ptr[BeastHeaders] =
      if headers.isEmpty
      then null.asInstanceOf[Ptr[BeastHeaders]]
      else
        val ___ptr = EasyBeast.newHeaders(headers.size)
        var pt = ___ptr._1
        for (name, value) <- headers do
          (!pt).name = name.c_str
          (!pt).value = value.c_str
          pt += 1
        ___ptr

  extension (struct: BeastHeaders)
    def header: Ptr[BeastHeader] = struct._1
    def header_=(value: Ptr[BeastHeader]): Unit = !struct.at1 = value
    def size: CInt = struct._2
    def size_=(value: CInt): Unit = !struct.at2 = value

    def headers: Map[String, String] =
      var ptr = struct.header
      val size = struct.size
      val data = mutable.HashMap.empty[String, String]
      for _ <- 0 until size do
        val name = ptr._1.str
        val value = ptr._2.str
        data.addOne(name -> value)
        ptr += 1
      data.toMap

  // body {str, body raw, int}
  type BeastBody = CStruct3[CString, Ptr[Byte], CInt]
  object BeastBody:
    given _tag: Tag[BeastBody] =
      Tag.materializeCStruct3Tag[CString, Ptr[Byte], CInt]

    def apply()(using Zone): Ptr[BeastBody] = alloc[BeastBody](1)

    def apply(body: CString, buff: Ptr[Byte], size: CInt)(using
        Zone
    ): Ptr[BeastBody] =
      val ___ptr = apply()
      (!___ptr)._body = body
      (!___ptr)._buff = buff
      (!___ptr)._size = size
      ___ptr

    def apply(body: Option[String], rawBody: Option[immutable.Seq[Byte]])(using
        Zone
    ): Ptr[BeastBody] =
      val size = rawBody.map(_.length).getOrElse(0)
      val rawBodyPtr =
        if size == 0
        then null.asInstanceOf[Ptr[Byte]]
        else
          val buff = rawBody.get
          val raw = alloc[Byte](size)
          for i <- 0 until size do raw(i) = buff(i)
          raw
      val bodyPtr = body.map(_.c_str).orNull match
        case null       => null.asInstanceOf[Ptr[Byte]]
        case s: CString => s
      EasyBeast.newBody(bodyPtr, rawBodyPtr, size)

  extension (struct: BeastBody)
    def _body: CString = struct._1
    def _body_=(value: CString): Unit = !struct.at1 = value
    def _buff: CString = struct._2
    def _buff_=(value: CString): Unit = !struct.at2 = value
    def _size: CInt = struct._3
    def _size_=(value: CInt): Unit = !struct.at3 = value

    def body: Option[String] =
      if struct._body == null
      then None
      else
        val b = struct._body
        if b != null
        then Some(b.str)
        else None

    def rawBody: Option[immutable.Seq[Byte]] =
      if struct._buff == null
      then None
      else
        val ptr = struct._buff
        val len = struct._size
        val buff = mutable.ArrayBuilder.ofByte()
        for i <- 0 until len do buff.addOne(ptr(i))
        Some(buff.result().toSeq)

  // verb, target, content type, {body str, body bytes, size} , {[{name, value], size}
  type BeastRequest =
    CStruct5[CString, CString, CString, Ptr[BeastBody], Ptr[BeastHeaders]]
  object BeastRequest:
    given _tag: Tag[BeastRequest] =
      Tag.materializeCStruct5Tag[CString, CString, CString, Ptr[BeastBody], Ptr[
        BeastHeaders
      ]]

    def apply()(using Zone): Ptr[BeastRequest] = alloc[BeastRequest](1)

    def apply(
        method: CString,
        target: CString,
        contentType: CString,
        body: Ptr[BeastBody],
        headers: Ptr[BeastHeaders]
    )(using Zone): Ptr[BeastRequest] =
      val ___ptr = apply()
      (!___ptr)._method = method
      (!___ptr)._target = target
      (!___ptr)._contentType = contentType
      (!___ptr)._body = body
      (!___ptr)._headers = headers
      ___ptr

  extension (struct: BeastRequest)
    def _method: CString = struct._1
    def _method_=(value: CString): Unit = !struct.at1 = value
    def _target: CString = struct._2
    def _target_=(value: CString): Unit = !struct.at2 = value
    def _contentType: CString = struct._3
    def _contentType_=(value: CString): Unit = !struct.at3 = value
    def _body: Ptr[BeastBody] = struct._4
    def _body_=(value: Ptr[BeastBody]): Unit = !struct.at4 = value
    def _headers: Ptr[BeastHeaders] = struct._5
    def _headers_=(value: Ptr[BeastHeaders]): Unit = !struct.at5 = value
    def headers: Map[String, String] = (!struct._headers).headers
    def method: String = struct._method.str
    def target: String = struct._target.str

    def contentType: Option[String] =
      if struct._contentType != null
      then Some(struct._contentType.str)
      else None

    def body: Option[String] =
      if struct._body == null
      then None
      else (!struct._body).body

    def rawBody: Option[immutable.Seq[Byte]] =
      if struct._body == null
      then None
      else (!struct._body).rawBody

  // status {code, content type, {body str, body bytes, size}, {[{name, value], size}}
  type BeastResponse =
    CStruct4[CInt, CString, Ptr[BeastBody], Ptr[BeastHeaders]]
  object BeastResponse:
    given _tag: Tag[BeastResponse] = Tag
      .materializeCStruct4Tag[CInt, CString, Ptr[BeastBody], Ptr[BeastHeaders]]

    def apply()(using Zone): Ptr[BeastResponse] = alloc[BeastResponse](1)

    def apply(
        status: CInt,
        contentType: CString,
        body: Ptr[BeastBody],
        headers: Ptr[BeastHeaders]
    )(using Zone): Ptr[BeastResponse] =
      val ___ptr = apply()
      (!___ptr).status = status
      (!___ptr)._contentType = contentType
      (!___ptr)._body = body
      (!___ptr)._headers = headers
      ___ptr

    def apply(
        status: Int,
        contentType: String,
        body: Option[String],
        rawBody: Option[immutable.Seq[Byte]],
        headers: Map[String, String]
    )(using Zone): Ptr[BeastResponse] =
      val resp = EasyBeast.newResponse(status)
      (!resp)._contentType = contentType.c_str
      (!resp)._body = BeastBody(body, rawBody)
      (!resp)._headers = BeastHeaders(headers)
      resp

  extension (struct: BeastResponse)
    def status: CInt = struct._1
    def status_=(value: CInt): Unit = !struct.at1 = value
    def _contentType: CString = struct._2
    def _contentType_=(value: CString): Unit = !struct.at2 = value
    def _body: Ptr[BeastBody] = struct._3
    def _body_=(value: Ptr[BeastBody]): Unit = !struct.at3 = value
    def _headers: Ptr[BeastHeaders] = struct._4
    def _headers_=(value: Ptr[BeastHeaders]): Unit = !struct.at4 = value

    def headers: Map[String, String] = (!struct._headers).headers

    def contentType: Option[String] =
      if struct._contentType != null
      then Some(struct._contentType.str)
      else None

    def body: Option[String] =
      if struct._body == null
      then None
      else (!struct._body).body

    def rawBody: Option[immutable.Seq[Byte]] =
      if struct._body == null
      then None
      else (!struct._body).rawBody

  type BeastHandlerCallback =
    CFuncPtr2[Ptr[BeastRequest], Ptr[BeastResponse], Unit]
  type BeastHttpHandlerSync = CFuncPtr1[Ptr[BeastRequest], Ptr[BeastResponse]]
  type BeastHttpHandlerAsync =
    CFuncPtr2[Ptr[BeastRequest], BeastHandlerCallback, Unit]

  type ThreadInit = CFuncPtr1[Ptr[Byte], Unit]
  type ThreadStarter = CFuncPtr3[ThreadInit, CInt, Ptr[Byte], Unit]

object Util:
  extension (cs: CString) def str: String = fromCString(cs)

  extension (s: String)(using Zone)
    def c_str: CString =
      toCString(s)

  def threadStart(
      init: Structs.ThreadInit,
      workers: CInt,
      ptr: Ptr[Byte]
  ): Unit =
    val threads =
      for _ <- 0 until workers
      yield new Thread:
        override def run(): Unit =
          init(ptr)

    for t <- threads do t.start()

    for t <- threads do t.join()

object Ext:
  import Structs.*, Util.*

  extension (reqPtr: Ptr[BeastRequest])
    def request(): Request =
      val req = !reqPtr
      Request(
        method = HttpMethod(req.method),
        target = req.target,
        body = req.body.getOrElse(""),
        contentType =
          req.contentType.map(ContentType.make).getOrElse(ContentType.Empty),
        bodyRaw = req.rawBody.getOrElse(Nil),
        headers = req.headers
      )

  extension (respPtr: Ptr[BeastResponse])
    def response(): Response =
      val resp = !respPtr
      Response(
        status = HttpStatus.make(resp.status),
        contentType = ContentType.make(resp.contentType.getOrElse("")),
        body = resp.body.getOrElse(""),
        rawBody = resp.rawBody.getOrElse(Nil),
        headers = resp.headers
      )

  extension (req: Request)(using Zone)
    def ptr(): Ptr[BeastRequest] =
      BeastRequest(
        req.method.verb.c_str,
        req.target.c_str,
        req.contentType.mimeType.c_str,
        BeastBody(
          Option.when(req.body.nonEmpty)(req.body),
          Option.when(req.bodyRaw.nonEmpty)(req.bodyRaw)
        ),
        BeastHeaders(req.headers)
      )

  extension (resp: Response)
    def ptr()(using Zone): Ptr[BeastResponse] =
      BeastResponse(
        status = resp.status.code,
        contentType = resp.contentType.mimeType,
        body = Option.when(resp.body.nonEmpty)(resp.body),
        rawBody = Option.when(resp.rawBody.nonEmpty)(resp.rawBody),
        headers = resp.headers
      )

@linkCppRuntime
@link("EasyBeast")
@extern
object EasyBeast:

  @name("run_sync")
  def runSync(
      hostname: CString,
      port: CUnsignedShort,
      maxThread: CUnsignedShort,
      threadStarter: Structs.ThreadStarter,
      handler: Structs.BeastHttpHandlerSync
  ): CInt = extern

  @name("run_async")
  def runAsync(
      hostname: CString,
      port: CUnsignedShort,
      maxThread: CUnsignedShort,
      threadStarter: Structs.ThreadStarter,
      handler: CFuncPtr
  ): CInt = extern

  @name("response_new")
  def newResponse(statusCode: CInt): Ptr[Structs.BeastResponse] = extern

  @name("body_new")
  def newBody(
      body: CString,
      rawBody: Ptr[Byte],
      size: CInt
  ): Ptr[Structs.BeastBody] = extern

  @name("headers_new")
  def newHeaders(size: CInt): Ptr[Structs.BeastHeaders] = extern
