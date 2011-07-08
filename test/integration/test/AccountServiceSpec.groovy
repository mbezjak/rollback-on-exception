package test

import grails.plugin.spock.*
import spock.lang.*

class AccountServiceSpec extends UnitSpec {

    AccountService service = new AccountService()

    def "transfer should succeed if no exception occurs"() {
        given:
        newAccount(1, 100).save()
        newAccount(2, 100).save()

        when:
        service.transfer()

        then:
        Account.read(1).balance == 90
        Account.read(2).balance == 110
    }
    
    private Account newAccount(id, balance) {
        // new Account(id: id, balance: balance) doesn't work. id is never set!
        def account = new Account(balance: balance)
        account.id = id
        account
    }

}
