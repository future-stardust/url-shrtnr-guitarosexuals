// 🚫 Dear students, please, don't change this file - all teams should be
// 🚫 in the same conditions -> use same tools/libs.

plugins {
  // Our main language is Java (do you remember the poll! Democracy...)
  java

  // Ancient tool for checking your coding style. But still work. We use Google style 😎
  checkstyle

  // As a REST API framework we use Micronaut. It uses code-generation this is why we need a plugin.
  id("io.micronaut.application") version "1.3.2"

  // Java Code Coverage tool. More details - https://www.jacoco.org/jacoco/
  jacoco

  // Java Microbenchmark Harness for measuring speed of your functions 🚄! More details - https://github.com/openjdk/jmh
  // Helpful article: https://habr.com/ru/post/349914/
  id("me.champeau.gradle.jmh") version "0.5.2"
}

group = "edu.kpi.softtest"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  // Bye-bye jcenter! https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/
  jcenter()
  // We need snapshot repo because latest stable jacoco doesn't support Java 15 🤦🏾‍
  maven("https://oss.sonatype.org/content/repositories/snapshots")
}

micronaut {
  version("2.3.0")
  runtime("netty")
  testRuntime("junit5")
  processing {
    incremental(true)
    annotations("edu.kpi.softtest.*")
  }
}

application {
  mainClass.set("edu.kpi.testcourse.Main")
}

// Gradle has built-in support for JUnit 5 test framework
tasks.withType<Test> {
  useJUnitPlatform()
}

// Ancient tool for checking your coding style. But still work. We use Google style 😎
checkstyle {
  toolVersion = "8.39"
  sourceSets = listOf(project.sourceSets.main.get())
}

jacoco {
  toolVersion = "0.8.7-SNAPSHOT"
}

java {
  // The latest and greatest Java 15 🎆
  sourceCompatibility = JavaVersion.toVersion("15")
  targetCompatibility = JavaVersion.toVersion("15")
}

// We even use experimental🔬 features for Java 15. Use them!
// https://blog.jetbrains.com/idea/2020/09/java-15-and-intellij-idea/
tasks.withType<JavaCompile> { options.compilerArgs.add("--enable-preview") }
tasks.withType<Test> { jvmArgs("--enable-preview") }
tasks.withType<JavaExec> { jvmArgs("--enable-preview") }

dependencies {
  // We use Micronaut framework for REST API implementation. More details - https://micronaut.io/
  implementation("io.micronaut:micronaut-validation")
  implementation("io.micronaut:micronaut-runtime")
  implementation("javax.annotation:javax.annotation-api")
  implementation("io.micronaut:micronaut-http-client")

  // Logging framework. More details - http://logback.qos.ch/
  implementation("ch.qos.logback:logback-classic")

  // We use JWT Authentication (Micronaut has a built-in support for it)
  // Details - https://guides.micronaut.io/micronaut-security-jwt/guide/index.html
  annotationProcessor("io.micronaut.security:micronaut-security-annotations")
  implementation("io.micronaut.security:micronaut-security-jwt")

  // For JSON serialization we use Gson library. More details - https://github.com/google/gson
  implementation("com.google.code.gson:gson:2.8.6")

  // We use JUnit 5 as unit test framework. More details - https://junit.org/junit5/
  testImplementation(platform("org.junit:junit-bom:5.7.0"))
  testImplementation("org.junit.jupiter:junit-jupiter")

  // AssertJ is a library that provides different form of assertions (e.g. assertThat(a).isEqualTo(1))
  // More details - https://assertj.github.io/doc/
  testImplementation("org.assertj:assertj-core:3.18.1")

  // We use Mockito for mocks/stubs/spies. More details - https://site.mockito.org/
  testImplementation("org.mockito:mockito-core:3.7.7")

  // Property-based testing framework. More details - https://github.com/quicktheories/QuickTheories
  testImplementation("org.quicktheories:quicktheories:0.26")
}
