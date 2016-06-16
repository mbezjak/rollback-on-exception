grails.project.work.dir = 'target'

grails.project.fork = [
   test    : [maxMemory: 1024, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
   console : [maxMemory: 1024, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver   = 'maven'
grails.project.dependency.resolution = {
    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }

    plugins {
        build(":release:3.1.1", ":rest-client-builder:2.1.1") {
            export = false
        }
    }
}
