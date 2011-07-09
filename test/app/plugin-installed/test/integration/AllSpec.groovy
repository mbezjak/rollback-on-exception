import java.lang.reflect.UndeclaredThrowableException
import java.sql.SQLException

import grails.validation.ValidationException
import grails.plugin.spock.*
import spock.lang.*

class AllSpec extends IntegrationSpec {

    // rely on transactional service
    static transactional = false

    FooService fooService
    SeparateTransactionsService separateTransactionsService

    def cleanup() {
        Foo.list()*.delete(flush: true)
    }

    def "should rollback on validation exception"() {
        when:
        fooService.validation()

        then:
        thrown(ValidationException)
        Foo.count() == 0
    }

    def "should rollback on unchecked exception"() {
        when:
        fooService.unchecked()

        then:
        thrown(IllegalStateException)
        Foo.count() == 0
    }

    def "should rollback on checked exception"() {
        when:
        fooService.checked()

        then:
        def e = thrown(UndeclaredThrowableException)
        e.cause.getClass() == CheckedException
        Foo.count() == 0
    }

    def "should rollback on SQLException"() {
        when:
        fooService.execute()

        then:
        def e = thrown(UndeclaredThrowableException)
        e.cause.getClass() == SQLException
        Foo.count() == 0
    }

    def "using dataSourceUnproxied in a transactional method is a bad idea"() {
        when:
        fooService.unproxied()

        then:
        def e = thrown(UndeclaredThrowableException)
        e.cause.getClass() == SQLException
        Foo.count() == 1
        Foo.list()[0].name == 'thud'
    }

    def "correct way of using dataSourceUnproxied in separate transaction"() {
        when:
        separateTransactionsService.separate()

        then:
        thrown(SQLException)
        Foo.count() == 2
        Foo.list()[0].name == 'plugh'
        Foo.list()[1].name == 'xyzzy'
    }

}