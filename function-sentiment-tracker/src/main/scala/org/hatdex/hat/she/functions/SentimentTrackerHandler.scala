package org.hatdex.hat.she.functions

import com.amazonaws.services.lambda.runtime.Context
import org.hatdex.hat.api.models._
import org.hatdex.hat.she.functions.SHEModels._
import org.hatdex.serverless.aws.proxy.ErrorResponse
import org.hatdex.serverless.aws.{AnyContent, AnyContentReads, LambdaHandler}
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{DefaultBodyWritables, JsonBodyReadables}

import scala.util.Try

class SentimentTrackerHandler extends LambdaHandler[SHEModels.SHERequest, Seq[Response]] with JsonBodyReadables with DefaultBodyWritables {
  private val counter = new SentimentTracker()

  override def handle(request: SHEModels.SHERequest, context: Context): Try[Seq[Response]] = {
    SentimentTrackerClient.logger.info(s"Handling request $request with context $context")
    Try(counter.execute(SentimentTrackerClient.tokenizer, SentimentTrackerClient.pipeline)(request.functionConfiguration, request.request))
  }
}

class SentimentTrackerConfigurationHandler extends LambdaHandler[AnyContent, JsValue] with JsonBodyReadables with DefaultBodyWritables {
  private val counter = new SentimentTracker()

  def handle(i: AnyContent, context: Context): Try[JsValue] = {
    SentimentTrackerClient.logger.info(s"Handling request $i with context $context")
    Try(Json.toJson(counter.configuration))
  }

  override protected def handleError(errorResponse: ErrorResponse): JsValue = Json.toJson(errorResponse)
}

class SentimentTrackerBundleHandler extends LambdaHandler[JsObject, EndpointDataBundle] with JsonBodyReadables with DefaultBodyWritables {
  private val counter = new SentimentTracker()

  override def handle(request: JsObject, context: Context): Try[EndpointDataBundle] = {
    SentimentTrackerClient.logger.info(s"Handling request $request with context $context")

    Try(counter.bundleFilterByDate(
      (request \ "fromDate").asOpt[String].map(r ⇒ DateTime.parse(r)),
      (request \ "untilDate").asOpt[String].map(r ⇒ DateTime.parse(r))))
  }
}

