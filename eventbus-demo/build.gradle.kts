/*
 * Copyright [2021-2021] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
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
    implementation(project(":eventbus-spring-boot-autoconfigure")) {
        capabilities {
            requireCapability("me.ahoo.eventbus:simba-jdbc-support")
        }
    }

//    implementation("org.springframework.cloud:spring-cloud-zookeeper-core")
    api("me.ahoo.cosid:cosid-jdbc")
    implementation("me.ahoo.cosid:cosid-spring-boot-starter")
    implementation("me.ahoo.cosid:cosid-shardingsphere")
    implementation("org.apache.shardingsphere:shardingsphere-jdbc-core-spring-boot-starter:${rootProject.ext.get("shardingsphereVersion")}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

description = "eventbus-demo"
