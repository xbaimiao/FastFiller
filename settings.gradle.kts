pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.xbaimiao.com/repository/maven-public/")
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
    //kotlin 版本
    val ktVersion: String by settings
    //shadowJar 版本
    val shadowJarVersion: String by settings
    val easylibPluginVersion: String by settings
    plugins {
        kotlin("jvm") version ktVersion
        id("com.gradleup.shadow") version shadowJarVersion
        id("com.xbaimiao.easylib") version easylibPluginVersion
    }
}
rootProject.name = "PlayerFiller"
