plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

springBoot {
    buildInfo()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // DB
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    runtimeOnly("org.postgresql:postgresql")

    // Security + JWT
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.auth0:java-jwt:4.6.0")

    // Mail
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Utils
    implementation("commons-io:commons-io:2.22.0")
    implementation("commons-codec:commons-codec:1.22.0")
    implementation("org.apache.commons:commons-csv:1.14.1")
    implementation("com.github.slugify:slugify:4.0.1")
    implementation("org.jsoup:jsoup:1.22.2")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.5"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Lombok is already on the main sourceSet; tests need their own wiring.
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    filter {
        excludeTestsMatching("*IntegrationTest")
        isFailOnNoMatchingTests = false
    }
}

tasks.register<Test>("integrationTest") {
    useJUnitPlatform()
    // A fresh Test task doesn't inherit test sources — wire them explicitly.
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter {
        includeTestsMatching("*IntegrationTest")
        isFailOnNoMatchingTests = false
    }
    shouldRunAfter(tasks.named("test"))
}

tasks.named("check") {
    dependsOn(tasks.named("integrationTest"))
}

jacoco {
    // 0.8.13 = pierwsza wersja z pełnym wsparciem bytecode'u Javy 25.
    toolVersion = "0.8.13"
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"), tasks.named("integrationTest"))
    executionData.setFrom(fileTree(layout.buildDirectory).include("jacoco/*.exec"))
    reports {
        xml.required.set(true)   // dla CI / SonarQube
        html.required.set(true)  // lokalny podgląd: build/reports/jacoco/test/html/index.html
    }
}

// Kopiowanie frontendu do zasobów statycznych backendu
tasks.register<Copy>("copyFrontend") {
    val frontendProject = project(projects.frontend.path)
    dependsOn(frontendProject.tasks.named("build"))

    // Angular application builder emituje artefakty do dist/<project>/browser/
    // (index.html, favicon, chunki). Kopiujemy zawartość tego katalogu wprost do
    // static/, żeby Spring Boot serwował index.html pod "/".
    val frontendDist = frontendProject.file("dist/frontend/browser")

    from(frontendDist)
    // Szkielet CSR z builda SSR — nie ma czego szukać w statykach Spring Boota.
    exclude("index.csr.html")
    into(layout.buildDirectory.dir("resources/main/static"))

    doFirst {
        if (!frontendDist.exists() || !frontendDist.isDirectory) {
            throw GradleException("Missing frontend dist directory: $frontendDist. Did ':frontend:build' produce it?")
        }
        // Angular hashuje bundle (outputHashing: all) — bez sprzątnięcia katalogu
        // stare chunki z poprzednich buildów kumulowałyby się w bootJar.
        // processResources biegnie PO tym tasku, więc odtworzy własne statyki.
        delete(layout.buildDirectory.dir("resources/main/static"))
    }
}

tasks.named("processResources") {
    dependsOn(tasks.named("copyFrontend"))
}
