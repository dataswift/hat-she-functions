package org.hatdex.hat.she.functions

import java.util.{Collections, Properties}

import com.amazonaws.services.lambda.runtime.Context
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.models.applications.{ApplicationDeveloper, ApplicationGraphics}
import org.hatdex.hat.she.functions.SHEModels._
import org.hatdex.serverless.aws.proxy.{ProxyRequest, ProxyResponse}
import org.hatdex.serverless.aws.{AnyContent, AnyContentReads, LambdaHandler, LambdaProxyHandler}
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import org.joda.time.{DateTime, Period}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import play.api.libs.ws.{DefaultBodyWritables, JsonBodyReadables}

import scala.collection.JavaConverters._
import scala.util.Try

class SentimentTrackerHandler extends LambdaProxyHandler[SHEModels.SHERequest, Seq[Response]] with JsonBodyReadables with DefaultBodyWritables {
  private val counter = new SentimentTracker()

  override def handle(request: SHEModels.SHERequest, context: Context): Try[Seq[Response]] = {
    SentimentTrackerClient.logger.info(s"Handling request $request with context $context")
    Try(counter.execute(SentimentTrackerClient.tokenizer, SentimentTrackerClient.pipeline)(request.functionConfiguration, request.request))
  }

}

class SentimentTrackerConfigurationHandler extends LambdaProxyHandler[AnyContent, FunctionConfiguration] with JsonBodyReadables with DefaultBodyWritables {
  private val counter = new SentimentTracker()

  override def handle(context: Context): Try[FunctionConfiguration] = {
    SentimentTrackerClient.logger.info(s"Handling request with context $context")
    Try(counter.configuration)
  }

}

class SentimentTrackerBundleHandler extends LambdaHandler[ProxyRequest[AnyContent], ProxyResponse[EndpointDataBundle]] with JsonBodyReadables with DefaultBodyWritables {
  private val counter = new SentimentTracker()

  override def handle(request: ProxyRequest[AnyContent], context: Context): Try[ProxyResponse[EndpointDataBundle]] = {
    SentimentTrackerClient.logger.info(s"Handling request with context $context")
    val result = Try(counter.bundleFilterByDate(
      request.queryStringParameters.flatMap(_.get("fromDate").map(r ⇒ DateTime.parse(r))),
      request.queryStringParameters.flatMap(_.get("untilDate").map(r ⇒ DateTime.parse(r)))))

    Try(ProxyResponse(result))
  }

}

object SentimentTrackerClient {
  lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  lazy val (tokenizer, pipeline): (StanfordCoreNLP, StanfordCoreNLP) = buildPipeline()

  def buildPipeline(): (StanfordCoreNLP, StanfordCoreNLP) = {
    val pipelineProps = new Properties

    pipelineProps.setProperty("annotators", "parse, sentiment")
    pipelineProps.setProperty("parse.binaryTrees", "true")
    pipelineProps.setProperty("enforceRequirements", "false")

    val tokenizerProps = new Properties()
    tokenizerProps.setProperty("annotators", "tokenize, ssplit")
    tokenizerProps.setProperty(StanfordCoreNLP.NEWLINE_SPLITTER_PROPERTY, "true")

    val tokenizer = new StanfordCoreNLP(tokenizerProps)
    val pipeline = new StanfordCoreNLP(pipelineProps)

    (tokenizer, pipeline)
  }
}

class SentimentTracker {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val namespace = "she"
  val endpoint = "insights/emotions"

