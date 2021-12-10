plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    id("net.mamoe.mirai-console") version "2.9.0-M1"
}

group = "io.github.gnuf0rce"
version = "1.1.1"

mirai {
    configureShadow {
        exclude {
            it.path.startsWith("kotlin") || it.path.startsWith("kotlinx")
        }
        exclude {
            val features = listOf("auth", "compression", "json")
            it.path.startsWith("io/ktor") && features.none { f -> it.path.startsWith("io/ktor/client/features/$f") }
        }
        exclude {
            it.path.startsWith("okhttp3")
        }
        exclude {
            it.path.startsWith("okio")
        }
        exclude {
            it.path.startsWith("org")
        }
    }
}

repositories {
    mavenLocal()
    maven(url = "https://maven.aliyun.com/repository/central")
    mavenCentral()
    maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
    gradlePluginPortal()
}

dependencies {
    implementation("xyz.cssxsh.baidu:baidu-oauth:2.0.3")
    implementation("xyz.cssxsh.baidu:baidu-aip:2.0.3")
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