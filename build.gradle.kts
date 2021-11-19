plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    id("net.mamoe.mirai-console") version "2.8.0"
}

group = "io.github.gnuf0rce"
version = "1.1.0"

repositories {
    mavenLocal()
    maven(url = "https://maven.aliyun.com/repository/central")
    mavenCentral()
    maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
    gradlePluginPortal()
}

dependencies {
    // implementation("com.baidu.aip:java-sdk:4.16.2")
    implementation("xyz.cssxsh.baidu:baidu-oauth:2.0.1")
    implementation("xyz.cssxsh.baidu:baidu-aip:2.0.1")
    testImplementation(kotlin("test", "1.5.31"))
}

java {
    disableAutoTargetJvm()
}

tasks {
    test {
        useJUnitPlatform()
    }
}