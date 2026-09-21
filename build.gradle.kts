plugins {
    id("java")
    id("application")
}

group = "br.ufpa.cameta.si"
version = "1.0"

val versaoLibGdx = "1.14.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.badlogicgames.gdx:gdx:$versaoLibGdx")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$versaoLibGdx")
    implementation("com.badlogicgames.gdx:gdx-freetype:$versaoLibGdx")

    runtimeOnly("com.badlogicgames.gdx:gdx-platform:$versaoLibGdx:natives-desktop")
    runtimeOnly("com.badlogicgames.gdx:gdx-freetype-platform:$versaoLibGdx:natives-desktop")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets["main"].resources.srcDir("assets")

application {
    mainClass.set("wumpusworld.Main")
    if (System.getProperty("os.name").startsWith("Mac")) {
        applicationDefaultJvmArgs = listOf("-XstartOnFirstThread")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

tasks.named<JavaExec>("run") {
    jvmArgs("-Dfile.encoding=UTF-8")
    standardInput = System.`in`
}

tasks.register<JavaExec>("console") {
    group = "application"
    description = "Executa a simulacao no modo console (sem janela grafica)."
    mainClass.set("wumpusworld.Main")
    classpath = sourceSets["main"].runtimeClasspath
    args("--console")
    jvmArgs("-Dfile.encoding=UTF-8")
    standardInput = System.`in`
}

tasks.register<Jar>("jarCompleto") {
    group = "build"
    description = "Gera um JAR executavel com todas as dependencias embutidas."
    archiveClassifier.set("completo")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes(
            "Main-Class" to "wumpusworld.Main",
            "Implementation-Title" to "Mundo de Wumpus",
            "Implementation-Version" to project.version.toString()
        )
    }

    from(sourceSets["main"].output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")
}

tasks.test {
    useJUnitPlatform()
    testLogging { events("passed", "failed", "skipped") }
}
