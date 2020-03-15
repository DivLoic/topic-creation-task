package io.ldivad.blog.example

import com.typesafe.config.Config
import io.ldivad.blog.example.Configuration.ConfigExample.{TaskExample, TopicExample}
import org.apache.avro.reflect.AvroSchema
import pureconfig.generic.ProductHint
import pureconfig.{CamelCase, ConfigFieldMapping, StringDelimitedNamingConvention}

import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._

/**
 * Created by loicmdivad.
 */
object Configuration {

  implicit def taskHint: ProductHint[TaskExample] =
    ProductHint[TaskExample](ConfigFieldMapping(CamelCase, new StringDelimitedNamingConvention(".")))

  implicit class configMapperOps(config: Config) {

    def toMap: Map[String, AnyRef] = config
      .entrySet()
      .asScala
      .map(pair => (pair.getKey, config.getAnyRef(pair.getKey)))
      .toMap
  }

  case class ConfigExample(kafkaConfig: Config, topics: Vector[TopicExample], taskConfig: TaskExample)

  object ConfigExample {

    case class SchemaExample(subject: String, schema: AvroSchema)

    case class TaskExample(schemaRegistryRetriesNum: Int,
                           schemaRegistryRetriesInterval: Duration,
                           topicCreationTimeout: Duration)

    case class TopicExample(name: String, partitions: Int, replicationFactor: Short)

  }

}
