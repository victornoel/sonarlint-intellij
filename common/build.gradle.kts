val sonarlintCoreVersion: String by project
val intellijBuildVersion: String by project

intellij {
    version = intellijBuildVersion
}

dependencies {
    implementation("org.sonarsource.sonarlint.core:sonarlint-core:$sonarlintCoreVersion")
}
