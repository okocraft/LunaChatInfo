plugins {
    java
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

group = "net.okocraft"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io/")
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.21-R0.1-SNAPSHOT") {
        exclude("com.mojang", "brigadier")
    }
    compileOnly("com.github.ucchyocean:LunaChat:v3.0.16") {
        exclude("org.bstats")
    }
    compileOnly("org.jetbrains:annotations:24.0.1")
}

tasks {
    compileJava {
        options.release.set(17)
    }
}
