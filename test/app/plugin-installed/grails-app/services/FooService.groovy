import javax.sql.DataSource
import groovy.sql.Sql

class FooService {

    static transactional = true

    DataSource dataSource

    void execute() {
        new Foo(name: 'bar').save(failOnError: true, flush: true)
        new Foo(name: 'baz').save(failOnError: true, flush: true)

        new Sql(dataSource).execute """
            Some sql statement that causes exception. This one isn't valid sql
            so it always throws SQLException.
        """
    }

    void checked() {
        new Foo(name: 'qux').save(failOnError: true, flush: true)
        new Foo(name: 'quux').save(failOnError: true, flush: true)

        throw new CheckedException()
    }

    void unchecked() {
        new Foo(name: 'corge').save(failOnError: true, flush: true)
        new Foo(name: 'grault').save(failOnError: true, flush: true)

        throw new IllegalStateException()
    }

    void validation() {
        new Foo(name: 'garply').save(failOnError: true, flush: true)

        // name is not nullable
        new Foo().save(failOnError: true, flush: true)
    }

}
