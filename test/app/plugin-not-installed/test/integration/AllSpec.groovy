import java.lang.reflect.UndeclaredThrowableException
import java.sql.SQLException

import grails.validation.ValidationException
import grails.plugin.spock.*
import spock.lang.*

class AllSpec extends IntegrationSpec {

    // rely on transactional service
    static transactional = false

    TransactionalService transactionalService
    NotTransactionalService notTransactionalService

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

    def "using dataSourceUnproxied in a transactional method is a bad idea"() {
        when:
        transactionalService.unproxied()

        then:
        def e = thrown(UndeclaredThrowableException)
        e.cause.getClass() == SQLException
        Foo.count() == 3
        Foo.list()[0].name == 'xyz'
        Foo.list()[1].name == 'zyx'
        Foo.list()[2].name == 'thud'
    }

    def "correct way of using dataSourceUnproxied in separate transaction"() {
        when:
        notTransactionalService.separate()

        then:
        def e = thrown(UndeclaredThrowableException)
        e.cause.getClass() == SQLException
        Foo.count() == 2
        Foo.list()[0].name == 'plugh'
        Foo.list()[1].name == 'xyzzy'
    }

    def "should rollback if transactional annotation defines rollbackFor"() {
        when:
        notTransactionalService.annotation()

        then:
        def e = thrown(UndeclaredThrowableException)
        e.cause.getClass() == SQLException
        Foo.count() == 0
    }

    def "should not rollback if exception doesnt match one in rollbackFor"() {
        when:
        notTransactionalService.annotationBare()

        then:
        def e = thrown(UndeclaredThrowableException)
        e.cause.getClass() == SQLException
        Foo.count() == 2
        Foo.list()[0].name == 'xxx'
        Foo.list()[1].name == 'yyy'
    }

}
