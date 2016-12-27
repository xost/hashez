package org.host43.gibloc;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    dbConn= DriverManager.getConnection("jdbc:mysql://jaba.gib.loc:3306/gibloc","admin","gibloc");
    stmt=dbConn.createStatement();
  }

  public List<File> getFileset(String client) throws SQLException, NoSuchAlgorithmException {
    List<File> fileSet=new ArrayList<>();
    int clientId=getClientId(client);
    ResultSet rs=stmt.executeQuery(
        "select item,checksum,state from hashez_file where client_id="+clientId);
    String filename;
    Checksum checksum;
    State state;
    byte[] digest;
    while(rs.next()){
      filename=rs.getString("item");
      state=State.valueOf(rs.getString("state"));
      digest=rs.getBytes("checksum");
      if(digest!=null)
        checksum=new Checksum(digest);
      else
        checksum=null;
      fileSet.add(new File(filename,digest,state));
    }
    return fileSet;
  }

  private int getClientId(String client) throws SQLException {
    ResultSet rs=stmt.executeQuery("select id from hashez_client where item=\""+client+"\"");
    int id=0;
    if(rs.next())
      id=rs.getInt("id");
    return id;
  }
}
