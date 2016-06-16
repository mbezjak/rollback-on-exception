## Summary
Initiate rollback on any throwable when inside a transactional context.

## Terminology
Grails service (or simply service) is a Groovy class located in
`grails-app/services` directory. *Transactional* service is any service **not**
marked as `static transactional = false` or any service with `@Transactional`
annotation at class or method level. Remember that grails services are
transactional by default.

## Rationale
When service class is marked as transactional, grails uses spring to setup proxy
around a service. Long story short, here is a quote for
`DefaultTransactionAttribute` from spring API documentation:

    Transaction attribute that takes the EJB approach to
    rolling back on runtime, but not checked, exceptions.

Things worth mentioning regarding spring's approach:

 1. API documentation doesn't mention it but errors (`java.lang.Error`) also
    rollback transaction.
 2. This behavior is only used for interceptors. They are needed to create proxy
    around transactional service. At least one other class rolls back on any
    exception - be that checked, unchecked exception or an error. One such
    example is `TransactionTemplate` class.

It might make sense not to rollback on checked exceptions in Java but it doesn't
in Groovy. Groovy doesn't force you to catch checked exceptions. Herein lies a
problem. If any code throws checked exception (which includes
`java.sql.SQLException`), transaction does **not** rollback. One commonly used
class that can throw checked exception is `groovy.sql.Sql`.

See further discussion on the matter at grails-user mail list post titled
[Rolling back transactions on checked exceptions](http://grails.1312388.n4.nabble.com/Rolling-back-transactions-on-checked-exceptions-td4634494.html).

## Example
Suppose a grails service:

```groovy
import javax.sql.DataSource
import groovy.sql.Sql

class FooService {

    static transactional = true

    DataSource dataSource

    def bar() {
        model1.save()
        model2.save()

        def sql = new Sql(dataSource)
        sql.call 'execute procedure that_runs_as_part_of_transactional_block'
    }

}
```

The code works for as long as `sql.call` never throws `java.sql.SQLException`.
Valid reasons for throwing exception are: row or page lock, duplicate record,
procedure raising exception, connection problems, replication or as simple as
*someone-renamed-my-procedure* error. No matter what the reason is,
`java.sql.SQLException` is checked exception. No transaction rollback occurs
under such circumstance.

One solution could be to use `@Transactional` spring annotation:

```groovy
import javax.sql.DataSource
import groovy.sql.Sql

import org.springframework.transaction.annotation.Transactional

class FooService {

    // using @Transactional annotation instead
    static transactional = false

    DataSource dataSource

    @Transactional(rollbackFor = Throwable)
    def bar() {
        model1.save()
        model2.save()

        def sql = new Sql(dataSource)
        sql.call 'execute procedure that_runs_as_part_of_transactional_block'
    }

}
```

This solution has a big problem though. It's easy to forget to use suggested
approach; leading to bugs that very hard to track down. You might notice it in
production - when it's far too late (I did).

*rollback-on-exception* plugin attacks problem head-on by configuring spring to
rollback on any exception or error (any `java.lang.Throwable`). Here are two
examples that work as expected once the plugin is installed.

Example #1: `sql.executeInsert` can fail because of locks or duplicate record

```groovy
import javax.sql.DataSource
import groovy.sql.Sql

class FooService {

    static transactional = true

    DataSource dataSource

    def bar() {
        model.save()

        def sql = new Sql(dataSource)
        sql.executeInsert 'insert into foo values (?, ?)', [1, 'bar']
    }

}
```

Example #2: Throwing checked exceptions

```groovy
import javax.sql.DataSource
import groovy.sql.Sql

class FooService {

    static transactional = true

    DataSource dataSource

    def bar() {
        def from = // source account id
        def into = // destination account id

        def sql = new Sql(dataSource)
        sql.executeInsert 'update account set balance = balance + 100 where id = ?', [into]
        sql.executeInsert 'update account set balance = balance - 100 where id = ?', [from]

        def balance = sql.firstRow('select balance from account where id = ?', [from])
        if (balance < 0) {
            throw new InsufficientFundsException(from, balance)
        }
    }

}

class InsufficientFundsException extends Exception {

    InsufficientFundsException(from, balance) {
        super("$from account can't have negative balance but would have $balance")
    }

}
```

## Install

* for grails 1.x use version `0.1`
* for grails 2.x use version `0.2`
* there is currently no support for grails 3.x

Add to `grails-app/conf/BuildConfig.groovy`:

    plugins {
        runtime ':rollback-on-exception:0.1'
    }

No additional configuration is required.

## Uninstall
Remove `rollback-on-exception` line in `grails-app/conf/BuildConfig.groovy`.

## Configuration
Currently, plugin doesn't support any configuration.

## Impact on Current Code
Plugin shouldn't drastically impact existing code. Only spring beans of type
`org.codehaus.groovy.grails.commons.spring.TypeSpecifyableTransactionProxyFactoryBean`
are altered. This generally only includes transactional services. Services are
configured to rollback on any throwable instead of committing transaction.
Service configuration happens at boot time - when web application is starting.

Additionally `GrailsTransactionAttribute` is overriden to make sure
`Domain.withTransaction` behaves as expected.

NOTE: Be aware that any use of `dataSourceUnproxied`, with or without this
plugin, requires programmatic transactional management. Declaring `static
transactional = true` doesn't work with `dataSourceUnproxied`. Acquired
connection is being used outside of `HibernateTransactionManager`. Therefore,
use `sql.withTransaction` where necessary.

## Compatibility with @Transactional
Code that uses `@Transactional` annotations remains unchanged. Meaning of
`rollbackFor` attribute is respected even after installing
*rollback-on-exception*. For example, following code still rolls back only for
`RuntimeException`, `Error` and `MyException`.

```groovy
import javax.sql.DataSource
import groovy.sql.Sql

import org.springframework.transaction.annotation.Transactional

class FooService {

    // using @Transactional annotation instead
    static transactional = false

    DataSource dataSource

    @Transactional(rollbackFor = [RuntimeException, Error, MyException])
    def bar() {
        model1.save()
        model2.save()

        def sql = new Sql(dataSource)
        sql.call 'execute procedure that_runs_as_part_of_transactional_block'
    }

}
```

## Further Resources

 * Homepage:   https://github.com/mbezjak/rollback-on-exception
 * Issues:     https://github.com/mbezjak/rollback-on-exception/issues
 * Changelog:  see Changelog.md file
 * Roadmap:    see Roadmap.md file
 * License:    MIT (see LICENSE.txt file)
