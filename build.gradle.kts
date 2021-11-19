plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    id("net.mamoe.mirai-console") version "2.8.0"
}

group = "io.github.gnuf0rce"
version = "1.0.1"

repositories {
    mavenLocal()
    maven(url = "https://maven.aliyun.com/repository/central")
    mavenCentral()
    maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
    gradlePluginPortal()
}

dependencies {
    implementation("com.baidu.aip:java-sdk:4.16.2")
    testImplementation(kotlin("test", "1.5.31"))
}

tasks {
    test {
        useJUnitPlatform()
    }
}