plugins {
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    id("net.mamoe.mirai-console") version "2.13.0"
    id("me.him188.maven-central-publish") version "1.0.0-dev-3"
    id("me.him188.kotlin-jvm-blocking-bridge") version "2.2.0-172.1"
}

group = "io.github.gnuf0rce"
version = "1.3.8"

mavenCentralPublish {
    useCentralS01()
    singleDevGithubProject("gnuf0rce", "mirai-content-censor", "cssxsh")
    licenseFromGitHubProject("AGPL-3.0")
    workingDir = System.getenv("PUBLICATION_TEMP")?.let { file(it).resolve(projectName) }
        ?: buildDir.resolve("publishing-tmp")
    publication {
        artifact(tasks["buildPlugin"])
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api("xyz.cssxsh.baidu:baidu-aip:3.3.0") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }
    implementation("io.github.kasukusakura:silk-codec:0.0.5")
    compileOnly("xyz.cssxsh.mirai:mirai-administrator:1.3.0")
    compileOnly("xyz.cssxsh.mirai:mirai-hibernate-plugin:2.5.0")
    //
    testImplementation(kotlin("test"))
    testImplementation("org.slf4j:slf4j-simple:2.0.3")
    testImplementation("net.mamoe:mirai-logging-slf4j:2.13.0")
}

mirai {
    jvmTarget = JavaVersion.VERSION_11
}

kotlin {
    explicitApi()
}

tasks {
    test {
        useJUnitPlatform()
    }
}