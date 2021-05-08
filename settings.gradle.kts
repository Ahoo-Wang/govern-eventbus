rootProject.name = "govern-eventbus"
include(":eventbus-rabbit")
include(":eventbus-spring-boot-starter")
include(":eventbus-zookeeper")
include(":eventbus-spring-context")
include(":eventbus-kafka")
include(":eventbus-demo")
include(":eventbus-spring-boot-autoconfigure")
include(":eventbus-core")
include(":eventbus-jdbc")
include(":eventbus-bom")
include(":eventbus-dependencies")

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")
    }
}
