grails.project.dependency.resolution = {
    inherits 'global'
    log 'warn'

    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
    }

    plugins {
        test ':spock:0.5-groovy-1.7'

        compile ':hibernate:1.3.4'
        compile ':tomcat:1.3.4'
    }

}
