import grails.util.GrailsUtil
import grails.converters.XML

class TestController {

    TransactionalService transactionalService
    NotTransactionalService notTransactionalService

    def validation() {
        invokeTransactionalMethod { transactionalService.validation() }
    }

    def unchecked() {
        invokeTransactionalMethod { transactionalService.unchecked() }
    }

    def checked() {
        invokeTransactionalMethod { transactionalService.checked() }
    }

    def execute() {
        invokeTransactionalMethod { transactionalService.execute() }
    }

    def unproxied() {
        invokeTransactionalMethod { transactionalService.unproxied() }
    }

    def separate() {
        invokeTransactionalMethod { notTransactionalService.separate() }
    }

    def domainWithTransaction() {
        invokeTransactionalMethod { notTransactionalService.domainWithTransaction() }
    }

    def annotation() {
        invokeTransactionalMethod { notTransactionalService.annotation() }
    }

    def annotationBare() {
        invokeTransactionalMethod { notTransactionalService.annotationBare() }
    }

    private void invokeTransactionalMethod(Closure work) {
        try {
            work()
        } catch (e) {
            GrailsUtil.deepSanitize(e).printStackTrace()
        }

        render Foo.list() as XML
    }

}
