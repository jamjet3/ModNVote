plugins {
    java
}

group = "com.modnmetl"
version = "1.1.4"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("org.xerial:sqlite-jdbc:3.46.0.0") // provided by server / JVM
}


tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

/**
 * Uber jar that only bundles:
 *  - your plugin classes
 *  - sqlite-jdbc
 *
 * Paper + PlaceholderAPI stay as external plugins, not shaded in.
 */
tasks.register<Jar>("uberJar") {
    dependsOn(tasks.named("jar"))

    // Final filename: modnvote-1.1.3.jar
    archiveFileName.set("modnvote-${project.version}.jar")
    archiveClassifier.set("")

     // Always include our own compiled classes
    from(sourceSets.main.get().output)

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Implementation-Title"] = "ModNVote"
        attributes["Implementation-Version"] = project.version
        attributes["Built-By"] = "MODN METL"
    }
}

/**
 * Make the normal `build` task also produce the uberJar.
 */
tasks.named("build") {
    dependsOn("uberJar")
}
