grails.project.work.dir = 'target'

grails.servlet.version = "3.0"
grails.tomcat.nio      = true

grails.project.fork = [
   test    : [maxMemory: 1024, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
   run     : [maxMemory: 1024, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
   war     : [maxMemory: 1024, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
   console : [maxMemory: 1024, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver   = 'maven'
grails.project.dependency.resolution = {
    inherits 'global'
    log 'warn'
    checksums true

    repositories {
        grailsPlugins()
        grailsHome()
        mavenLocal()
        grailsCentral()
        mavenCentral()
    }

    dependencies {
        test "org.grails:grails-datastore-test-support:1.0.2-grails-2.4"
    }

    plugins {
        build ":tomcat:7.0.55.3" // or ":tomcat:8.0.22"
        runtime ":hibernate4:4.3.10" // or ":hibernate:3.6.10.18"
    }
}

