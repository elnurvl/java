// Make the main build's version catalog available to buildSrc (it is not shared automatically).
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
