package org.hatdex.hat.she.functions

import com.amazonaws.services.lambda.runtime.Context
import org.hatdex.hat.api.models._
import org.hatdex.hat.she.functions.SHEModels._
import org.hatdex.serverless.aws.{AnyContent, AnyContentReads, LambdaHandler}
import org.joda.time.DateTime
import play.api.libs.json.JsObject
import play.api.libs.ws.{DefaultBodyWritables, JsonBodyReadables}

import scala.util.Try

class DataFeedCounterHandler extends LambdaHandler[SHEModels.SHERequest, Seq[Response]] with JsonBodyReadables with DefaultBodyWritables {
  private val counter = new DataFeedCounter()

  override def handle(request: SHEModels.SHERequest, context: Context): Try[Seq[Response]] = {
    Client.logger.info(s"Handling request $request with context $context")
    Try(counter.execute(request.functionConfiguration, request.request))
  }
}

class DataFeedCounterConfigurationHandler extends LambdaHandler[AnyContent, FunctionConfiguration] with JsonBodyReadables with DefaultBodyWritables {
  private val counter = new DataFeedCounter()

  override def handle(request: AnyContent, context: Context): Try[FunctionConfiguration] = {
    Client.logger.info(s"Handling request $request with context $context")
    Try(counter.configuration)
  }
}

class DataFeedCounterBundleHandler extends LambdaHandler[JsObject, EndpointDataBundle] with JsonBodyReadables with DefaultBodyWritables {
  private val counter = new DataFeedCounter()

  override def handle(request: JsObject, context: Context): Try[EndpointDataBundle] = {
    Client.logger.info(s"Handling request $request with context $context")
    Try(counter.bundleFilterByDate(
      (request \ "fromDate").asOpt[String].map(r ⇒ DateTime.parse(r)),
      (request \ "untilDate").asOpt[String].map(r ⇒ DateTime.parse(r))))
  }
}

