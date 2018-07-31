package org.hatdex.hat.she.functions

import java.util.UUID

import org.hatdex.hat.api.json.RichDataJsonFormats
import org.hatdex.hat.api.json.ApplicationJsonProtocol
import org.hatdex.hat.api.models.{EndpointData, EndpointDataBundle, FormattedText}
import org.hatdex.hat.api.models.applications.{ApplicationDeveloper, ApplicationGraphics, ApplicationUpdateNotes}
import org.hatdex.serverless.aws.{AnyContent, AnyContentReads}
import org.hatdex.serverless.aws.proxy.{ProxyJsonProtocol, ProxyRequest, ProxyResponse}
import org.joda.time.{DateTime, Period}
import play.api.libs.json.{Format, Json, _}

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
      updateNotes: Option[ApplicationUpdateNotes],
      name: String,
      headline: String,
      description: FormattedText,
      termsUrl: String,
      supportContact: String,
      graphics: ApplicationGraphics,
      dataPreviewEndpoint: Option[String])

  case class FunctionStatus(
      available: Boolean,
      enabled: Boolean,
      lastExecution: Option[DateTime],
      executionStarted: Option[DateTime])

  case class FunctionConfiguration(
      id: String,
      info: FunctionInfo,
      developer: ApplicationDeveloper,
      trigger: FunctionTrigger.Trigger,
      dataBundle: EndpointDataBundle,
      status: FunctionStatus)

  object FunctionTrigger {
    // Sealed type for the different types of function triggers available
    sealed trait Trigger {
      val triggerType: String
    }

    case class TriggerPeriodic(period: Period) extends Trigger {
      final val triggerType: String = "periodic"
    }

    case class TriggerIndividual() extends Trigger {
      final val triggerType: String = "individual"
    }

    case class TriggerManual() extends Trigger {
      final val triggerType: String = "manual"
    }

  }

  import FunctionTrigger._
  import org.hatdex.hat.api.json.ApplicationJsonProtocol.applicationDeveloperFormat
  import ApplicationJsonProtocol.applicationGraphicsFormat
  import ApplicationJsonProtocol.versionFormat
  import ApplicationJsonProtocol.applicationUpdateNotesFormat
  import ApplicationJsonProtocol.formattedTextFormat

  protected implicit val triggerPeriodicFormat: Format[TriggerPeriodic] = Json.format[TriggerPeriodic]

  implicit val triggerFormat: Format[Trigger] = new Format[Trigger] {
    def reads(json: JsValue): JsResult[Trigger] = (json \ "triggerType").as[String] match {
      case "periodic"   => Json.fromJson[TriggerPeriodic](json)(triggerPeriodicFormat)
      case "individual" => JsSuccess(TriggerIndividual())
      case "manual"     => JsSuccess(TriggerManual())
      case triggerType  => JsError(s"Unexpected JSON value $triggerType in $json")
    }

    def writes(trigger: Trigger): JsValue = {
      val triggerJson = trigger match {
        case ds: TriggerPeriodic  => Json.toJson(ds)(triggerPeriodicFormat)
        case _: TriggerIndividual => JsObject(Seq())
        case _: TriggerManual     => JsObject(Seq())
      }
      triggerJson.as[JsObject].+(("triggerType", Json.toJson(trigger.triggerType)))
    }
  }

  implicit val functionInfoFormat: Format[FunctionInfo] = Json.format[FunctionInfo]
  implicit val functionStatusFormat: Format[FunctionStatus] = Json.format[FunctionStatus]
  implicit val functionConfigurationFormat: Format[FunctionConfiguration] = Json.format[FunctionConfiguration]
  implicit val responseFormat: Format[Response] = Json.format[Response]
  implicit val requestFormat: Format[Request] = Json.format[Request]
  implicit val sheRequestFormat: Format[SHERequest] = Json.format[SHERequest]

  implicit val anyContentReads: Reads[ProxyRequest[AnyContent]] = ProxyJsonProtocol.RequestJsonReads[AnyContent](AnyContentReads)
  implicit val proxyBundleWrites: Writes[ProxyResponse[EndpointDataBundle]] = ProxyJsonProtocol.ResponseJsonWrites[EndpointDataBundle]

}
