import grails.util.GrailsUtil
import grails.converters.XML

class FooController {

    FooService fooService

    def execute = {
        invokeTransactionalMethod { fooService.execute() }
    }

    def checked = {
        invokeTransactionalMethod { fooService.checked() }
    }

    def unchecked = {
        invokeTransactionalMethod { fooService.unchecked() }
    }

    def validation = {
        invokeTransactionalMethod { fooService.validation() }
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
