import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import com.typesafe.config.ConfigFactory
import kamon.Kamon
import org.apache.kafka.common.serialization.ByteArrayDeserializer

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Main extends App with SLF4JLogging {

  val config                = ConfigFactory.load()

  if (config.getBoolean("kamon-enable")) Kamon.start()

  implicit val system = ActorSystem("ReactiveKafkaCantStopWithKamonAkka", config)
  implicit val materializer = ActorMaterializer()

  // localhost:19092 doesn't exist and so causes WakeupException continuously.
  // It is expected that this stream will fail very soon.
  // However, this stream doesn't fail forever until SIGINT(Ctrl+c) or similar is signaled
  // if Kamon enabled, more preceisely, "-javaagent:aspecjweaver.jar" is given and kamon-akka's jar exists in classpath.
  val consumerSetting = ConsumerSettings(system, new ByteArrayDeserializer, new ByteArrayDeserializer)
    .withBootstrapServers("localhost:19092")
    .withGroupId("dummy-group-id")
  val done = Consumer
    .atMostOnceSource(consumerSetting, Subscriptions.topics("topic-no-exist"))
    .toMat(Sink.ignore)(Keep.right)
    .run()

  sys.addShutdownHook {
    log.info("Shutdown hook started.")
    Await.result(system.terminate(), 10 second)
    if (config.getBoolean("kamon-enable")) Kamon.shutdown()
    log.info("Shutdown hook finished.")
  }

  done.onComplete {
    case Failure(th) =>
      log.error("consumer stream ended up with failure by", th)
      sys.exit(1)
    case Success(_) =>
      sys.exit(0)
  }(scala.concurrent.ExecutionContext.Implicits.global)
}
