import grails.util.GrailsUtil
import grails.converters.XML

class TestController {

    TransactionalService transactionalService
    SeparateTransactionsService separateTransactionsService

    def validation = {
        invokeTransactionalMethod { transactionalService.validation() }
    }

    def unchecked = {
        invokeTransactionalMethod { transactionalService.unchecked() }
    }

    def checked = {
        invokeTransactionalMethod { transactionalService.checked() }
    }

    def execute = {
        invokeTransactionalMethod { transactionalService.execute() }
    }

    def unproxied = {
        invokeTransactionalMethod { transactionalService.unproxied() }
    }

    def separate = {
        invokeTransactionalMethod { separateTransactionsService.separate() }
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
