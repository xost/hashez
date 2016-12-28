package org.host43.gibloc;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stas on 22.12.2016.
 */
public class DbDialog {
  private static DbDialog instance;
  Map<String,PreparedStatement> pstmts;

  static synchronized DbDialog getInstance()
      throws SQLException, ClassNotFoundException{
    if(instance==null)
      instance=new DbDialog();
    return instance;
  }

  private DbDialog()
      throws ClassNotFoundException, SQLException{
    Class.forName("com.mysql.jdbc.Driver");
    Connection dbConn= DriverManager.getConnection("jdbc:mysql://jaba.gib.loc:3306/gibloc","admin","gibloc");
    pstmts=new HashMap<>();
    pstmts.put("getClientId",dbConn.prepareStatement("select id from hashez_client where item=?"));
    pstmts.put("getFileSet",dbConn.prepareStatement("select item,checksum,state from hashez_file where client_id=?"));
    pstmts.put("update",dbConn.prepareStatement("update hashez_file set checksum=?,state=? where client_id=? and item=?"));
  }

  List<File> getFileSet(int clientId) throws SQLException, NoSuchAlgorithmException {
    List<File> fileSet=new ArrayList<>();
    PreparedStatement pstmt = pstmts.get("getFileSet");
    pstmt.setInt(1,clientId);
    if (pstmt.execute()) {
      ResultSet rs = pstmt.getResultSet();
      String filename;
      State state;
      byte[] digest;
      while (rs.next()) {
        filename = rs.getString("item");
        state = State.valueOf(rs.getString("state"));
        digest = rs.getBytes("checksum");
        fileSet.add(new File(filename, digest, state));
      }
      return fileSet;
    }
    return null;
  }
  //Возвращаем список файлов которые не удалось обновить
  List<File> update(int clientId, List<File> fileSet) {
    List<File> failFiles=new ArrayList<>();
    PreparedStatement pstmt=pstmts.get("update");
    try {
      pstmt.setInt(3, clientId);
    }catch(SQLException e){
      return fileSet;
    }
    State state;
    byte[] digest;
    for(File file:fileSet){
      state=file.getState();
      if (state==State.UPDATED || state==State.OK)
        digest=file.getChecksum().getDigest();
      else
        digest=null;
      try {
        pstmt.setString(2, state.toString());
        pstmt.setBytes(1, digest);
        pstmt.setString(4,file.toString());
        pstmt.execute();
      }catch(SQLException e) {
        failFiles.add(file);
      }
    }
    return failFiles;
  }

  int getClientId(String client) throws SQLException {
    PreparedStatement pstmt=pstmts.get("getClientId");
    pstmt.setString(1,client);
    if(pstmt.execute()) {
      ResultSet rs = pstmt.getResultSet();
      int id = 0;
      if (rs.next())
        id = rs.getInt("id");
      return id;
    }else return -1;
  }
}
