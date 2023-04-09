package one.estrondo.jotawt

abstract class JotawtException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

object JotawtException:

  class Header(message: String, cause: Throwable = null) extends JotawtException(message, cause)

  class InvalidFormat(message: String, cause: Throwable = null) extends JotawtException(message, cause)

  class Key(message: String, cause: Throwable = null) extends JotawtException(message, cause)

  class Token(message: String, cause: Throwable = null) extends JotawtException(message, cause)
