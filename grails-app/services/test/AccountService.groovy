package test

/**
 * Transactional service needed by tests. This service isn't included in
 * plugin distribution.
 * 
 * @author Miro Bezjak
 */
class AccountService {

    static transactional = true

    void transfer() {
        def sum = 10
        def source = Account.get(1)
        def destination = Account.get(2)

        source.balance -= 10
        destination.balance += 10

        source.save()
        destination.save()
    }

}
