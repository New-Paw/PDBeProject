package dbs.oracle_project_template;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import oracle.jdbc.OracleDriver;
import oracle.jdbc.pool.OracleDataSource;

public class App {

  public static void main(String[] args) throws Exception {
    System.out.println("*** Oracle driver information ***");
    OracleDriver.main(args);
    System.out.println("*** Connecting to the Oracle db. and running a simple query ***");
    try {
      // create a OracleDataSource instance
      OracleDataSource ods = new OracleDataSource();
      ods.setURL("jdbc:oracle:thin:@//gort.fit.vutbr.cz:1521/orclpdb");
      /**
       * * To set System properties, run the Java VM with the following at its command line: ...
       * -Dlogin=LOGIN_TO_ORACLE_DB -Dpassword=PASSWORD_TO_ORACLE_DB ... or set the project
       * properties (in NetBeans: File / Project Properties / Run / VM Options)
       */
      ods.setUser(System.getProperty("login"));
      ods.setPassword(System.getProperty("password"));
      /** */
      // connect to the database
      try (Connection conn = ods.getConnection()) {
        // create a Statement
        try (Statement stmt = conn.createStatement()) {
          // select something from the system's dual table
          try (ResultSet rset = stmt.executeQuery("select 1+2 as col1, 3-4 as col2 from dual")) {
            // iterate through the result and print the values
            while (rset.next()) {
              System.out.println(
                  "col1: '" + rset.getString(1) + "'\tcol2: '" + rset.getString(2) + "'");
            }
          } // close the ResultSet
        } // close the Statement
      } // close the connection
    } catch (SQLException sqlEx) {
      System.err.println("SQLException: " + sqlEx.getMessage());
    }
  }
}
