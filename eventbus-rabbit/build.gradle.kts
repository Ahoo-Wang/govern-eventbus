dependencies {
    implementation(project(":eventbus-core"))
    api("org.springframework.amqp:spring-rabbit")
}

description = "eventbus-rabbit"
