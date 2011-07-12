import java.lang.reflect.UndeclaredThrowableException
import java.sql.SQLException

import grails.validation.ValidationException
import grails.plugin.spock.*
import spock.lang.*

class AllSpec extends IntegrationSpec {

    // rely on transactional service
    static transactional = false

    TransactionalService transactionalService

    def cleanup() {
        Foo.list()*.delete(flush: true)
    }

    def "should rollback on validation exception"() {
        when:
        transactionalService.validation()

        then:
        thrown(ValidationException)
        Foo.count() == 0
    }

    def "should rollback on unchecked exception"() {
        when:
        transactionalService.unchecked()

        then:
        thrown(IllegalStateException)
        Foo.count() == 0
    }

    def "should not rollback on checked exception"() {
        when:
        transactionalService.checked()

        then:
        def e = thrown(UndeclaredThrowableException)
        e.cause.getClass() == CheckedException
        Foo.count() == 2
    }

    def "should not rollback on SQLException"() {
        when:
        transactionalService.execute()

        then:
        def e = thrown(UndeclaredThrowableException)
        e.cause.getClass() == SQLException
        Foo.count() == 2
    }

}
