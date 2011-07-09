import java.lang.reflect.UndeclaredThrowableException
import java.sql.SQLException

import grails.plugin.spock.*
import spock.lang.*

class FooServiceSpec extends IntegrationSpec {

    // rely on transactional service
    static transactional = false

    FooService fooService

    def "execute should not rollback on SQLException"() {
        when:
        fooService.execute()

        then:
        def e = thrown(UndeclaredThrowableException)
        e.cause.getClass() == SQLException
        Foo.count() == 2
    }

}
