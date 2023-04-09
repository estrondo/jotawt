package one.estrondo.jotawt

import one.estrondo.jotawt.fixture.JWKSSourceFixture
import one.estrondo.sweetmockito.SweetMockito
import org.specs2.specification.core.SpecStructure
import one.estrondo.sweetmockito.{Effect => SweetEffect}

import scala.reflect.ClassTag
import one.estrondo.jotawt.fixture.JotawtJWKFixture
import org.mockito.Mockito
import one.estrondo.jotawt.fixture.KeyProviderFixture

abstract class AbstractJWKSFetcherSpec[F[_]: Effect: EffectMatcher](using
    ClassTag[F[Any]],
    SweetEffect[F, Throwable, Any]
) extends Spec[F]:

  override def is = s2"""
  A JWKSFetcher using ${summon[ClassTag[F[Any]]].runtimeClass.toString()} as effectfull type:
    It should read a JWKS from an OpenID Configuration Endpoint ${shouldReadFromOpenIDConfiguration}
    IT should read a JWKS from a JWKS Endpoint ${shouldReadFromJWKSEndpoint}
""""

  def shouldReadFromOpenIDConfiguration =
    val source = JWKSSourceFixture.createOpenIDConfiguration()

    val expectedOpenIDMetadata            = OpenIDProviderMetada(jkws_uri = s"https://${randomCode()}/jwks")
    val expectedJWKSet                    = JotawtJWKSet(keys = Seq(JotawtJWKFixture.createRSA()))
    val (httpClient, jsonReader, fetcher) = createContext()

    SweetMockito
      .whenF(httpClient.get(source.uri))
      .thenReturn("<json-OpenIDMetadata>")

    SweetMockito
      .whenF(jsonReader.readOpenIDProviderMetada("<json-OpenIDMetadata>"))
      .thenReturn(expectedOpenIDMetadata)

    SweetMockito
      .whenF(httpClient.get(expectedOpenIDMetadata.jkws_uri))
      .thenReturn("<json-JWKS>")

    SweetMockito
      .whenF(jsonReader.readJWKSet("<json-JWKS>"))
      .thenReturn(expectedJWKSet)

    Effect[F].run(fetcher.get(source)) must beEqualTo(expectedJWKSet)

  def shouldReadFromJWKSEndpoint =
    val source   = JWKSSourceFixture.createJWKSUri()
    val provider = KeyProviderFixture.create(source)

    val expectedJWKSet = JotawtJWKSet(keys = Seq(JotawtJWKFixture.createRSA()))

    val (httpClient, jsonReader, fetcher) = createContext()
    SweetMockito
      .whenF(httpClient.get(source.uri))
      .thenReturn("<json-JWKS>")

    SweetMockito
      .whenF(jsonReader.readJWKSet("<json-JWKS>"))
      .thenReturn(expectedJWKSet)

    Effect[F].run(fetcher.get(source)) must beEqualTo(expectedJWKSet)

  type Context = (
      HttpClient,
      JsonReader,
      JWKSFetcher
  )

  def createContext(): Context =
    val httpClient = SweetMockito[HttpClient]
    val jsonReader = SweetMockito[JsonReader]

    (httpClient, jsonReader, JWKSFetcher(httpClient, jsonReader))
