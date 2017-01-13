package org.host43.gibloc;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by stas on 22.12.2016.
 */
class DbDialog {
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
    pstmts.put("getClientId",dbConn.prepareStatement(
        "select max(id) from hashez_client where item=?"));
    pstmts.put("getFileSet",dbConn.prepareStatement(
        "select item,checksum,state from hashez_file where client_id=?"));
    pstmts.put("update",dbConn.prepareStatement(
        "update hashez_file set checksum=?,state=?,recalculate=? where client_id=? and item=?"));
    pstmts.put("createCli",dbConn.prepareStatement(
        "insert into hashez_client(client,descr,registration) values(?,?,?)"));
    pstmts.put("createFS",dbConn.prepareStatement(
        "insert into hashez_file(item,checksum,state,recalculate,client_id) values(?,?,?,?,?)"));
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
  List<File> update(int clientId, List<File> fileSet){
    //Возвращаем список файлов которые не удалось обновить
    List<File> failFiles=new ArrayList<>();
    PreparedStatement pstmt=pstmts.get("update");
    try {
      pstmt.setInt(4, clientId);
      pstmt.setObject(3,(Object)atnow());
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
        pstmt.setString(5,file.toString());
        pstmt.execute();
      }catch(SQLException e) {
        failFiles.add(file);
      }
    }
    return failFiles;
  }

  int newCli(String clientName,String descr) throws SQLException {
    PreparedStatement pstmt=pstmts.get("createCli");
    pstmt.setString(1,clientName);
    pstmt.setString(2,descr);
    pstmt.setObject(3,(Object)atnow());
    pstmt.execute();
    return getClientId(clientName);
  }

  void newFileSet(int clientId, List<File> fileSet) throws SQLException {
    PreparedStatement pstmt=pstmts.get("createFS");
    pstmt.setObject(4,(Object)atnow());
    pstmt.setInt(5,clientId);
    Checksum chS=null;
    for(File file:fileSet){
      pstmt.setString(1,file.toString());
      chS=file.getChecksum();
      if(chS!=null)
        pstmt.setBytes(2,chS.getDigest());
      else
        pstmt.setBytes(2,null);
      pstmt.setString(3,file.getState().toString());
      pstmt.execute();
    }
  }

  private Timestamp atnow(){
    Calendar now=Calendar.getInstance();
    return new Timestamp(now.getTimeInMillis());
  }

  int getClientId(String client) throws SQLException {
    PreparedStatement pstmt=pstmts.get("getClientId");
    pstmt.setString(1,client);
    if(pstmt.execute()) {
      ResultSet rs = pstmt.getResultSet();
      int id = -1;
      if (rs.next())
        id = rs.getInt("max(id)");
      return id;
    }else return -1;
  }
}
