import io.fast4s.data.*

package object fast4s:

  export io.fast4s.core.HttpServerBuilder
  export io.fast4s.data.{
    ContentType,
    HttpMethod,
    HttpStatus,
    MimeType,
    Request,
    Response
  }

  export Response.{
    Ok,
    NotFound,
    ServerError,
    BadRequest,
    Created,
    Unauthorized,
    Forbidden
  }

  export io.fast4s.data.HttpMethod.{
    Head,
    Options,
    Patch,
    Get,
    Post,
    Put,
    Delete,
    Trace,
    Connect
  }

  export io.fast4s.api.Fast4s
  export io.fast4s.api.Fast4s.fast

  export io.fast4s.api.router.{
    Enter,
    Leave,
    Route,
    Controller,
    Handler,
    Dispatcher
  }
