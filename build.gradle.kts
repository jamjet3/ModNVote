plugins {
    `java`
}

group = "com.modnmetl"
version = "1.1.3"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation("org.xerial:sqlite-jdbc:3.46.0.1")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

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
