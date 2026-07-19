plugins {
    base
}

allprojects {
    group = "pl.dudios"
    version = "2.0.0"
}

// ---------- Root-level test shortcuts --------------------------------------
// Aliasy uruchamiające testy backendu bez pamiętania pełnej ścieżki `:backend:`
// ani flagi `-x :frontend:build`. Build frontendu jest automatycznie pomijany,
// gdy jednym z uruchamianych tasków jest alias — patrz `gradle.projectsEvaluated`.
//
//   ./gradlew unitTests          # tylko klasy *Test (bez *IntegrationTest)
//   ./gradlew integrationTests   # tylko klasy *IntegrationTest
//   ./gradlew allTests           # oba, po kolei (najpierw unit, potem integracyjne)

tasks.register("unitTests") {
    group = "verification"
    description = "Run backend unit tests (auto-skips frontend build)."
    dependsOn(":backend:test")
}

tasks.register("integrationTests") {
    group = "verification"
    description = "Run backend integration tests with Testcontainers (auto-skips frontend build)."
    dependsOn(":backend:integrationTest")
}

tasks.register("allTests") {
    group = "verification"
    description = "Run unit + integration tests (auto-skips frontend build). Units run first."
    dependsOn("unitTests", "integrationTests")
}

gradle.projectsEvaluated {
    val testAliases = setOf("unitTests", "integrationTests", "allTests")
    val launchedFromAlias = gradle.startParameter.taskNames.any(testAliases::contains)
    if (launchedFromAlias) {
        // Aliases imply "I only care about tests, don't build the frontend bundle."
        gradle.startParameter.excludedTaskNames.addAll(
            listOf(":frontend:build", ":backend:copyFrontend")
        )
    }
}
