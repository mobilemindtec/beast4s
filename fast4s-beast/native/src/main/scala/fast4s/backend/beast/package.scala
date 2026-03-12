package fast4s.backend

import io.fast4s.backend.beast.{HttpServerAsync, HttpServerSync}
import io.fast4s.core.{HttpServerConfigs, HttpServerCreator, requestBuilder}

package object beast:

  given sync: HttpServerCreator =
    (cfg: HttpServerConfigs) => HttpServerSync(cfg)

  given async: HttpServerCreator =
    (cfg: HttpServerConfigs) => HttpServerAsync(cfg)
