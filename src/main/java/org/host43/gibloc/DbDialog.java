package org.host43.gibloc;

import java.sql.*;

/**
 * Created by stas on 22.12.2016.
 */
public class DbDialog {
  private static DbDialog instance;
  private Connection dbConn;
  private Statement stmt;

  public static synchronized DbDialog getInstance() throws SQLException, ClassNotFoundException {
    if(instance==null)
      instance=new DbDialog();
    return instance;
  }

  private DbDialog() throws ClassNotFoundException, SQLException {
    Class.forName("com.mysql.jdbc.Driver");
    dbConn= DriverManager.getConnection("jdbc:mysql/jaba.gib.loc:3306/gibloc","hashez","hashezzz");
    stmt=dbConn.createStatement();
  }


}
