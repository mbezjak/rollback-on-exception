import java.lang.reflect.UndeclaredThrowableException
import java.sql.SQLException

import grails.validation.ValidationException
import grails.plugin.spock.*
import spock.lang.*

class FooServiceSpec extends IntegrationSpec {

    // rely on transactional service
    static transactional = false

    FooService fooService

    def cleanup() {
        Foo.getAll()*.delete(flush: true)
    }

    def "execute should not rollback on SQLException"() {
        when:
        fooService.execute()

        then:
        def e = thrown(UndeclaredThrowableException)
        e.cause.getClass() == SQLException
        Foo.count() == 2
    }

    def "checked should not rollback on checked exception"() {
        when:
        fooService.checked()

        then:
        def e = thrown(UndeclaredThrowableException)
        e.cause.getClass() == CheckedException
        Foo.count() == 2
    }

    def "unchecked should rollback on unchecked exception"() {
        when:
        fooService.unchecked()

        then:
        thrown(IllegalStateException)
        Foo.count() == 0
    }

    def "validation should rollback on validation exception"() {
        when:
        fooService.validation()

        then:
        thrown(ValidationException)
        Foo.count() == 0
    }

}
