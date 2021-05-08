dependencies {
    implementation(project(":eventbus-core"))
    implementation("org.springframework:spring-context")
    api("org.apache.curator:curator-recipes")
}

description = "eventbus-zookeeper"
