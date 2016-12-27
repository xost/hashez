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
    int clientId=getClientId(client);
    List<File> fileSet=new ArrayList<>();
    ResultSet rs=stmt.executeQuery(
        "select item,checksum,state from hashez_file where client_id="+clientId+"");
    while(rs.next()){
      String filename=rs.getNString("item");
      byte[] checksum =rs.getBytes("checksum");
      String stVal=rs.getString("state");
      State state=State.valueOf(stVal);
      fileSet.add(new File(filename,checksum,state));
    }
    return fileSet;
  }

  public void update(String client,List<File> fileSet) throws SQLException {
    if(fileSet.size()>0){
      int clientId=getClientId(client);
      String statement="update hashez_file set checksum=?,state=? where client_id="+clientId+" and item=?";
      PreparedStatement pstmt=dbConn.prepareStatement(statement);
      Checksum checksum=null;
      for(File f:fileSet) {
        checksum=f.getChecksum();
        if(checksum==null)
          pstmt.setBytes(1,null);
        else
          pstmt.setBytes(1,f.getChecksum().getDigest());
        pstmt.setString(2,f.getState().toString());
        pstmt.setString(3,f.toString());
        pstmt.execute();
      }
    }
  }

  private int getClientId(String client) throws SQLException {
    ResultSet rs=stmt.executeQuery("select id from hashez_client where item='"+client+"'");
    if(rs.next()) {
      return (int) rs.getInt("id");
    }
    return -1;
  }
}
