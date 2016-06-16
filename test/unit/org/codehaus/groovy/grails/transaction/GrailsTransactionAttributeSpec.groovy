package org.codehaus.groovy.grails.transaction

import java.sql.SQLException

import spock.lang.*

class GrailsTransactionAttributeSpec extends Specification {

    @Unroll("'#e' should suggest transaction rollback")
    def "rollbackOn should suggest rollback for any kind of throwable"() {
        given:
        def attribute = new GrailsTransactionAttribute()

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
