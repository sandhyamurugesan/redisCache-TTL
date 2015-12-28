grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"


grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }
    dependencies {
       compile 'redis.clients:jedis:2.7.3'
        compile "org.springframework:spring-expression:$springVersion"
		compile 'org.springframework.data:spring-data-redis:1.6.1.RELEASE', {
			exclude group: 'org.springframework', name: 'spring-aop'
			exclude group: 'org.springframework', name: 'spring-context-support'
			exclude group: 'org.springframework', name: 'spring-context'
		}

    }

    plugins {
        build(":release:3.0.1",
              ":rest-client-builder:1.0.3") {
            export = false
        }
			  compile ':cache:1.1.8'
    }
}
