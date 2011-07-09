import grails.util.GrailsUtil
import grails.converters.XML

class FooController {

    FooService fooService

    def execute = {
        try {
            fooService.execute()
        } catch(e) {
            GrailsUtil.sanitize(e).printStackTrace()
        }

        render Foo.getAll() as XML
    }

}
