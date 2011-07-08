package test

/**
 * Domain needed by tests. This domain isn't included in plugin distribution.
 * 
 * @author Miro Bezjak
 */
class Account {

    Integer id
    Integer balance

    static mapping = {
        table 'account'
        id generator: 'assigned'
        version false
    }

}
