package one.estrondo.jotawt

import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import java.math.BigInteger
import java.util.Base64

def generateKeyPair(): (PublicKey, PrivateKey) =
  val generator = KeyPairGenerator.getInstance("RSA")
  generator.initialize(1048, SecureRandom.getInstance("SHA1PRNG"))
  val pair      = generator.genKeyPair()
  (pair.getPublic(), pair.getPrivate())

def generateSecretKey(length: Int = 64): SecretKey =
  val generator = KeyGenerator.getInstance("HmacSHA512")
  generator.init(length)
  generator.generateKey()

def decodeBase64urlAsBigInteger(raw: String): BigInteger =
  BigInteger(Base64.getUrlDecoder().decode(raw))
