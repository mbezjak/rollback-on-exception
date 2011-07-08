package hr.helix.transaction

import java.sql.SQLException

import grails.plugin.spock.*
import spock.lang.*

class RollbackAlwaysTransactionAttributeSpec extends UnitSpec {

    @Unroll("'#e' should suggest transaction rollback")
    def "rollbackOn should suggest rollback for any kind of throwable"() {
        given:
        def attribute = new RollbackAlwaysTransactionAttribute()

        expect:
        attribute.rollbackOn e

        where:
        e << [
            new IllegalArgumentException(),
            new IllegalStateException(),
            new SQLException(),
            new ClassCastException(),
            new OutOfMemoryError(),
            new RuntimeException(),
            new Exception(),
            new Error(),
            new Throwable()
        ]
    }

}
