plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.cyte"
version = "1.0"

application {
    mainModule.set("com.cyte.edamame")
    mainClass.set("com.cyte.edamame.EDAmame")
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    systemProperty("file.encoding", "UTF-8")
}

tasks.withType<Javadoc>{
    options.encoding = "UTF-8"
}

javafx {
    version = "17.0.2"
    modules("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.yaml:snakeyaml:1.30")
    implementation("com.h2database:h2:2.1.212")
    implementation("org.jetbrains:annotations:20.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

jlink {
    imageZip.set(project.file("${buildDir}/distributions/app-${javafx.platform.classifier}.zip"))
    addExtraDependencies("javafx")
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "EDAmame"
    }
}

tasks.jlinkZip {
    group = "distribution"
}