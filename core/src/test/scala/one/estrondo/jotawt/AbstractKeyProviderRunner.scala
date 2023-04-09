package one.estrondo.jotawt

import one.estrondo.jotawt.KeyReader
import one.estrondo.jotawt.fixture.JWKSSourceFixture
import one.estrondo.jotawt.fixture.JotawtJWKFixture
import one.estrondo.jotawt.fixture.KeyProviderFixture
import one.estrondo.jotawt.generateSecretKey
import one.estrondo.sweetmockito.Answer
import one.estrondo.sweetmockito.SweetMockito
import one.estrondo.sweetmockito.{Effect => SweetEffect}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito

import java.security.Key
import java.util.concurrent.Delayed
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import scala.collection.immutable.HashMap
import scala.reflect.ClassTag

abstract class AbstractKeyProviderRunner[F[+_]: EffectMatcher: Effect](using
    ClassTag[F[Any]],
    SweetEffect[F, Throwable, Any]
) extends Spec[F]:

  override def is = s2"""
  A KeyProviderRunner using ${summon[ClassTag[F[Any]]].runtimeClass.toString()} as effectfull type:
    It should call the callback function with the correct new cache map. ${invokeCallbackWithNewCache}
    It should schedule itself in ScheduledExecutorService. ${shouldScheduleItself}
"""

  def invokeCallbackWithNewCache =
    val openIDConfigurationSource = JWKSSourceFixture.createOpenIDConfiguration()
    val provider                  = KeyProviderFixture.create(openIDConfigurationSource)
    val jwkRSA                    = JotawtJWKFixture.createRSA()
    val jwkSet                    = JotawtJWKSet(keys = Seq(jwkRSA))
    val secretKey                 = generateSecretKey()
    val kid                       = randomCode()
    val expectedNewCache          = Map(kid -> secretKey)

    val context @ (fetcher, keyReader, callback, executor, runner) = createContext(provider)
    SweetMockito
      .whenF(fetcher.get(openIDConfigurationSource))
      .thenReturn(jwkSet)

    SweetMockito
      .whenF(keyReader.create(jwkRSA))
      .thenReturn(kid -> secretKey)

    SweetMockito
      .whenF(callback(provider, expectedNewCache))
      .thenReturn(())

    execute(context)
    Mockito.verify(callback)(provider, expectedNewCache) === null

  def shouldScheduleItself =
    val provider = KeyProviderFixture.create(JWKSSourceFixture.createJWKSUri())

    val (fetcher, keyReader, callback, executor, runner) = createContext(provider)

    Mockito
      .when(
        executor.scheduleWithFixedDelay(any(), eqTo(1000L), eqTo(provider.delay.toMillis), eqTo(TimeUnit.MILLISECONDS))
      )
      .thenReturn(null)

    runner.schedule(executor, fetcher, keyReader, callback)

    Mockito
      .verify(executor)
      .scheduleAtFixedRate(any(), eqTo(1000L), eqTo(provider.delay.toMillis), eqTo(TimeUnit.MILLISECONDS)) === null

  def execute(context: Context) =
    val (fetcher, keyReader, callback, executor, runner) = context
    var runnable: Runnable                                = null

    val future = SweetMockito[ScheduledFuture[_]]

    Mockito
      .when(executor.scheduleAtFixedRate(any(), any(), any(), any()))
      .thenAnswer(invocation => {
        runnable = invocation.getArgument(0)
        future
      })

    Effect[F].run {
      runner.schedule(executor, fetcher, keyReader, callback)
    }

    runnable.run()

  type Context = (
      JWKSFetcher,
      KeyReader,
      (KeyProvider, Map[String, Key]) => F[Unit],
      ScheduledExecutorService,
      KeyProviderRunner
  )

  def createContext(provider: KeyProvider) =
    val fetcherMock    = SweetMockito[JWKSFetcher]
    val keyFactoryMock = SweetMockito[KeyReader]
    val callback       = SweetMockito[(KeyProvider, Map[String, Key]) => F[Unit]]
    val executor       = SweetMockito[ScheduledExecutorService]
    val runner         = KeyProviderRunner(provider)

    SweetMockito.whenF(callback(any(), any()))

    (fetcherMock, keyFactoryMock, callback, executor, runner)
