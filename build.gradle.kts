import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val ktVersion: String by project
val easylibVersion: String by project
val craftEngineVersion: String by project

plugins {
    java
    id("com.gradleup.shadow")
    id("com.xbaimiao.easylib")
    kotlin("jvm")
}

group = "com.xbaimiao.fastfiller"
version = "2.0.0"
description = "玩家快速填充/清理方块工具"

// easylib-gradle-plugin 的 generatePluginYml 会在 afterEvaluate 里覆盖 project.group,
// tasks {} 中不要直接引用 project.group, 统一使用这个常量
val basePackage = "com.xbaimiao.fastfiller"

easylib {
    env {
        mainClassName = "$basePackage.FastFiller"
        pluginName = "PlayerFiller"
        kotlinVersion = ktVersion
        updateInfo = "支持 1.18.2 - 26.1.2, 重构物品存储与配置"
        // 填充任务基于 Bukkit 全局调度器且会跨区块放置方块, 不支持 Folia
        foliaSupported = false
        authors.add("xbaimiao")
        softDepend.add("Residence")
        softDepend.add("PlotSquared")
        softDepend.add("BentoBox")
        softDepend.add("land")
        softDepend.add("MagicBlock")
        softDepend.add("CraftEngine")
    }
    version = easylibVersion

    library("de.tr7zw:item-nbt-api:2.15.7", false) {
        repo("https://repo.codemc.org/repository/maven-public/")
    }
    relocate("de.tr7zw.changeme.nbtapi", "$basePackage.shadow.itemnbtapi", false)

    relocate("com.xbaimiao.easylib", "$basePackage.easylib", false)
    relocate("kotlin", "$basePackage.shadow.kotlin", true)
    relocate("kotlinx", "$basePackage.shadow.kotlinx", true)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    // CraftEngine
    maven("https://repo.momirealms.net/releases")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("public:res:1.0.0")
    compileOnly("net.momirealms:craft-engine-core:$craftEngineVersion")
    compileOnly("net.momirealms:craft-engine-bukkit:$craftEngineVersion")
    compileOnly(fileTree("libs"))
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = "UTF-8"
    }
    processResources {
        outputs.upToDateWhen { false }
    }
    shadowJar {
        dependsOn("generatePluginYml")
        dependencies {
            easylib.library.forEach {
                if (it.cloud) {
                    exclude(dependency(it.id))
                }
            }
            exclude(dependency("org.slf4j:"))
            exclude(dependency("org.jetbrains:annotations:"))
            exclude(dependency("com.google.code.gson:gson:"))
            exclude(dependency("org.jetbrains.kotlin:"))
            exclude(dependency("org.jetbrains.kotlinx:"))
        }
        archiveClassifier.set("")
        easylib.relocate.forEach {
            relocate(it.pattern, it.replacement)
        }
        minimize()
    }
}
