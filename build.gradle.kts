plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("net.mamoe.mirai-console") version "2.12.1"
    id("me.him188.maven-central-publish") version "1.0.0-dev-3"
    id("me.him188.kotlin-jvm-blocking-bridge") version "2.1.0-170.1"
}

group = "io.github.gnuf0rce"
version = "1.3.7"

mavenCentralPublish {
    useCentralS01()
    singleDevGithubProject("gnuf0rce", "mirai-content-censor", "cssxsh")
    licenseFromGitHubProject("AGPL-3.0")
    workingDir = System.getenv("PUBLICATION_TEMP")?.let { file(it).resolve(projectName) }
        ?: project.buildDir.resolve("publishing-tmp")
    publication {
        artifact(tasks.getByName("buildPlugin"))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api("xyz.cssxsh.baidu:baidu-aip:3.2.0") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }
    compileOnly("xyz.cssxsh.mirai:mirai-administrator:1.2.6")
    compileOnly("xyz.cssxsh.mirai:mirai-hibernate-plugin:2.4.3")
    compileOnly("net.mamoe:mirai-core-utils:2.12.1")
    //
    testImplementation(kotlin("test"))
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