grails.project.dependency.resolution = {
    inherits 'global'
    log 'warn'

    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
    }

    plugins {
        test ':hibernate:1.3.4'
        test ':spock:0.5-groovy-1.7'
    }

}
