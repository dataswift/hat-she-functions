package org.hatdex.hat.she.functions

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.amazonaws.services.lambda.runtime.Context
import org.hatdex.hat.she.functions.SHEModels._
import org.hatdex.serverless
import org.hatdex.serverless.aws.EventJsonProtocol.eventReads
import org.hatdex.serverless.aws.{Event, LambdaHandler}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.api.libs.ws.{DefaultBodyWritables, JsonBodyReadables}
import org.joda.time.{ DateTime }

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DataFeedCounterHandler extends LambdaHandler[SHEModels.SHERequest, Seq[Response]] with JsonBodyReadables with DefaultBodyWritables {
  
  import SHEModels._

  val counter = new DataFeedCounter()
  override val logger = Client.logger

  def handle(request: SHEModels.SHERequest, context: Context): Try[Seq[Response]] = {
    logger.info(s"Handling request $request with context $context")
    Try(counter.execute(request.functionConfiguration, request.request))
  }

}

class DataFeedCounter {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val namespace = "she"
  val endpoint = "insights/activity-records"

  def execute(configuration: FunctionConfiguration, request: Request): Seq[Response] = {
    val counters = request.data
      .collect {
        case (mappingEndpoint, records) ⇒ (mappingEndpoint, records.length)
      }

    logger.info(s"Recorded records since ${configuration.status.lastExecution}: ${counters}")

    val data = Json.obj(
      "timestamp" → Json.toJson(DateTime.now()),
      "since" → Json.toJson(configuration.status.lastExecution),
      "counters" → Json.toJson(counters))

    val response = Response(namespace, endpoint, Seq(data), Seq())

    Seq(response)
  }
}

object Client {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
}
