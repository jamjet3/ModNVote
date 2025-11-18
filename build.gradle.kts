plugins {
    java
}

group = "com.modnmetl"
version = "1.1.5"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    // PlaceholderAPI repo (needed ONLY for compilation)
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.5")
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

/**
 * Lightweight jar: only your plugin classes + resources.
 * SQLite is provided by the server’s classpath, not shaded.
 */
tasks.jar {
    archiveFileName.set("modnvote-${project.version}.jar")

    // keep it simple; nothing fancy here
    manifest {
        attributes["Implementation-Title"] = "ModNVote"
        attributes["Implementation-Version"] = project.version
        attributes["Built-By"] = "MODN METL"
    }
}

/**
 * Legacy uberJar task – kept around in case you ever need
 * a fat jar again. CI / releases use the normal `jar` task.
 */
tasks.register<Jar>("uberJar") {
    dependsOn(tasks.named("jar"))
    archiveClassifier.set("")
    val runtimeCp = configurations.runtimeClasspath.get()
    from(sourceSets.main.get().output)
    from(runtimeCp.filter { it.name.endsWith(".jar") }.map { zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Implementation-Title"] = "ModNVote"
        attributes["Implementation-Version"] = project.version
        attributes["Built-By"] = "MODN METL"
    }
}
