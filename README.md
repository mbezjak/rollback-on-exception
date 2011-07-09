## Summary
Initiate rollback on any exception in a grails service marked as transactional.

## Rationale
When service class is marked as transactional, grails will use spring to setup
proxy around your service. Long story short, here is a quote for
`DefaultTransactionAttribute` from spring API documentation:

    Transaction attribute that takes the EJB approach to rolling back on
    runtime, but not checked, exceptions.

Couple of notes on this:

 1. Even though it's not mentioned in the documentation, errors
    (`java.lang.Error`) also rollback transaction.
 2. This behavior is only used for interceptors (needed to create proxy around
    transactional service). `TransactionTemplate`, for example, rolls back on
    any exception. Be that checked or unchecked exception or an error.

It might make sense in Java not to rollback on checked exceptions but it doesn't
in groovy. Groovy doesn't force you to catch checked exception. Herein lies a
problem. If any code throws checked (that includes `java.sql.SQLException`)
exception transaction does **not** rollback.

## Example
Suppose a grails service:

```groovy
import groovy.sql.Sql

class FooService {

    static transactional = true

    Sql sql

    def bar() {
        model1.save()
        model2.save()
        sql.call 'execute procedure executing_inside_transaction_procedure'
    }

}
```

This code will work as long as `sql.call` never fails. However it can fail due
to: connection problems, row locking, duplicate record, procedure itself raises
an exception or as simple as *someone-renamed-my-procedure* error. Whatever the
case it fails with `java.sql.SQLException`. That is a checked exception. No
transaction rollback will occur.

Simple
[google search](http://www.google.com/#q=grails+rollback+on+checked+exception)
suggests using `withTransaction` method:

```groovy
import groovy.sql.Sql

class FooService {

    static transactional = false

    Sql sql

    def bar() {
        Domain.withTransaction {
            model1.save()
            model2.save()
            sql.call 'execute procedure executing_inside_transaction_procedure'
        }
    }

}
```

That will work because `withTransaction` uses `TransactionTemplate` which rolls
back on any exception or error. But that solution is more of a hack then a real
solution. It's harder to unit test service written in this way. Moreover
**every** time service method can, under some circumstances, throw checked
exception it should use this form instead of simply placing `static
transactional = true` in service declaration.

Instead of relying on hacks this plugin attacks problem head-on. By configuring
spring to rollback on any exception (any `java.lang.Throwable` to be exact).

Here are two examples that will work as expected ones installing this plugin.

Example #1: `sql.executeInsert` can fail because of locking or duplicate record

```groovy
import groovy.sql.Sql

class FooService {

    static transactional = true

    Sql sql

    def bar() {
        model.save()
        sql.executeInsert 'insert into foo values (?, ?)', [1, 'bar']
    }

}
```

Example #2: Throwing checked exceptions

```groovy
import groovy.sql.Sql

class FooService {

    static transactional = true

    Sql sql

    def bar() {
        def from = // source account id
        def into = // destination account id

        sql.executeInsert 'update account set balance = balance + 100 where id = ?', [into]
        sql.executeInsert 'update account set balance = balance - 100 where id = ?', [from]

        def balance = sql.firstRow('select balance from account where id = ?', [from])
        if (balance < 0) {
            throw new InsufficientFundsException(from, balance)
        }
    }

}
```

## Install

    grails install-plugin rollback-on-exception

or add following line in *plugins* section in `grails-app/conf/BuildConfig.groovy`

    ':rollback-on-exception:0.1'

No additional configuration is required.

## Upgrade

    grails install-plugin rollback-on-exception

or update version in `grails-app/conf/BuildConfig.groovy`

## Uninstall

    grails uninstall-plugin rollback-on-exception

or remove line from `grails-app/conf/BuildConfig.groovy`

## Configuration
Currently plugin doesn't support any configuration.

## Impact on current code
Plugin shouldn't drastically impact your existing code. Only spring beans of
type
`org.codehaus.groovy.grails.commons.spring.TypeSpecifyableTransactionProxyFactoryBean`
are altered. This generally includes only transactional services (i.e. any
service **not** marked as `static transactional = false`). Remember that
services are transactional by default. Such services will now rollback on any
exception instead of committing transaction. This is a good thing.

There is one situation where installing this plugin can impact your code in a
bad way. Although it is a bad code from the start! Don't code like this. This
example is an anti pattern. You have been warned.

Suppose service is transactional. It has five methods. Four of which are normal
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

        // transaction (#2): execute insert and procedure
        def sql = new Sql(dataSourceUnproxied)
        sql.call 'execute procedure foo'
    }

}
```

Now, without this plugin installed this code actually works. Even though code is
convoluted there are actually two transactions here. Saving models uses
connection acquired by hibernate. Executing procedure uses `dataSourceUnproxied`
which returns a separate connection. Two connections -> two transactions. It
works. Life is good. You move on.

Until the day you install `rollback-on-exception`. Then it starts breaking. On
some occasions. When `sql.call` fails it throws `SQLException`. A checked
exception. With this plugin transaction #1 rolls back. It wouldn't otherwise
because that is spring's defaults. But correct way would be not to use
transactional service if you need separate transactions. Example of correctly
written example:

```groovy
import javax.sql.DataSource

class FooService {

    // use transactions manually
    static transactional = false

    DataSource dataSourceUnproxied

    def bar() {
        // transaction (#1): save models
        Domain.withTransaction {
            model1.save()
            model2.save()
        }

        // transaction (#2): execute procedure
        def sql = new Sql(dataSourceUnproxied)
        sql.call 'execute procedure foo'
    }

}
```

NOTE: Be aware that any use of `dataSourceUnproxied`, with or without this
plugin, requires it's own manual transactional management. Your code is
executing outside of `HibernateTransactionManager`. Therefore use
`sql.withTransaction` where necessary.

If you think of any similar example where this plugin would cause your program
to misbehave let me know so I can update documentation.

## Source code
Source code is available at github:
https://github.com/mbezjak/rollback-on-exception

## License
Plugin uses MIT license. Check LICENSE.txt file for more info.
