package io.fast4s.common

import scala.scalanative.unsafe.*

object syntax:
  extension (cs: CString) def str: String = fromCString(cs)

  extension (s: String)(using Zone)
    def c_str: CString =
      toCString(s)
