import grails.util.GrailsUtil
import grails.converters.XML

class FooController {

    FooService fooService

    def execute = {
        try {
            fooService.execute()
        } catch(e) {
            GrailsUtil.deepSanitize(e).printStackTrace()
        }

        render Foo.getAll() as XML
    }

    def checked = {
        try {
            fooService.checked()
        } catch(e) {
            GrailsUtil.deepSanitize(e).printStackTrace()
        }

        render Foo.getAll() as XML
    }

}
