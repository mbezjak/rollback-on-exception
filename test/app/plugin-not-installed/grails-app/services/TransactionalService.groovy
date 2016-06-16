import javax.sql.DataSource
import groovy.sql.Sql

class TransactionalService {

    static transactional = true

    DataSource dataSource
    DataSource dataSourceUnproxied

    void validation() {
        new Foo(name: 'bar').save(failOnError: true, flush: true)

        // name is not nullable; fails with ValidationException
        new Foo().save(failOnError: true, flush: true)
    }

    void unchecked() {
        new Foo(name: 'baz').save(failOnError: true, flush: true)
        new Foo(name: 'qux').save(failOnError: true, flush: true)

        throw new IllegalStateException()
    }

    void checked() {
        new Foo(name: 'quux').save(failOnError: true, flush: true)
        new Foo(name: 'garply').save(failOnError: true, flush: true)

        throw new CheckedException()
    }

    void execute() {
        new Foo(name: 'corge').save(failOnError: true, flush: true)
        new Foo(name: 'grault').save(failOnError: true, flush: true)

        def sql = new Sql(dataSource)
        executeSqlThatCausesException sql
    }

    void unproxied() {
        new Foo(name: 'xyz').save(failOnError: true, flush: true)
        new Foo(name: 'zyx').save(failOnError: true, flush: true)

        def sql = new Sql(dataSourceUnproxied)
        def last = Foo.list()*.id.max()
        sql.execute 'insert into foo values (?, ?, ?)', [last + 1, 0, 'thud']
        executeSqlThatCausesException sql
    }

    private void executeSqlThatCausesException(Sql sql) {
        sql.execute """
            Some sql statement that causes exception. This one isn't valid sql
            so it always throws SQLException.
        """
    }

}
