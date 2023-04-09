package one.estrondo.jotawt

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.KeyGenerator
import scala.util.Random

def encodeBase64Url(content: String): String =
  encodeBase64Url(content.getBytes(StandardCharsets.UTF_8))

def encodeBase64Url(bytes: Array[Byte]): String =
  new String(Base64.getUrlEncoder().encode(bytes), StandardCharsets.UTF_8)

def createToken(parts: String*): String =
  parts.map(encodeBase64Url).mkString(".")

def randomCode(length: Int = 8): String =
  val bytes   = Array.ofDim[Byte](1)
  val builder = StringBuilder()
  for _ <- 0 until length yield
    val value = bytes.head.abs
    Random.nextBytes(bytes)
    if (value < 16)
      builder.addOne('0')
    builder.addAll(Integer.toString(value, 16))

  builder.result()

def createRandomBigInteger(length: Int) =
  val bytes = Array.ofDim[Byte](length)
  Random.nextBytes(bytes)
  BigInteger(bytes).abs()