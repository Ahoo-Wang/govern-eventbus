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

rootProject.name = "govern-eventbus"
include(":eventbus-rabbit")
include(":eventbus-spring-boot-starter")
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
        gradlePluginPortal()
    }
    dependencies {
        classpath("me.champeau.jmh:jmh-gradle-plugin:0.6.8")
        classpath("io.github.gradle-nexus:publish-plugin:1.3.0")
        classpath("com.github.spotbugs.snom:spotbugs-gradle-plugin:5.0.12")
    }
}
