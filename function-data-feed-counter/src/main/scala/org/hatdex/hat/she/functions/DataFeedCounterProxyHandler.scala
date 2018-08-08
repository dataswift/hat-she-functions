package org.hatdex.hat.she.functions

import com.amazonaws.services.lambda.runtime.Context
import org.hatdex.hat.api.models._
import org.hatdex.hat.she.functions.SHEModels._
import org.hatdex.serverless.aws.proxy.{ProxyRequest, ProxyResponse}
import org.hatdex.serverless.aws.{AnyContent, AnyContentReads, LambdaHandler, LambdaProxyHandler}
import org.joda.time.DateTime
import play.api.libs.ws.{DefaultBodyWritables, JsonBodyReadables}

import scala.util.Try

class DataFeedCounterProxyHandler extends LambdaProxyHandler[SHEModels.SHERequest, Seq[Response]] with JsonBodyReadables with DefaultBodyWritables {
  private val counter = new DataFeedCounter()

  override def handle(request: SHEModels.SHERequest, context: Context): Try[Seq[Response]] = {
    Client.logger.info(s"Handling request $request with context $context")
    Try(counter.execute(request.functionConfiguration, request.request))
  }
}

class DataFeedCounterConfigurationProxyHandler extends LambdaProxyHandler[AnyContent, FunctionConfiguration] with JsonBodyReadables with DefaultBodyWritables {
  private val counter = new DataFeedCounter()

  override def handle(context: Context): Try[FunctionConfiguration] = {
    Client.logger.info(s"Handling request with context $context")
    Try(counter.configuration)
  }
}

class DataFeedCounterBundleProxyHandler extends LambdaHandler[ProxyRequest[AnyContent], ProxyResponse[EndpointDataBundle]] with JsonBodyReadables with DefaultBodyWritables {
  private val counter = new DataFeedCounter()

  override def handle(request: ProxyRequest[AnyContent], context: Context): Try[ProxyResponse[EndpointDataBundle]] = {
    Client.logger.info(s"Handling request with context $context")
    val result = Try(counter.bundleFilterByDate(
      request.queryStringParameters.flatMap(_.get("fromDate").map(r ⇒ DateTime.parse(r))),
      request.queryStringParameters.flatMap(_.get("untilDate").map(r ⇒ DateTime.parse(r)))))

    Try(ProxyResponse(result))
  }
}

