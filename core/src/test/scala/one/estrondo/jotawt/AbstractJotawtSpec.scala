package one.estrondo.jotawt

import one.estrondo.jotawt.generateKeyPair
import one.estrondo.sweetmockito.SweetMockito
import one.estrondo.sweetmockito.given
import one.estrondo.sweetmockito.{Effect => SweetEffect}
import org.specs2.execute.AsResult
import org.specs2.execute.Result
import org.specs2.matcher.Matcher
import org.specs2.specification.core.SpecStructure

import scala.reflect.ClassTag
import scala.util.Try
abstract class AbstractJotawtSpec[F[+_]](using
    Effect[F],
    SweetEffect[F, Throwable, Any],
    EffectMatcher[F],
    ClassTag[F[Any]]
) extends Spec[F]:

  override def is: SpecStructure = s2"""
  A Jotawt instance using ${summon[ClassTag[F[Any]]].runtimeClass.toString()} as Effectfull type:
    It should reject an invalid JWT token (bad-format) $invalidHeader
    It should reject an invalid JWT Header (informed by the framework) $badHeaderByFramework
    It should reject a JWT token with no kid $tokenWithnokid
    It should reject when there is no default kid $noDefaultkid
    It should accept a valid JWT token with kid (framework's JWT type) $validWithKidFrameworkTokenType
    It should accept a valid JWT token with kid (framework's Json type) $validWithKidFrameworkJsonType
    It should accept a valid JWT token with no kid (framework's JWT type) $validWithNoKidFrameworkTokenType
    It should accept a valid JWT token with no kid (framework's Json type) $validWithNoKidFrameworkJsonType
"""

  case class TestJson(sub: String)

  case class TestJWT(value: String)

  def createFrameworMock() = SweetMockito[Framework[TestJWT, TestJson]]

  def createKeyRegistryMock() = SweetMockito[KeyRegistry]

  def createJsonReader() = SweetMockito[JsonReader]

  def createJotawt() =
    val framework   = createFrameworMock()
    val keyRegistry = createKeyRegistryMock()
    val jsonReader  = createJsonReader()

    (Jotawt(framework, keyRegistry, jsonReader), framework, keyRegistry, jsonReader)

  def invalidHeader =
    val (jotawt, _, _, _) = createJotawt()
    jotawt.decode(createToken("a", "b")) must fail {
      hasCause(beAnInstanceOf[JotawtException.InvalidFormat])
    }

  def badHeaderByFramework =
    val (jotawt, framework, _, jsonReader) = createJotawt()
    SweetMockito
      .whenF(jsonReader.readTokenHeader[F]("a"))
      .thenFail(IllegalArgumentException("Invalid header!"))

    jotawt.decode(createToken("a", "b", "c")) must fail {
      hasCause(beAnInstanceOf[JotawtException.Header])
    }

  def tokenWithnokid =
    val (jotawt, framework, registry, jsonReader) = createJotawt()

    SweetMockito
      .whenF(jsonReader.readTokenHeader("a"))
      .thenReturn(JotawtHeader(typ = Some("jwt"), kid = Some("123")))

    SweetMockito
      .whenF(registry.search("123"))
      .thenFail(IllegalArgumentException("kid 123 was not found!"))

    jotawt.decode(createToken("a", "b", "c")) must fail {
      hasCause(beAnInstanceOf[JotawtException.Key])
    }

  def noDefaultkid =
    val (jotawt, framework, registry, jsonReader) = createJotawt()

    SweetMockito
      .whenF(jsonReader.readTokenHeader("a"))
      .thenReturn(JotawtHeader(typ = Some("jwt"), kid = None))

    SweetMockito
      .whenF(registry.defaultKey)
      .thenFail(IllegalArgumentException("There is no default kid!"))

    jotawt.decode(createToken("a", "b", "c")) must fail {
      hasCause(beAnInstanceOf[JotawtException.Key])
    }

  def validWithKidFrameworkTokenType =
    val (jotawt, framework, registry, jsonReader) = createJotawt()

    SweetMockito
      .whenF(jsonReader.readTokenHeader("a"))
      .thenReturn(JotawtHeader(typ = Some("jwt"), kid = Some("42")))

    val (public, _) = generateKeyPair()

    SweetMockito
      .whenF(registry.search("42"))
      .thenReturn(public)

    val token         = createToken("a", "b", "c")
    val expectedToken = TestJWT("A super value!")

    SweetMockito
      .whenF(framework.decode(token, public))
      .thenReturn(expectedToken)

    jotawt.decode(token) must success {
      be_==(expectedToken)
    }

  def validWithNoKidFrameworkTokenType =
    val (jotawt, framework, registry, jsonReader) = createJotawt()

    SweetMockito
      .whenF(jsonReader.readTokenHeader("a"))
      .thenReturn(JotawtHeader(typ = Some("jwt"), kid = None))

    val (public, _) = generateKeyPair()

    SweetMockito
      .whenF(registry.defaultKey)
      .thenReturn(public)

    val expectedToken = TestJWT("A super value!")
    val token         = createToken("a", "b", "c")

    SweetMockito
      .whenF(framework.decode(token, public))
      .thenReturn(expectedToken)

    jotawt.decode(token) must success { be_==(expectedToken) }

  def validWithKidFrameworkJsonType =
    val (jotawt, framework, registry, jsonReader) = createJotawt()

    SweetMockito
      .whenF(jsonReader.readTokenHeader("a"))
      .thenReturn(JotawtHeader(typ = Some("jwt"), kid = Some("42")))

    val (public, _) = generateKeyPair()

    SweetMockito
      .whenF(registry.search("42"))
      .thenReturn(public)

    val expectedToken = TestJson("Goku")
    val token         = createToken("a", "b", "c")

    SweetMockito
      .whenF(framework.decodeJson(token, public))
      .thenReturn(expectedToken)

    jotawt.decodeJson(token) must success { be_==(expectedToken) }

  def validWithNoKidFrameworkJsonType =
    val (jotawt, framework, registry, jsonReader) = createJotawt()

    SweetMockito
      .whenF(jsonReader.readTokenHeader("a"))
      .thenReturn(JotawtHeader(typ = Some("jwt"), kid = None))

    val (public, _) = generateKeyPair()

    SweetMockito
      .whenF(registry.defaultKey)
      .thenReturn(public)

    val expectedToken = TestJson("Goku")
    val token         = createToken("a", "b", "c")

    SweetMockito
      .whenF(framework.decodeJson(token, public))
      .thenReturn(expectedToken)

    jotawt.decodeJson(token) must success { be_==(expectedToken) }
