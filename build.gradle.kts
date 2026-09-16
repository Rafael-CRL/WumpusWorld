// ---------------------------------------------------------------------------
//  Mundo de Wumpus - Interface Grafica com libGDX
//  Universidade Federal do Para - Campus Cameta
//  Faculdade de Sistemas de Informacao - Computacao Grafica
// ---------------------------------------------------------------------------

plugins {
    id("java")
    id("application")
}

group = "br.ufpa.cameta.si"
version = "1.0"

/** Versao da biblioteca grafica utilizada no trabalho. */
val versaoLibGdx = "1.14.2"

repositories {
    mavenCentral()
}

dependencies {
    // Nucleo da libGDX: matematica, utilitarios, SpriteBatch e ShapeRenderer.
    implementation("com.badlogicgames.gdx:gdx:$versaoLibGdx")
    // Backend de desktop (janela, OpenGL e entrada) baseado em LWJGL 3.
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$versaoLibGdx")
    // Extensao FreeType: gera fontes vetoriais nitidas em qualquer tamanho.
    implementation("com.badlogicgames.gdx:gdx-freetype:$versaoLibGdx")

    // Bibliotecas nativas (.so / .dll / .dylib) para Windows, Linux e macOS.
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

// A pasta "assets" entra no classpath para que Gdx.files.internal() encontre
// as fontes tanto dentro da IDE quanto dentro do JAR distribuido.
sourceSets["main"].resources.srcDir("assets")

application {
    mainClass.set("wumpusworld.Main")
    // macOS exige que a janela da libGDX seja criada na primeira thread.
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
    // Garante acentuacao correta no modo console em qualquer sistema.
    jvmArgs("-Dfile.encoding=UTF-8")
    standardInput = System.`in`
}

/** Atalho para executar a versao em texto usada nas aulas 1 a 7. */
tasks.register<JavaExec>("console") {
    group = "application"
    description = "Executa a simulacao no modo console (sem janela grafica)."
    mainClass.set("wumpusworld.Main")
    classpath = sourceSets["main"].runtimeClasspath
    args("--console")
    jvmArgs("-Dfile.encoding=UTF-8")
    standardInput = System.`in`
}

/**
 * Gera um JAR unico, com todas as dependencias e bibliotecas nativas embutidas.
 * Basta executar:  java -jar build/libs/WumpusWorld-1.0-completo.jar
 */
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
