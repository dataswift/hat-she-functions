/*
 * Copyright (C) 2017 HAT Data Exchange Ltd
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>
 * 5 / 2018
 */

package org.hatdex.slack.hat.she.functions

import org.hatdex.hat.api.models.EndpointData
import org.hatdex.hat.she.functions.SHEModels.Request
import org.hatdex.hat.she.functions.{SentimentTracker, SentimentTrackerClient}
import org.specs2.mutable.Specification
import play.api.Logger
import play.api.libs.json.JodaReads

class SentimentTrackerSpec extends Specification with DataFeedDirectMapperContext with JodaReads {
  val logger = Logger(this.getClass)

  "The `execute` method" should {
    "return sentiments for each item with text" in {
      val request = Request(Map[String, Seq[EndpointData]](
        "twitter" -> Seq(exampleTweetRetweet, exampleTweetMentions),
        "facebook/feed" -> Seq(exampleFacebookPhotoPost, exampleFacebookPost, facebookStory),
        "rumpel/notablesv1" -> Seq(exampleNotable)), linkRecords = true)

      val function = new SentimentTracker()
      val responseRecords = function.execute(SentimentTrackerClient.tokenizer, SentimentTrackerClient.pipeline)(
        function.configuration, request)

      responseRecords.length must be equalTo 6
      responseRecords.headOption must beSome
      responseRecords.head.namespace must be equalTo "she"
      responseRecords.head.endpoint must be equalTo "insights/emotions"
      responseRecords.head.data.length must be equalTo 1

      responseRecords.forall(r ⇒ (r.data.head \ "text").asOpt[String] must beSome)
      responseRecords.forall(r ⇒ (r.data.head \ "sentiment").as[String] must beOneOf("Very Negative", "Negative", "Neutral", "Positive", "Very Positive"))

    }
  }
}

