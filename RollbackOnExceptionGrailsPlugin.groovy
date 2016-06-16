import org.codehaus.groovy.grails.commons.ServiceArtefactHandler
import org.codehaus.groovy.grails.orm.support.GroovyAwareNamedTransactionAttributeSource
import org.springframework.beans.factory.config.BeanDefinition

import hr.helix.transaction.RollbackAlwaysTransactionAttribute

class RollbackOnExceptionGrailsPlugin {

    def version = "0.2"
    def grailsVersion = "2.5 > *"

    def loadAfter = ['services']
    def observe = ['services']
    def watchedResources = [ 'file:./grails-app/services/*Service.groovy' ]

    def title = "Rollback on exception"
    def author = "Miro Bezjak"
    def authorEmail = "miro.bezjak@helix.hr"
    def description = 'Initiate rollback on any throwable when inside a transactional context.'
    def documentation = "https://github.com/mbezjak/rollback-on-exception"
    def scm = [ url: 'https://github.com/mbezjak/rollback-on-exception' ]

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
