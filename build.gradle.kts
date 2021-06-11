plugins {
    kotlin("jvm") version "1.4.31"
    kotlin("plugin.serialization") version "1.4.31"
    id("net.mamoe.mirai-console") version "2.6.6"
}

group = "io.github.gnuf0rce"
version = "0.1.0-dev"

repositories {
    mavenLocal()
    maven(url = "https://maven.aliyun.com/repository/releases")
    maven(url = "https://maven.aliyun.com/repository/public")
    mavenCentral()
    jcenter()
    maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
    gradlePluginPortal()
}

dependencies {
    implementation("com.baidu.aip:java-sdk:4.16.2")
    testImplementation(platform("org.junit:junit-bom:5.7.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
    test {
        useJUnitPlatform()
    }
}