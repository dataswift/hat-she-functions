# HAT SHE Functions

## Introduction
HAT She Functions are built using Scala and deployed on the AWS Lambda Platform. The detailed developer's 
guide to the SMART HAT Engine (aka SHE) can be found here -> https://developers.hubofallthings.com/guides/smart-hat-engine/.

There are 2 projects in this repository - `sentiment-tracker` and `data-feed-counter`. These are the default
SHE functions available to the HAT microserver. This README file serves as a Quickstart guide to developing 
a SHE function in Scala by explaining the code layout of `data-feed-counter`. The command scripts used to
deploy the functions are also described.

## Requirements
* sbt
* serverless [https://serverless.com]
* AWS credentials to deploy lambda functions. Remember to configure your serverless with your AWS credentials.

## What constitutes a SHE function
A SHE function must consists of 3 things
1. an endpoint / method to return its configuration [https://developers.hubofallthings.com/guides/smart-hat-engine/01-function-information-format.html]
2. an endpoint / method to define the `data-bundle` specifying what data it wants to receive, parametrised by the date range (fromDate and untilDate query parameters in ISO8601 format).
3. an endpoint / method that accepts 1 and 2 above and does the actual computation.

## Caveats of a Lambda Function
There are 2 ways of invoking a Lambda function on AWS.
* via HTTP endpoints
* via the protocols provided by the AWS SDKs

**The format of the inbound request is DIFFERENT depending on the invocation method chosen.** HAT executes
lambda functions via SDKs. However it's easier to test with HTTPs endpoints with a client like Postman. Hence
you will see later that both the methods above are coded for in the `data-feed-counter` (and `sentiment-tracker`)
SHE function.

## Breakdown of the `data-feed-counter`
We have broken down the project into 3 files, each with a specific purpose.
Looking into the folder `function-data-feed-counter/src/main/scala/org/hatdex/hat/she/functions/`
1. DataFeedCounter.scala
    * performs the actual processing
2. DataFeedCounterHandler.scala
    * handles invocation by AWS SDKs
3. DataFeedCounterProxyHandler.scala
    * handles invocation via HTTP endpoints

The only difference between 2 and 3 is the way input parameters are processed.

Within the `DataFeedCounterHandler.scala`, you will see 3 classes defined
1. DataFeedCounterConfigurationHandler class (DataFeedCounterHandler.scala:LINE 22) which actually gets the `configuration` object in `DataFeedCounter.scala`(DataFeedCounter.scala:LINE 21)
2. DataFeedCounterBundleHandler class (DataFeedCounterHandler.scala:LINE 31) which calls the helper class/method `bundleFilterByDate` in `DataFeedCounter.scala`(DataFeedCounter.scala:LINE 64) to get the data for processing
3. DataFeedCounterHandler class (DataFeedCounterHandler.scala:LINE 13) which calls the `execute` method in `DataFeedCounter.scala`(DataFeedCounter.scala:LINE 127) to compute and respond

The HTTP counterpart in `DataFeedCounterProxyHandler.scala` are
1. DataFeedCounterProxyHandler.scala:LINE 22
2. DataFeedCounterProxyHandler.scala:LINE 31
3. DataFeedCounterProxyHandler.scala:LINE 13

## Building the project for Lambda deployment
We need to build a fat jar file containing all the required classes and libraries for deployment. Use the command `sbt assembly`.
Refer to `build-artifacts.sh` in the root of the repository.

## Configuring the Lambda deployment with serverless
Refer to the `serverless.yaml` file.
1. You need to provide the location of the jar file you have built above. see line 26.
2. Then you need to define the lambda endpoints
    * Again using `data-feed-counter` as example, look at the `functions` section
    * Note that `api-data-feed-counter`, `api-data-feed-counter-configuration` and `api-data-feed-counter-bundle` all points
    to their respective classes defined in `DataFeedCounterProxyHandler.scala`. This defines HTTPs lambda endpoints
    * Note that `data-feed-counter`, `data-feed-counter-configuration` and `data-feed-counter-bundle` all points
    to their respective classes defined in `DataFeedCounterHandler.scala`. This is directly invoked by AWS SDKs.
3. In the `api-*` sections, the endpoint paths are defined.
    * the path is of the format `<function-name>/<version>`. The endpoint is terminated with `/configuration` and `/data-bundle` for the 
    Configuration and DataBundle functions respectively. The `function-name` and `version` name is defined in the 
    `configuration` object of the `DataFeedCounter.scala`. See lines 22 and 24.

## Deployment
Refer to `build-artifacts.sh` file. This script builds the jar files and makes the deployment using serverless

## Posts deployment testing
Refer to -> https://developers.hubofallthings.com/guides/smart-hat-engine/02-function-testing.html
You can use Postman on your endpoints. The Postman collection can be found in the link immediately above.


