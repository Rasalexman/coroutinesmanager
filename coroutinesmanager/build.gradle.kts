plugins {
    id("java-library")
    id("kotlin")
    id("maven-publish")
}

val versionName: String by rootProject.extra
val codePath: String by rootProject.extra

// Declare the task that will monitor all configurations.
configurations.all {
    // 2 Define the resolution strategy in case of conflicts.
    resolutionStrategy {
        // Fail eagerly on version conflict (includes transitive dependencies),
        // e.g., multiple different versions of the same dependency (group and name are equal).
        failOnVersionConflict()

        // Prefer modules that are part of this build (multi-project or composite build) over external modules.
        preferProjectModules()
    }
}

sourceSets {
    getByName("main") {
        java.setSrcDirs(listOf(codePath))
    }
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    val settings = rootProject.extra
    val coroutines: String by settings
    api(coroutines)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withJavadocJar()
    withSourcesJar()
}

group = "com.rasalexman.coroutinesmanager"
version = versionName


publishing {
    publications {
        create<MavenPublication>("coroutinesmanager") {
            from(components["kotlin"])

            // You can then customize attributes of the publication as shown below.
            groupId = "com.rasalexman.coroutinesmanager"
            artifactId = "coroutinesmanager"
            version = versionName

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }

    repositories {
        maven {
            name = "coroutinesmanager"
            url = uri("${buildDir}/publishing-repository")
        }
    }
}
