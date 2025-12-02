# Bugs

## IMG-00703 for JPEG Images

JPEG images
[should be supported](https://docs.oracle.com/en/database/oracle/oracle-database/12.2/aivug/image-file-compression-formats-for-oracle-multimedia.html)
in Oracle Multimedia, however, they cause IMG-00703 now.

Any workaround or bug-fix is welcome.

```
java.sql.SQLException: ORA-29400: data cartridge error
IMG-00703: unable to read image data
ORA-06512: at "ORDSYS.ORDIMG_PKG", line 1136
ORA-06512: at "ORDSYS.ORDIMERRORCODES", line 75
ORA-06512: at "ORDSYS.ORDIMERRORCODES", line 65
ORA-06512: at "ORDSYS.ORDIMG_PKG", line 47
ORA-06512: at "ORDSYS.ORDIMG_PKG", line 1083
ORA-06512: at "ORDSYS.ORDIMAGE", line 222
ORA-06512: at line 1

    at oracle.jdbc.driver.T4CTTIoer11.processError(T4CTTIoer11.java:494)
    at oracle.jdbc.driver.T4CTTIoer11.processError(T4CTTIoer11.java:446)
    at oracle.jdbc.driver.T4C8Oall.processError(T4C8Oall.java:1052)
    at oracle.jdbc.driver.T4CTTIfun.receive(T4CTTIfun.java:537)
    at oracle.jdbc.driver.T4CTTIfun.doRPC(T4CTTIfun.java:255)
    at oracle.jdbc.driver.T4C8Oall.doOALL(T4C8Oall.java:610)
    at oracle.jdbc.driver.T4CCallableStatement.doOall8(T4CCallableStatement.java:249)
    at oracle.jdbc.driver.T4CCallableStatement.doOall8(T4CCallableStatement.java:82)
    at oracle.jdbc.driver.T4CCallableStatement.executeForRows(T4CCallableStatement.java:924)
    at oracle.jdbc.driver.OracleStatement.doExecuteWithTimeout(OracleStatement.java:1136)
    at oracle.jdbc.driver.OraclePreparedStatement.executeInternal(OraclePreparedStatement.java:3640)
    at oracle.jdbc.driver.T4CCallableStatement.executeInternal(T4CCallableStatement.java:1318)
    at oracle.jdbc.driver.OraclePreparedStatement.executeLargeUpdate(OraclePreparedStatement.java:3730)
    at oracle.jdbc.driver.OraclePreparedStatement.executeUpdate(OraclePreparedStatement.java:3710)
    at oracle.jdbc.driver.OracleCallableStatement.executeUpdate(OracleCallableStatement.java:4265)
    at oracle.jdbc.driver.OraclePreparedStatementWrapper.executeUpdate(OraclePreparedStatementWrapper.java:1061)
    at oracle.ord.im.OrdImageBase._setProperties(OrdImageBase.java:1251)
    at oracle.ord.im.OrdImage.setProperties(OrdImage.java:1118)
```
