// import external variables from gradle.properties
val externalRootProjectName: String by settings

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositories {
    // Public repository
    mavenCentral()
    // Private/Custom repository for the Database Systems courses
    maven {
      name = "rychly-edu-dbs-mvn-repo"
      url = uri("https://rychly-edu.gitlab.io/dbs/mvn-repo")
    }
  }
  // Highly recommended, see https://docs.gradle.org/current/userguide/declaring_repositories.html#sub:centralized-repository-declaration
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

rootProject.name = externalRootProjectName
