plugins {
    scala
    application
}

repositories {
    jcenter()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClassName = "io.ldivad.blog.example.Main"
}

dependencies {
    implementation("org.scala-lang:scala-library:2.13.1")
    implementation("org.typelevel:cats-core_2.13:2.1.0")

    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.github.pureconfig:pureconfig_2.13:0.12.2")

    implementation("org.apache.avro:avro:1.9.1")
    implementation("org.apache.kafka:kafka-clients:2.4.0")
    implementation("org.apache.kafka:kafka-streams-scala_2.13:2.4.0")

    testImplementation("org.scalatest:scalatest_2.13:3.1.1")
}

val topicCreation by tasks.register<JavaExec>("topicCreation") {
    main = "io.ldivad.blog.example.TopicCreation"
    classpath = sourceSets["main"].runtimeClasspath
}

val schemaCreation by tasks.register<JavaExec>("schemaCreation") {
    main = "io.ldivad.blog.example.SchemaCreation"
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.getByName("run")
        .dependsOn(topicCreation)
        .dependsOn(schemaCreation)
