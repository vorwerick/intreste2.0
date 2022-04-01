import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version ("1.6.10")
    id("org.jetbrains.compose") version ("1.1.0-alpha04")
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    //implementation(compose.desktop.currenOS)
    implementation(compose.desktop.linux_arm64)


    implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)")
    implementation(
        group = "org.usb4java",
        name = "usb4java-javax",
        version = "1.2.0"
    ) //Thanks for using https://jar-download.com )
    implementation(
        group = "javax.usb",
        name = "usb-api",
        version = "1.0.2"
    ) //Thanks for using https://jar-download.com )
    implementation("com.google.code.gson:gson:2.8.9")
    implementation(files("libs/bluecove-2.1.0.jar"))
}

compose.desktop {

    application {
        mainClass = "MainKt"

        nativeDistributions {
            appResourcesRootDir.set(project.layout.projectDirectory.dir("xxx"))
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Intreste"
            packageVersion = "2.0.0"
        }
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
}

tasks.register<Jar>("uberJar") {
    archiveClassifier.set("uber")

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}


