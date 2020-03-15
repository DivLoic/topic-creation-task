package io.ldivad.blog.example

import java.util.concurrent.ExecutionException

import cats.syntax.either._
import io.ldivad.blog.example.Configuration.{ConfigExample, _}
import org.apache.kafka.clients.admin.{Admin, CreateTopicsResult, NewTopic}
import org.apache.kafka.common.errors.TopicExistsException
import org.slf4j.{Logger, LoggerFactory}
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

/**
 * Created by loicmdivad.
 */
object TopicCreation extends App {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  ConfigSource.default.load[ConfigExample].map { config =>

    val client = Admin.create(config.kafkaConfig.toMap.asJava)

    val newTopics = config.topics.map(topic => new NewTopic(topic.name, topic.partitions, topic.replicationFactor))

    logger.info(s"Starting the topics creation for: ${config.topics.map(_.name).mkString(", ")}")

    val allResults: CreateTopicsResult = client.createTopics(newTopics.asJava)

    allResults.values().asScala.foreach { case (topicName, kafkaFuture) =>

      kafkaFuture.whenComplete {

        case (_, throwable: Throwable) if Option(throwable).isDefined =>
          logger warn s"Topic creation failed due to a ${throwable.getClass.getSimpleName}: ${throwable.getMessage}"

        case _ =>
          newTopics find(_.name() == topicName) map { topic =>
            logger.info(
              s"""|Topic ${topic.name}
                  | has been successfully created with ${topic.numPartitions} partitions
                  | and replicated ${topic.replicationFactor() - 1} times""".stripMargin.replaceAll("\n", "")
            )
          }
      }
    }

    val wait: Duration = config.taskConfig.topicCreationTimeout
    val (delay, timeUnit) = (wait._1, wait._2)

    Try(allResults.all().get(delay, timeUnit)) match {

      case Failure(ex) if ex.getCause.isInstanceOf[TopicExistsException] =>
        logger info "Creation stage completed."

      case failure@Failure(_: InterruptedException | _: ExecutionException) =>
        logger error "The topic creation failed to complete"
        failure.exception.printStackTrace()
        sys.exit(2)

      case Failure(exception) =>
        logger error "The following exception occurred during the topic creation"
        exception.printStackTrace()
        sys.exit(3)

      case Success(_) =>
        logger info "Creation stage completed."
    }
  }
    .recover {
      case failures: ConfigReaderFailures =>
        failures.toList.foreach(failure => logger.error(failure.description))
        sys.exit(1)

      case failures =>
        logger.error("Unknown error: ", failures)
        sys.exit(1)
    }
}

