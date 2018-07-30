package org.hatdex.hat.she.functions

import play.api.libs.json.{Format, Json}
import org.joda.time.{ DateTime, Period }
import play.api.libs.json._
import org.hatdex.hat.api.json.{ ApplicationJsonProtocol, DataFeedItemJsonProtocol, RichDataJsonFormats }
import org.hatdex.hat.api.models.applications._
import org.hatdex.hat.api.models.{ EndpointData, EndpointDataBundle, FormattedText }
import java.util.UUID

object SHEModels extends JodaWrites with JodaReads with RichDataJsonFormats {

  case class SHERequest(
    functionConfiguration: FunctionConfiguration,
    request: Request
  )

  case class Response(
    namespace: String,
    endpoint: String,
    data: Seq[JsValue],
    linkedRecords: Seq[UUID])

  case class Request(
    data: Map[String, Seq[EndpointData]],
    linkRecords: Boolean)

  case class FunctionInfo(
    version: String,
    versionReleaseDate: DateTime,
    name: String,
    headline: String)

  case class FunctionStatus(
    available: Boolean,
    enabled: Boolean,
    lastExecution: Option[DateTime],
    executionStarted: Option[DateTime])

  case class FunctionConfiguration(
    id: String,
    info: FunctionInfo,
    status: FunctionStatus)
  
  implicit val functionInfoFormat: Format[FunctionInfo] = Json.format[FunctionInfo]
  implicit val functionStatusFormat: Format[FunctionStatus] = Json.format[FunctionStatus]
  implicit val functionConfigurationFormat: Format[FunctionConfiguration] = Json.format[FunctionConfiguration]
  implicit val responseFormat: Format[Response] = Json.format[Response]
  implicit val requestFormat: Format[Request] = Json.format[Request]
  implicit val sheRequestFormat: Format[SHERequest] = Json.format[SHERequest]

}
