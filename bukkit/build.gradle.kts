plugins {
    java
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

group = "net.okocraft"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.1")
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
