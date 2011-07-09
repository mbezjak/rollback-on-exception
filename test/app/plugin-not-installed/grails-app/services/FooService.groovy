import javax.sql.DataSource
import groovy.sql.Sql

class FooService {

    static transactional = true

    DataSource dataSource

    void execute() {
        new Foo(name: 'bar').save()
        new Foo(name: 'baz').save()

        new Sql(dataSource).execute """
            Some sql statement that causes exception. This one isn't valid sql
            so it always throws SQLException.
        """
    }

}
