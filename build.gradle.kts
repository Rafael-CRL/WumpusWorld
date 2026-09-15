plugins {
    id("java")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val gdxVersion = "1.14.2"
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-freetype:$gdxVersion")
    runtimeOnly("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    runtimeOnly("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("wumpusworld.desktop.DesktopLauncher")
    applicationDefaultJvmArgs = if (System.getProperty("os.name").startsWith("Mac")) {
        listOf("-XstartOnFirstThread")
    } else {
        emptyList()
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.register<JavaExec>("runConsole") {
    group = "application"
    description = "Executa a simulação original no terminal."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("wumpusworld.Main")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("verifyDesktop") {
    group = "verification"
    description = "Abre uma janela para verificar os controles e capturar a interface."
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("wumpusworld.grafico.VerificacaoDesktop")
    jvmArgs(application.applicationDefaultJvmArgs)
}
