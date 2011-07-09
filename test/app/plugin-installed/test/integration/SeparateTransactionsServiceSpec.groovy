import java.sql.SQLException

import grails.plugin.spock.*
import spock.lang.*

class SeparateTransactionsServiceSpec extends IntegrationSpec {

    // rely on transactional service
    static transactional = false

    SeparateTransactionsService separateTransactionsService

    def cleanup() {
        Foo.list()*.delete(flush: true)
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
