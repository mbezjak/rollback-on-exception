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

## Source code
Source code is available at github:
https://github.com/mbezjak/rollback-on-exception

## License
Plugin uses MIT license. Check LICENSE.txt file for more info.
