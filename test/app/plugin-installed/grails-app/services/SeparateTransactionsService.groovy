import java.sql.SQLException
import javax.sql.DataSource
import groovy.sql.Sql

class SeparateTransactionsService {

    static transactional = false

    DataSource dataSourceUnproxied

    void separate() {
        Foo.withTransaction {
            new Foo(name: 'plugh').save(failOnError: true, flush: true)
            new Foo(name: 'xyzzy').save(failOnError: true, flush: true)
        }

        def sql = new Sql(dataSourceUnproxied)
        def last = sql.firstRow('select max(id) as last from foo').last
        sql.withTransaction {
            sql.execute 'insert into foo values (?, ?, ?)', [last + 1, 0, 'thud']
            throw new SQLException('instead of sql.execute')
        }
    }

}
