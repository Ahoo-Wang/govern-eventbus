plugins {
    application
}

application {
    mainClass.set("me.ahoo.eventbus.demo.DemoApplication")
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation(platform(project(":eventbus-dependencies")))
    implementation("io.springfox:springfox-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    compileOnly("org.projectlombok:lombok:${rootProject.ext.get("lombokVersion")}")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${rootProject.ext.get("springBootVersion")}")
    annotationProcessor("org.projectlombok:lombok:${rootProject.ext.get("lombokVersion")}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("mysql:mysql-connector-java")

    implementation(project(":eventbus-spring-boot-starter"))
    implementation(project(":eventbus-spring-boot-autoconfigure")) {
        capabilities {
            requireCapability("me.ahoo.eventbus:rabbit-bus-support")
        }
    }
//    implementation(project(":eventbus-spring-boot-autoconfigure")) {
//        capabilities {
//            requireCapability("me.ahoo.eventbus:kafka-bus-support")
//        }
//    }
//    implementation(project(":eventbus-spring-boot-autoconfigure")) {
//        capabilities {
//            requireCapability("me.ahoo.eventbus:zookeeper-leader-support")
//        }
//    }

//    implementation("org.springframework.cloud:spring-cloud-zookeeper-core")


}

tasks.withType<Test> {
    useJUnitPlatform()
}

description = "eventbus-demo"
