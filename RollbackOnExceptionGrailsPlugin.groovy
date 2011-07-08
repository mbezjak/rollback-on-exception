import org.codehaus.groovy.grails.commons.ServiceArtefactHandler
import org.codehaus.groovy.grails.orm.support.GroovyAwareNamedTransactionAttributeSource
import org.springframework.beans.factory.config.BeanDefinition

import hr.helix.transaction.RollbackAlwaysTransactionAttribute

class RollbackOnExceptionGrailsPlugin {

    def version = "0.1"
    def grailsVersion = "1.3.4 > *"
    def pluginExcludes = [
        'grails-app/domain/test/Account.groovy',
        'grails-app/services/test/AccountService.groovy'
    ]

    def loadAfter = ['services']
    def observe = ['services']
    def watchedResources = [ 'file:./grails-app/services/*Service.groovy' ]

    def author = "Miro Bezjak"
    def authorEmail = "miro.bezjak@helix.hr"
    def title = "Rollback on exception"
    def description = '''\\
Initiate rollback on any exception in a grails service marked as transactional.
'''
    def documentation = "https://github.com/mbezjak/rollback-on-exception"

    def doWithSpring = {
        beanDefinitions.each { name, definition ->
            makeServiceMethodsRollbackOnAnyThrowable definition
        }
    }

    def onChange = { event ->
        if (event.source && event.ctx) {
            def definition = fromChangeEventSource(source)
            makeServiceMethodsRollbackOnAnyThrowable definition
        }
    }

    private BeanDefinition fromChangeEventSource(source) {
        def type = ServiceArtefactHandler.TYPE
        def name = event.source.name

        def serviceClass = application.getArtefact(type, name)
        event.ctx.getBeanDefinition serviceClass.propertyName
    }

    private void makeServiceMethodsRollbackOnAnyThrowable(BeanDefinition definition) {
        if (definition.beanClassName ==
            'org.codehaus.groovy.grails.commons.spring.TypeSpecifyableTransactionProxyFactoryBean') {

            def methodMatchingMap = ['*': new RollbackAlwaysTransactionAttribute()]
            def source = new GroovyAwareNamedTransactionAttributeSource(nameMap: methodMatchingMap)

            definition.propertyValues.addPropertyValue('transactionAttributeSource', source)
        }
    }

}
