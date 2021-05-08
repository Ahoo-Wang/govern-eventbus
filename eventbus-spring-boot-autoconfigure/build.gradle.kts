java {
    registerFeature("rabbitBusSupport") {
        usingSourceSet(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
        capability(group.toString(), "rabbit-bus-support", version.toString())
    }
    registerFeature("kafkaBusSupport") {
        usingSourceSet(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
        capability(group.toString(), "kafka-bus-support", version.toString())
    }
    registerFeature("zookeeperLeaderSupport") {
        usingSourceSet(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
        capability(group.toString(), "zookeeper-leader-support", version.toString())
    }
}

dependencies {
    api(project(":eventbus-core"))
    "rabbitBusSupportImplementation"(project(":eventbus-rabbit"))
    "kafkaBusSupportImplementation"(project(":eventbus-kafka"))
    "zookeeperLeaderSupportImplementation"(project(":eventbus-zookeeper"))
    implementation(project(":eventbus-jdbc"))
    implementation(project(":eventbus-spring-context"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${rootProject.ext.get("springBootVersion")}")
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor:${rootProject.ext.get("springBootVersion")}")
}

description = "eventbus-spring-boot-autoconfigure"
