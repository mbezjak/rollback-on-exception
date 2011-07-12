import org.codehaus.groovy.grails.commons.ServiceArtefactHandler
import org.codehaus.groovy.grails.orm.support.GroovyAwareNamedTransactionAttributeSource
import org.springframework.beans.factory.config.BeanDefinition

import hr.helix.transaction.RollbackAlwaysTransactionAttribute

class RollbackOnExceptionGrailsPlugin {

    def version = "0.1"
    def grailsVersion = "1.3.2 > *"

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
            def serviceClass = application.getArtefact(ServiceArtefactHandler.TYPE, event.source.name)
            def definition = event.ctx.getBeanDefinition(serviceClass.propertyName)
            makeServiceMethodsRollbackOnAnyThrowable definition
        }
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
