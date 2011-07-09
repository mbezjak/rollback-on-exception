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

Here are a couple of examples that will work as expected ones installing this
plugin.

Example #1: `sql.executeInsert` can fail because of locking or duplicate record

```groovy
import groovy.sql.Sql

class FooService {

    static transactional = true

    Sql sql

    def bar() {
        model.save()
        sql.executeInsert 'insert into foo values(?, ?)', [1, 'bar']
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

## Source code
Source code is available at github:
https://github.com/mbezjak/rollback-on-exception

## License
Plugin uses MIT license. Check LICENSE.txt file for more info.
