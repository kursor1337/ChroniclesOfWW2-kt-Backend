import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val koin_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposedVersion: String by project
val model_version: String by project
val dtos_version: String by project


plugins {
    application
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.kursor.chroniclesofww2"
version = "0.0.1"
application {
    mainClass.set("com.kursor.chroniclesofww2.App")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {

    implementation(project(":models"))
    implementation(project(":data"))

    //cli
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")

    //crypt
    implementation("org.mindrot:jbcrypt:0.4")

    // Koin Core features
    implementation("io.insert-koin:koin-core:$koin_version")
    // Koin Test features
    testImplementation("io.insert-koin:koin-test:$koin_version")
    // Koin for Ktor
    implementation("io.insert-koin:koin-ktor:$koin_version")
    // SLF4J Logger
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")


    //exposed db
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    //ktor
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")


    //my
    implementation("com.github.kursor1337:chronicles-of-ww2-kt-model:$model_version")
    implementation("com.github.kursor1337:chronicles-of-ww2-kt-dtos:$dtos_version")
    implementation(kotlin("stdlib-jdk8"))
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

tasks.shadowJar {
    archiveBaseName.set("chronicles-of-ww2")
    archiveClassifier.set("shadowJar")
    archiveVersion.set("beta-0.1")
}