dataSource {
    pooled = true
    driverClassName = 'org.h2.Driver'
    dbCreate = 'create-drop'
    url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
    username = 'sa'
    password = ''
}

