## Summary
Initiate rollback on any exception in a grails service marked as transactional.

## Terminology
Grails services are classes located in `grails-app/services`
directory. *Transactional* service includes any grails service **not** marked as
`static transactional = false` or services with `@Transactional` annotation at
class or method level. Remember that services are transactional by default.

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
`java.sql.SQLException`) transaction does **not** rollback. One commonly used
class that can throw checked exception is `groovy.sql.Sql`.

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

This code works as long as `sql.call` never throws
`java.sql.SQLException`. Valid reasons for throwing exception are: row or page
lock, duplicate record, procedure raising exception, connection problems,
replication or as simple as *someone-renamed-my-procedure* error. No matter what
the reason is, `java.sql.SQLException` is a checked exception. No transaction
rollback occurs under such circumstances.

Simple
[google search](http://www.google.com/#q=grails+rollback+on+checked+exception)
suggests using `withTransaction` method:

```groovy
import javax.sql.DataSource
import groovy.sql.Sql

class FooService {

    // using manual withTransaction block instead
    static transactional = false

    DataSource dataSource

    def bar() {
        Domain.withTransaction {
            model1.save()
            model2.save()

            def sql = new Sql(dataSource)
            sql.call 'execute procedure that_runs_as_part_of_transactional_block'
        }
    }

}
```

Suggestion works because `withTransaction` method uses `TransactionTemplate`
internally. `TransactionTemplate`, as stated before, rolls back on any exception
or error.

However such solution has a couple of downsides. (1) Declarative transaction
approach feels much clearer then explicit one. Having fewer lines and being
easier to read. (2) It's also harder to unit test services having
`withTransaction` block. (3) Moreover it's easier to forget to use suggested
approach. Leading to bugs that are harder to track down.

Another way would be to use `@Transactional` spring annotation:

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

This solves problem (1) and (2) but not (3).

*rollback-on-exception* plugin attacks problem head-on by configuring spring to
rollback on any exception or error (any `java.lang.Throwable`). Here are two
examples that work as expected once plugin is installed.

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
        super("$from account can't have negative balance but now has $balance")
    }

}
```

## Install

    grails install-plugin rollback-on-exception

or add following line in *plugins* section in `grails-app/conf/BuildConfig.groovy`

    // or use any other version number
    ':rollback-on-exception:0.1'

No additional configuration is required.

## Upgrade

    grails install-plugin rollback-on-exception

or update version in `grails-app/conf/BuildConfig.groovy`

## Uninstall

    grails uninstall-plugin rollback-on-exception

or remove plugin declaration from `grails-app/conf/BuildConfig.groovy`

## Configuration
Currently plugin doesn't support any configuration.

## Impact on current code
Plugin shouldn't drastically impact your existing code. Only spring beans of
type
`org.codehaus.groovy.grails.commons.spring.TypeSpecifyableTransactionProxyFactoryBean`
are altered. This generally only includes transactional services. Services are
configured to rollback on any throwable instead of committing
transaction. Service configuration happens at boot time - when web application
is starting.

There is one situation where installing *rollback-on-exception* plugin can
impact your code in a bad way. Although it is a bad code from the start! Don't
code like this. This example is an anti pattern. You have been warned.

Suppose service is transactional and has five methods. Four of which are normal
transactional methods. But one is quasi transactional. That is, it needs to
execute one peace as part of transaction #1 then other peace as part of
transaction #2. One might be tempted to use `dataSourceUnproxied` to achieve
this. Example code:

```groovy
import javax.sql.DataSource

class FooService {

    static transactional = true

    DataSource dataSourceUnproxied

    def bar() {
        // transaction (#1): save models
        model1.save()
        model2.save()

        // implicit transaction (#2): execute procedure
        def sql = new Sql(dataSourceUnproxied)
        // no explicit transaction block but it executes in it's own transaction
        sql.call 'execute procedure foo'
    }

}
```

Now, without this plugin installed this code actually works. Even though code is
convoluted there are actually two transactions here. Saving models uses
connection acquired by hibernate. Executing procedure uses `dataSourceUnproxied`
which returns a separate connection. Two connections -> two transactions. It
works. Life is good. You move on.

Until the day *rollback-on-exception* is installed. Then it starts breaking. On
some occasions. When `sql.call` fails it throws `SQLException`. A checked
exception. With this plugin transaction #1 rolls back. It wouldn't otherwise
because that is spring's defaults. But correct way would be not to use
transactional service if you need separate transactions. This is what correctly
written example looks like:

```groovy
import javax.sql.DataSource

class FooService {

    // use transactions programmatically instead of declaratively
    static transactional = false

    DataSource dataSourceUnproxied

    def bar() {
        // transaction (#1): save models
        Domain.withTransaction {
            model1.save()
            model2.save()
        }

        // implicit transaction (#2): execute procedure
        def sql = new Sql(dataSourceUnproxied)
        sql.call 'execute procedure foo'
    }

}
```

NOTE: Be aware that any use of `dataSourceUnproxied`, with or without this
plugin, requires programmatic transactional management. Declaring `static
transactional = true` doesn't work with `dataSourceUnproxied`. Acquired
connection is being used outside of `HibernateTransactionManager`. Therefore use
`sql.withTransaction` where necessary.

## Compatibility with @Transactional
Code that uses `@Transactional` annotations remains unchanged. Meaning that
`rollbackFor` attribute is respected even after installing
`rollback-on-exception`. For example, following code still rolls back only for
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

## Source code
Source code is available at github:
https://github.com/mbezjak/rollback-on-exception

## Roadmap and changelog
Roadmap and changelog are available at:
https://github.com/mbezjak/rollback-on-exception/issues

## License
Plugin uses MIT license. Check LICENSE.txt file for more info.
