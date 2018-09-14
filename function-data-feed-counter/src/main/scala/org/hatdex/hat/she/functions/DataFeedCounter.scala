package org.hatdex.hat.she.functions

import org.hatdex.hat.api.models._
import org.hatdex.hat.api.models.applications.{ApplicationDeveloper, ApplicationGraphics}
import org.hatdex.hat.she.functions.SHEModels._
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import org.joda.time.{DateTime, Period}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

object Client {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
}

class DataFeedCounter {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val namespace = "she"
  val endpoint = "insights/activity-records"

  val configuration: FunctionConfiguration = FunctionConfiguration(
    "data-feed-counter",
    FunctionInfo(
      "1.0.0",
      new DateTime("2018-01-01T12:00:00+00:00"),
      None,
      "Weekly Summary",
      "A summary of your week’s digital activities",
      FormattedText(
        text = """Weekly Summary shows your weekly online activities.
                 |It allows you to to have an overview of your data accumulated in a week. The first weekly summary establish the start date of the tool and is a summary of your history of activities.""".stripMargin,
        None, None),
      "https://hatdex.org/terms-of-service-hat-owner-agreement",
      "contact@hatdex.org",
      ApplicationGraphics(
        Drawable(None, "", None, None),
        Drawable(None, "https://github.com/Hub-of-all-Things/exchange-assets/blob/master/insights-activity-summary/logo.png?raw=true", None, None),
        Seq(Drawable(None, "https://github.com/Hub-of-all-Things/exchange-assets/blob/master/insights-activity-summary/screenshot1.jpg?raw=true", None, None), Drawable(None, "https://github.com/Hub-of-all-Things/exchange-assets/blob/master/insights-activity-summary/screenshot2.jpg?raw=true", None, None))),
      Some("/she/feed/she/activity-records")),
    ApplicationDeveloper("hatdex", "HAT Data Exchange Ltd", "https://hatdex.org", Some("United Kingdom"), None),
    FunctionTrigger.TriggerPeriodic(Period.parse("P1W")),
    dataBundle = bundleFilterByDate(None, None),
    status = FunctionStatus(available = true, enabled = false, lastExecution = None, executionStarted = None))

  protected def dateFilter(fromDate: Option[DateTime], untilDate: Option[DateTime]): Option[FilterOperator.Operator] = {
    val dateTimeFormat: DateTimeFormatter = ISODateTimeFormat.dateTime()
    if (fromDate.isDefined) {
      Some(FilterOperator.Between(Json.toJson(fromDate.map(_.toString(dateTimeFormat))), Json.toJson(untilDate.map(_.toString(dateTimeFormat)))))
    }
    else {
      None
    }
  }

  protected def dateOnlyFilter(fromDate: Option[DateTime], untilDate: Option[DateTime]): Option[FilterOperator.Operator] = {
    if (fromDate.isDefined) {
      Some(FilterOperator.Between(Json.toJson(fromDate.map(_.toString("yyyy-MM-dd"))), Json.toJson(untilDate.map(_.toString("yyyy-MM-dd")))))
    }
    else {
      None
    }
  }

  def bundleFilterByDate(fromDate: Option[DateTime], untilDate: Option[DateTime]): EndpointDataBundle = {
    EndpointDataBundle("data-feed-counter",
      Map(
        "facebook/feed" → PropertyQuery(List(
          EndpointQuery("facebook/feed", Some(Json.toJson(Map("id" → "id"))),
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("created_time", None, f))), None)),
          Some("created_time"), Some("descending"), None),
        "twitter/tweets" → PropertyQuery(List(
          EndpointQuery("twitter/tweets", Some(Json.toJson(Map("id" → "id_str"))),
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("lastUpdated", None, f))), None)),
          Some("lastUpdated"), Some("descending"), None),
        "fitbit/sleep" → PropertyQuery(List(
          EndpointQuery("fitbit/sleep", None,
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("endTime", None, f))), None)),
          Some("endTime"), Some("descending"), None),
        "fitbit/activity" → PropertyQuery(List(
          EndpointQuery("fitbit/activity", None,
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("originalStartTime", None, f))), None)),
          Some("originalStartTime"), Some("descending"), None),
        "fitbit/weight" → PropertyQuery(List(
          EndpointQuery("fitbit/weight", None,
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("date", None, f))), None)),
          Some("date"), Some("descending"), None),
        "calendar/google/events" → PropertyQuery(List(
          EndpointQuery("calendar/google/events", Some(Json.toJson(Map("id" → "id"))),
            Some(Seq(dateFilter(fromDate, untilDate).map(f ⇒ EndpointQueryFilter("start.dateTime", None, f))).flatten), None),
          EndpointQuery("calendar/google/events", Some(Json.toJson(Map("id" → "id"))),
            Some(Seq(dateOnlyFilter(fromDate, untilDate).map(f ⇒ EndpointQueryFilter("start.date", None, f))).flatten), None)),
          Some("start.dateTime"), Some("descending"), None),
        "notables/feed" → PropertyQuery(List(
          EndpointQuery("rumpel/notablesv1", None,
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("created_time", None, f))), None)),
          Some("created_time"), Some("descending"), None),
        "spotify/feed" → PropertyQuery(List(
          EndpointQuery("spotify/feed", None,
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("played_at", None, f))), None)),
          Some("played_at"), Some("descending"), None),
        "monzo/transactions" → PropertyQuery(List(
          EndpointQuery("monzo/transactions", None,
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("created", None, f))), None)),
          Some("created"), Some("descending"), None),
        "she/insights/emotions" → PropertyQuery(List(
          EndpointQuery("she/insights/emotions", None,
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("timestamp", None, f))), None)),
          Some("timestamp"), Some("descending"), None),
        "she/insights/emotions/neutral" → PropertyQuery(List(
          EndpointQuery("she/insights/emotions", None,
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("timestamp", None, f), EndpointQueryFilter("sentiment", None, FilterOperator.Contains(Json.toJson("Neutral"))))), None)),
          Some("timestamp"), Some("descending"), None),
        "she/insights/emotions/negative" → PropertyQuery(List(
          EndpointQuery("she/insights/emotions/negative", None,
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("timestamp", None, f), EndpointQueryFilter("sentiment", None, FilterOperator.Contains(Json.toJson("Negative"))))), None)),
          Some("timestamp"), Some("descending"), None),
        "she/insights/emotions/positive" → PropertyQuery(List(
          EndpointQuery("she/insights/emotions/positive", None,
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("timestamp", None, f), EndpointQueryFilter("sentiment", None, FilterOperator.Contains(Json.toJson("Positive"))))), None)),
          Some("timestamp"), Some("descending"), None)))
  }

  def execute(configuration: FunctionConfiguration, request: Request): Seq[Response] = {
    val counters = request.data
      .collect {
        case (mappingEndpoint, records) ⇒
          (mappingEndpoint, records.flatMap(r ⇒ (r.data \ "id").asOpt[String].orElse(r.recordId.map(_.toString))).toSet.size)
      }

    logger.info(s"Recorded records since ${configuration.status.lastExecution}: $counters")

    val data = Json.obj(
      "timestamp" → Json.toJson(DateTime.now()),
      "since" → Json.toJson(configuration.status.lastExecution),
      "counters" → Json.toJson(counters))

    val response = Response(namespace, endpoint, Seq(data), Seq())

    Seq(response)
  }
}