  val configuration: FunctionConfiguration = FunctionConfiguration(
    "sentiment-tracker",
    FunctionInfo(
      "1.0.0",
      new DateTime("2018-01-01T12:00:00+00:00"),
      None,
      "Sentiment Tracker",
      "Sentiment in your words",
      FormattedText(
        text = """Sentiment Tracker analyses your texts on Facebook, Twitter and Notables to work out how negative or positive your postings are.""".stripMargin,
        None, None),
      "https://hatdex.org/terms-of-service-hat-owner-agreement",
      "contact@hatdex.org",
      ApplicationGraphics(
        Drawable(None, "", None, None),
        Drawable(None, "https://github.com/Hub-of-all-Things/exchange-assets/blob/master/insights-activity-summary/logo.png?raw=true", None, None),
        Seq(Drawable(None, "https://github.com/Hub-of-all-Things/exchange-assets/blob/master/insights-activity-summary/screenshot1.jpg?raw=true", None, None), Drawable(None, "https://github.com/Hub-of-all-Things/exchange-assets/blob/master/insights-activity-summary/screenshot2.jpg?raw=true", None, None))),
      Some("/she/feed/she/sentiments")),
    ApplicationDeveloper("hatdex", "HATDeX", "https://hatdex.org", Some("United Kingdom"), None),
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
          EndpointQuery("facebook/feed", Some(Json.toJson(Map("message" → "message", "timestamp" → "created_time"))),
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("created_time", None, f))), None)),
          Some("created_time"), Some("descending"), Some(20)),
        "twitter/tweets" → PropertyQuery(List(
          EndpointQuery("twitter/tweets", Some(Json.toJson(Map("message" → "text", "timestamp" → "lastUpdated"))),
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("lastUpdated", None, f))), None)),
          Some("lastUpdated"), Some("descending"), Some(20)),
        "notables/feed" → PropertyQuery(List(
          EndpointQuery("rumpel/notablesv1", Some(Json.toJson(Map("message" → "message", "timestamp" → "created_time"))),
            dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("created_time", None, f))), None)),
          Some("created_time"), Some("descending"), Some(20))
      ))
  }

  def execute(tokenizer: StanfordCoreNLP, pipeline: StanfordCoreNLP)(configuration: FunctionConfiguration, request: Request): Seq[Response] = {
    logger.debug(s"running version ${configuration.info.version}")
    val messages = request.data
      .collect {
        case (mappingEndpoint, records) ⇒
          val endpointSentiments = records.flatMap { dataRecord ⇒
            Try((dataRecord, (dataRecord.data \ "message").as[String], (dataRecord.data \ "timestamp").as[DateTime])).toOption
          } map {
            case (dataRecord, text, timestamp) ⇒
              val annotation: Annotation = new Annotation(text)
              tokenizer.annotate(annotation)

              val sentenceAnnotations: Seq[Annotation] = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])
                .asScala.map { sentence ⇒
                val nextAnnotation: Annotation = new Annotation(sentence.get(classOf[CoreAnnotations.TextAnnotation]))
                nextAnnotation.set(classOf[CoreAnnotations.SentencesAnnotation], Collections.singletonList(sentence))
                nextAnnotation
              }

              val sentenceSentiments: Seq[(String, BigDecimal, BigDecimal)] = sentenceAnnotations.flatMap { annotatedSentence ⇒
                pipeline.annotate(annotatedSentence)

                annotatedSentence.get(classOf[CoreAnnotations.SentencesAnnotation]).asScala.map { sentence ⇒
                  val tree = sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])
                  val sentiment = RNNCoreAnnotations.getPredictedClass(tree)

                  (sentence.toString, BigDecimal(sentiment), BigDecimal(sentence.toString.length))
                }
              }

              val itemSentiment = sentenceSentiments.reduceLeft( (item, sentence) ⇒ {
                (item._1.concat(sentence._1),
                  ((item._2 * item._3) + (sentence._2 * sentence._3))/(item._3 + sentence._3),
                  item._3 + sentence._3)
              })

              val sentimentToString = if (itemSentiment._2 < 1) {
                "Very Negative"
              } else if (itemSentiment._2 >= 1 && itemSentiment._2 < 2) {
                "Negative"
              } else if (itemSentiment._2 >= 2 && itemSentiment._2 < 3) {
                "Neutral"
              } else if (itemSentiment._2 >= 3 && itemSentiment._2 < 4) {
                "Positive"
              } else {
                "Very Positive"
              }

              Response(namespace, endpoint,
                Seq(Json.toJson(Map(
                  "timestamp" → timestamp.toString,
                  "source" → mappingEndpoint,
                  "text" → itemSentiment._1,
                  "sentiment" → sentimentToString))),
                Seq(dataRecord.recordId).flatten)
          }
          endpointSentiments
      }

    messages.flatten.toSeq
  }

}

