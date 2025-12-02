package dbs.oracle_lab_multimedia;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import oracle.jdbc.pool.OracleDataSource;

/** Hello world! */
public class App {

  public static void main(String[] args) throws Exception {
    // create a OracleDataSource instance
    OracleDataSource ods = new OracleDataSource();
    ods.setURL("jdbc:oracle:thin:@//gort.fit.vutbr.cz:1521/orclpdb");
    /**
     * * To set System properties, run the Java VM with the following at its command line: ...
     * -Dlogin=LOGIN_TO_ORACLE_DB -Dpassword=PASSWORD_TO_ORACLE_DB ... or set the project properties
     * (in NetBeans: File / Project Properties / Run / VM Options)
     */
    String LOGIN_TO_ORACLE_DB="xzhaome00";
    String PASSWORD_TO_ORACLE_DB="828PB9GH";
    ods.setUser(LOGIN_TO_ORACLE_DB);
    ods.setPassword(PASSWORD_TO_ORACLE_DB);
    /** */
    // connect to the database
    try (Connection conn = ods.getConnection()) {
      final List<Product> products = new ArrayList<>();
      // new products
      for (int i = 1; i <= 4; i++) {
        products.add(new Product(i, "car" + i));
      }
      // save to database
      for (Product product : products) {
        product.saveToDb(conn);
      }
      // set pictures
      for (Product product : products) {
        product.saveImageToDbFromFile(conn, "./car" + product.getCode() + ".gif");
      }
      // get pictures
      for (Product product : products) {
        product.loadImageFromDbToFile(conn, "./car" + product.getCode() + "-out.gif");
      }
      // similarity search
      final Product firstProduct = products.get(0);
      final Product similarProduct = firstProduct.findTheMostSimilar(conn, 0.3, 0.3, 0.1, 0.3);
      System.out.println(
          "The most similar to " + firstProduct.getCode() + " is " + similarProduct.getCode());
    }
  }
}
