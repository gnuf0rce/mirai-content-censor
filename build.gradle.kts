plugins {
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
    id("net.mamoe.mirai-console") version "2.11.1"
    id("net.mamoe.maven-central-publish") version "0.7.1"
}

group = "io.github.gnuf0rce"
version = "1.3.1"

mavenCentralPublish {
    useCentralS01()
    singleDevGithubProject("gnuf0rce", "mirai-content-censor", "cssxsh")
    licenseFromGitHubProject("AGPL-3.0", "master")
    publication {
        artifact(tasks.getByName("buildPlugin"))
        artifact(tasks.getByName("buildPluginLegacy"))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("xyz.cssxsh.baidu:baidu-aip:3.1.0") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }
    compileOnly("xyz.cssxsh.mirai:mirai-administrator:1.2.4")
    compileOnly("net.mamoe:mirai-core-utils:2.12.0")
    //
    testImplementation(kotlin("test", "1.6.21"))
}

kotlin {
    explicitApi()
}

tasks {
    test {
        useJUnitPlatform()
    }
}