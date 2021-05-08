dependencies {
    constraints {
        rootProject.subprojects.forEach {
            if (it.name == "eventbus-demo") {
                return@forEach
            }
            api(it)
        }
    }
}
