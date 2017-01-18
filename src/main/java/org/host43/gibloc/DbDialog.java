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
    pstmts.put("getFileId",dbConn.prepareStatement(
        "select id from hashez_file where client_id=? and item=?"));
    pstmts.put("update",dbConn.prepareStatement(
        "update hashez_file set checksum=?,state=?,recalculate=? where client_id=? and item=?"));
    pstmts.put("saveDiff",dbConn.prepareStatement(
        "insert into hashez_diff(event_id,client_id,path,state) values (?,?,?,?)"));
    pstmts.put("createCli",dbConn.prepareStatement(
        "insert into hashez_client(client,descr,registration) values(?,?,?)"));
    pstmts.put("createFS",dbConn.prepareStatement(
        "insert into hashez_file(path,fsCount,checksum,state,updated,client_id) values(?,?,?,?,?,?)"));
    pstmts.put("clean",dbConn.prepareStatement(
        "delete from hashez_file where client_id=?"));
    pstmts.put("descr",dbConn.prepareStatement(
        "select descr from hashez_client where id=?"));
    pstmts.put("newEvent",dbConn.prepareStatement(
        "insert into hashez_event (client_id,eType,result,lasttime) values(?,?,?,?)"));
    pstmts.put("lastEvent",dbConn.prepareStatement(
        "select max(id) from hashez_event where client_id=?"));
    pstmts.put("getFSCount",dbConn.prepareStatement(
        "select max(fsCount) from hashez_file where client_id=?"));
  }

  String getDescription(int clientId){
    PreparedStatement pstmt=pstmts.get("descr");
    String descr="";
    try{
      pstmt.setInt(1,clientId);
      pstmt.execute();
      ResultSet rs=pstmt.getResultSet();
      while(rs.next()){
        descr=rs.getString("descr");
      }
    }catch(SQLException ignored){}
    return descr;
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

  List<File> updateFileSet(int clientId, List<File> fileSet){
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

  void saveDiff(int eventId,List<File> fileSet) throws SQLException {
    //insert into hashez_diff(event_id,path,state) values (?,?,?)
    PreparedStatement pstmt=pstmts.get("saveDiff");
    pstmt.setInt(1,eventId);
    for(File file:fileSet){
      pstmt.setString(2,file.getFileName());
      pstmt.setString(3,file.getState().toString());
      pstmt.execute();
    }
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
    int fsCount=getFSCount(clientId);
    //path,fsCount,checksum,state,updated,client_id
    fsCount++;
    PreparedStatement pstmt=pstmts.get("newFS");
    for(File file:fileSet){
      pstmt.setString(1,file.getFileName());
      pstmt.setInt(2,fsCount);
      Checksum chs=file.getChecksum();
      if(chs!=null)
        pstmt.setBytes(3,chs.getDigest());
      else
        pstmt.setBytes(3,null);
      pstmt.setString(4,file.getState().toString());
      pstmt.setObject(5,(Object)atnow());
      pstmt.setInt(6,clientId);
      pstmt.execute();
    }
  }

  int newEvent(int clientId,eventType type) throws SQLException {
    PreparedStatement pstmt=pstmts.get("newEvent");
    pstmt.setInt(1,clientId);
    pstmt.setString(2,type.toString());
    pstmt.setObject(4,(Object)atnow());
    pstmt.execute();
    return lastEvent(clientId);
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

  int getFSCount(int clientId) throws SQLException {
    PreparedStatement pstmt=pstmts.get("getFSCount");
    pstmt.setInt(1,clientId);
    int fsCount=-1;
    if(pstmt.execute()) {
      ResultSet rs=pstmt.getResultSet();
      if(rs.next())
        fsCount=rs.getInt("max(fsCount)");
    }
    return fsCount;
  }

  int lastEvent(int clientId) throws SQLException {
    PreparedStatement pstmt=pstmts.get("lastEvent");
    pstmt.setInt(1,clientId);
    if(pstmt.execute()){
      ResultSet rs=pstmt.getResultSet();
      int id=-1;
      if(rs.next())
        rs.getInt("max(id)");
      return id;
    }else
      return -1;
  }

  private Timestamp atnow(){
    Calendar now=Calendar.getInstance();
    return new Timestamp(now.getTimeInMillis());
  }
}
