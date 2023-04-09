package one.estrondo.jotawt

import scala.util.Try

class KeyReaderSpec extends Spec[Try]:

  override def is = s2"""
The KeyReader object:
  It should support the following ktys: EC, RSA. ${supportedKtys}
"""

  def supportedKtys =
    KeyReader.supportedKty must beEqualTo(Set("EC", "RSA"))
