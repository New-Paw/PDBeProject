// import external variables from gradle.properties
val externalGroup: String by project
val externalVersion: String by project
val externalMainClassName: String by project

plugins {
  // Apply the java plugin to add support for Java
  id("java")
  // Apply the application plugin to add support for building an application
  //  id("application")
  // Apply the Maven publish plugin to publish resulting artefacts into a Maven repository
  id("maven-publish")

  id ("war")

}

group = externalGroup
version = externalVersion

//application {
  // Define the main class for the application
//  mainClass.set(externalMainClassName)
//}

// prevent the use of non reproducible dependencies
// configurations.all {
//  resolutionStrategy {
//    failOnNonReproducibleResolution()
//  }
// }


dependencies {
  // Servlet API
  compileOnly("jakarta.servlet:jakarta.servlet-api:5.0.0")
  // Oracle JDBC Driver compatible with JDK11
  implementation("com.oracle.database.jdbc:ojdbc11-production:23.3.0.23.09")
  // Oracle XML Database
  implementation("com.oracle.weblogic:oracle.xdb_12.1.0:12.1.3-0-0")
  // Oracle Multimedia Database
  implementation("oracle.ord.im:ordhttp:12.2.0.1.0")
  implementation("oracle.ord.im:ordim:12.2.0.1.0")
  implementation("oracle.sqlj:runtime12:18.3.0.0.0")
  // Oracle SDO Locator Objects
  implementation("oracle.sdo.locator:sdoapi:12.2.0.1.0")
  implementation("oracle.sdo.locator:sdogr:12.2.0.1.0")
  implementation("oracle.sdo.locator:sdonm:12.2.0.1.0")
  implementation("oracle.sdo.locator:sdotopo:12.2.0.1.0")
  // Use JUnit test framework
  testImplementation(platform("org.junit:junit-bom:5.11.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
  // for compatibility with Hadoop distributions such as Cloudera QuickStarts for CDH 5.13 (Java 1.7.0_67), however, increased to the minimal 1.8 version of JDK available in Gradle
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

// WAR configuration
tasks.withType<War> {
    archiveFileName.set("oracle-lab-multimedia.war")
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
    }
  }
  repositories {
    maven {
      name = "build-repository"
      url = uri(layout.buildDirectory.dir("mvn-repo"))
    }
  }
}

tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
}

//tasks.named<JavaExec>("run") {
//  // Define default system properties applied to the run task and in the generated start scripts of the distribution
//  // systemProperty("login", "mylogin")
//  // systemProperty("password", "mypassword")
//  // Pass all system properties to the application and the start scripts (overwrite previously defined defaults if any)
//  systemProperties(System.getProperties().mapKeys { it.key as String })
//  classpath = sourceSets["main"].runtimeClasspath
//}

tasks.withType<Test> {
  useJUnitPlatform()
}

//tasks.withType<Jar> {
//  manifest {
//    attributes(
//      "Built-By" to System.getProperty("user.name"),
//      "Build-Jdk" to System.getProperty("java.version"),
//      "Main-Class" to externalMainClassName,
//    )
//  }
//}

// ensure proper reproducibility of the genereated .jar files
tasks.withType<AbstractArchiveTask>().configureEach {
  isPreserveFileTimestamps = false
  isReproducibleFileOrder = true
}

//// use dynamic classpath instead of a list of JARs; prevents "The input line is too long" on Windows
//tasks.withType<CreateStartScripts> {
//  classpath = files("lib/*")
//}
