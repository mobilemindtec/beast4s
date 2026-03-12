package fast4s.backend

import io.fast4s.backend.node.{HttpServerAsync, HttpServerSync}
import io.fast4s.core.{HttpServerConfigs, HttpServerCreator, requestBuilder}

package object node:

  // given sync: HttpServerCreator =
  //  (cfg: HttpServerConfigs) => HttpServerSync(cfg)

  given async: HttpServerCreator =
    (cfg: HttpServerConfigs) => HttpServerAsync(cfg)
