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
    implementation(compose.desktop.currentOs)


    implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)")


    implementation("com.google.code.gson:gson:2.8.9")

    implementation(files("libs/commons-lang3-3.8.1.jar"))
    implementation(files("libs/usb4java-1.3.0.jar"))
    implementation(files("libs/usb4java-javax-1.3.0.jar"))
    implementation(files("libs/usb-api-1.0.2.jar"))
    implementation(files("libs/bluecove-2.1.0.jar"))
    implementation("net.sf.bluecove:bluecove-gpl:2.1.0")

    if(org.apache.tools.ant.taskdefs.condition.Os.isFamily(org.apache.tools.ant.taskdefs.condition.Os.FAMILY_MAC)){
        implementation(files("libs/libusb4java-1.3.0-darwin-x86-64.jar"))

    } else {
        implementation(files("libs/libusb4java-1.3.0-linux-aarch64.jar"))
    }


}

compose.desktop {

    application {
        mainClass = "MainKt"
        javaHome = System.getenv("JDK_8")
        nativeDistributions {
            appResourcesRootDir.set(project.layout.projectDirectory.dir("xxx"))
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Intreste"
            packageVersion = "2.0.0"
        }
    }
}
