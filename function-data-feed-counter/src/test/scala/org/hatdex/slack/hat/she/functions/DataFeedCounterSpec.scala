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
import org.hatdex.hat.she.functions.DataFeedCounter
import org.hatdex.hat.she.functions.SHEModels.Request
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import play.api.Logger
import play.api.libs.json.JodaReads

class DataFeedCounterSpec extends Specification with DataFeedDirectMapperContext with JodaReads {
  val logger = Logger(this.getClass)

  "The `execute` method" should {
    "return correct counters of records" in {
      val request = Request(Map[String, Seq[EndpointData]](
        "twitter" -> Seq(exampleTweetRetweet, exampleTweetMentions),
        "facebook/feed" -> Seq(exampleFacebookPhotoPost, exampleFacebookPost, facebookStory),
        "facebook/events" -> Seq(facebookEvent, facebookEvenNoLocation, facebookEvenPartialLocation),
        "fitbit/sleep" -> Seq(fitbitSleepMeasurement),
        "fitbit/weight" -> Seq(fitbitWeightMeasurement),
        "fitbit/activity" -> Seq(fitbitActivity),
        "fitbit/activity/day/summary" -> Seq(fitbitDaySummary),
        "calendar" -> Seq(googleCalendarEvent, googleCalendarFullDayEvent)), linkRecords = true)

      val function = new DataFeedCounter()
      val responseRecords = function.execute(function.configuration, request)

      responseRecords.headOption must beSome
      responseRecords.head.namespace must be equalTo "she"
      responseRecords.head.endpoint must be equalTo "insights/activity-records"
      responseRecords.head.data.length must be equalTo 1
      (responseRecords.head.data.head \ "counters" \ "facebook/feed").as[Int] must be equalTo 3
      (responseRecords.head.data.head \ "counters" \ "twitter").as[Int] must be equalTo 2

    }

    "include last execution date when available" in {
      val request = Request(Map[String, Seq[EndpointData]](
        "twitter" -> Seq(exampleTweetRetweet, exampleTweetMentions),
        "facebook/feed" -> Seq(exampleFacebookPhotoPost, exampleFacebookPost, facebookStory),
        "facebook/events" -> Seq(facebookEvent, facebookEvenNoLocation, facebookEvenPartialLocation),
        "fitbit/sleep" -> Seq(fitbitSleepMeasurement),
        "fitbit/weight" -> Seq(fitbitWeightMeasurement),
        "fitbit/activity" -> Seq(fitbitActivity),
        "fitbit/activity/day/summary" -> Seq(fitbitDaySummary),
        "calendar" -> Seq(googleCalendarEvent, googleCalendarFullDayEvent)), linkRecords = true)

      val function = new DataFeedCounter()
      val responseRecords = function.execute(function.configuration.copy(status = function.configuration.status.copy(lastExecution = Some(DateTime.now().minusDays(7)))), request)

      responseRecords.headOption must beSome
      responseRecords.head.namespace must be equalTo "she"
      responseRecords.head.endpoint must be equalTo "insights/activity-records"
      responseRecords.head.data.length must be equalTo 1
      (responseRecords.head.data.head \ "since").as[DateTime].isBefore(DateTime.now().minusDays(6)) must beTrue
      (responseRecords.head.data.head \ "counters" \ "facebook/feed").as[Int] must be equalTo 3
      (responseRecords.head.data.head \ "counters" \ "twitter").as[Int] must be equalTo 2
    }
  }
}

