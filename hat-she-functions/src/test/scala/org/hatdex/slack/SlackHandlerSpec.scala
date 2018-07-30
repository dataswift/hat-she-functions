package org.hatdex.slack

import java.io.ByteArrayOutputStream

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.util.StringInputStream
import org.slf4j.{Logger, LoggerFactory}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class SlackHandlerSpec(implicit ee: ExecutionEnv) extends Specification with Mockito {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val contextMock: Context = mock[Context]
  contextMock.getFunctionName returns "testFunctionName"
  contextMock.getAwsRequestId returns "requestid"
  contextMock.getRemainingTimeInMillis returns 10000

  "`postMessage`" should {
    "Send simple slack message with no attachments" in {
      val s = Json.toJson(SlackModels.Message("Test ping", Seq())).toString

      val is = new StringInputStream(s)
      val os = new ByteArrayOutputStream()

      val result = new PostMessageHandler().postMessage(SlackModels.Message("Test ping", Seq()))
      result map { m =>
        m.text must be equalTo("Test ping")
      } await
    }
  }

  "handler" should {
    "Send simple slack message with no attachments" in {
      val s = Json.toJson(SlackModels.Message("Test ping", Seq())).toString

      val is = new StringInputStream(s)
      val os = new ByteArrayOutputStream()

      val result = new PostMessageHandler().postMessage(SlackModels.Message("Test ping", Seq()))
      result map { m =>
        m.text must be equalTo("Test ping")
      } await
    } skip
  }
}
