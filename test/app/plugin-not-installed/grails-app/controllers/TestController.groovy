import grails.util.GrailsUtil
import grails.converters.XML

class TestController {

    TransactionalService transactionalService

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

    private void invokeTransactionalMethod(Closure work) {
        try {
            work()
        } catch (e) {
            GrailsUtil.deepSanitize(e).printStackTrace()
        }

        render Foo.list() as XML
    }

}
