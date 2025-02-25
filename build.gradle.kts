plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
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
    implementation("net.okocraft:lunachatinfo-bukkit:1.0.0")
    implementation("net.okocraft:lunachatinfo-bungeecord:1.0.0")
    implementation("net.okocraft:lunachatinfo-velocity:1.0.0")
}

tasks {
    compileJava {
        options.release.set(17)
    }
    build {
        dependsOn(shadowJar)
    }
}
