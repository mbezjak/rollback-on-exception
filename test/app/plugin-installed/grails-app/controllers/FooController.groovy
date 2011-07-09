import grails.util.GrailsUtil
import grails.converters.XML

class FooController {

    FooService fooService

    def validation = {
        invokeTransactionalMethod { fooService.validation() }
    }

    def unchecked = {
        invokeTransactionalMethod { fooService.unchecked() }
    }

    def checked = {
        invokeTransactionalMethod { fooService.checked() }
    }

    def execute = {
        invokeTransactionalMethod { fooService.execute() }
    }

    def unproxied = {
        invokeTransactionalMethod { fooService.unproxied() }
    }

    private void invokeTransactionalMethod(Closure work) {
        try {
            work()
        } catch (e) {
            GrailsUtil.deepSanitize(e).printStackTrace()
        }

        render Foo.getAll() as XML
    }

}
