import java.sql.SQLException
import javax.sql.DataSource
import groovy.sql.Sql

import org.springframework.transaction.annotation.Transactional

class NotTransactionalService {

    static transactional = false

    DataSource dataSource
    DataSource dataSourceUnproxied

    void separate() {
        Foo.withTransaction {
            new Foo(name: 'plugh').save(failOnError: true, flush: true)
            new Foo(name: 'xyzzy').save(failOnError: true, flush: true)
        }

        def sql = new Sql(dataSourceUnproxied)
        def last = Foo.list()*.id.max()
        sql.withTransaction {
            sql.execute 'insert into foo values (?, ?, ?)', [last + 1, 0, 'thud']
            executeSqlThatCausesException sql
        }
    }

    void domainWithTransaction() {
        Foo.withTransaction {
            new Foo(name: 'plugh').save(failOnError: true, flush: true)
            new Foo(name: 'xyzzy').save(failOnError: true, flush: true)

            def sql = new Sql(dataSource)
            executeSqlThatCausesException sql
        }
    }

    @Transactional(rollbackFor = Throwable)
    void annotation() {
        new Foo(name: 'xyz').save(failOnError: true, flush: true)
        new Foo(name: 'zyx').save(failOnError: true, flush: true)

        def sql = new Sql(dataSource)
        executeSqlThatCausesException sql
    }

    @Transactional
    void annotationBare() {
        new Foo(name: 'xxx').save(failOnError: true, flush: true)
        new Foo(name: 'yyy').save(failOnError: true, flush: true)

        def sql = new Sql(dataSource)
        executeSqlThatCausesException sql
    }

    private void executeSqlThatCausesException(Sql sql) {
        sql.execute """
            Some sql statement that causes exception. This one isn't valid sql
            so it always throws SQLException.
        """
    }

}
