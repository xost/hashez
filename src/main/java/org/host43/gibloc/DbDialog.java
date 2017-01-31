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
        "select max(id) from hashez_client where client=?"));
    pstmts.put("getFileSet",dbConn.prepareStatement(
        "select path,checksum,state from hashez_file where fileset_id=?")); //!!!
    pstmts.put("getFileId",dbConn.prepareStatement(
        "select id from hashez_file where fileset_id=? and path=?"));
    pstmts.put("lastEvent",dbConn.prepareStatement(
        "select max(id) from hashez_event where client_id=?"));
    pstmts.put("getFileSetId",dbConn.prepareStatement(
        "select max(id) from hashez_fileset where client_id=?"));
    pstmts.put("descr",dbConn.prepareStatement(
        "select descr from hashez_client where id=?"));
    pstmts.put("updateFileSet",dbConn.prepareStatement(
        "update hashez_file set checksum=?,state=?,happened=? where fileset_id=? and path=?")); //!!!
    pstmts.put("saveDiff",dbConn.prepareStatement(
        "insert into hashez_diff(event_id,fileset_id,path,state) values (?,?,?,?)"));//!!!
    pstmts.put("createCli",dbConn.prepareStatement(
        "insert into hashez_client(client,descr,registred) values(?,?,?)"));//!!!
    // два запроса.
    // 1. создать запить в таблице hashez_fileset
    // 2. добавить файлы в таблицу hashez_file
    pstmts.put("newFileSet",dbConn.prepareStatement(
        "insert into hashez_fileset(registred,client_id) values(?,?)")); //!!!
    pstmts.put("fillFileSet",dbConn.prepareStatement(
        "insert into hashez_file(path,fileset_id,checksum,state,updated,client_id) values(?,?,?,?,?,?)"));//!!!
    //
    pstmts.put("newEvent",dbConn.prepareStatement(
        "insert into hashez_event (client_id,eventType,comment,registred) values(?,?,?,?)"));
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

  List<File> getFileSet(int fileSetId) {
    List<File> fileSet=new ArrayList<>();
    PreparedStatement pstmt = pstmts.get("getFileSet");
    try {
      pstmt.setInt(1, fileSetId);
      if (pstmt.execute()) {
        ResultSet rs = pstmt.getResultSet();
        String filename;
        State state;
        byte[] digest;
        while (rs.next()) {
          filename = rs.getString("path");
          state = State.valueOf(rs.getString("state"));
          digest = rs.getBytes("checksum");
          fileSet.add(new File(filename, digest, state));
        }
        return fileSet;
      }
    }catch(SQLException ignored){}
    return fileSet;
  }

  List<File> updateFileSet(int clientId, int fsCount, List<File> fileSet){
    //Возвращаем список файлов которые не удалось обновить
    //update hashez_file set checksum=?,state=?,happened=? where client_id=? and path=? and fsCount=?
    List<File> failFiles=new ArrayList<>();
    PreparedStatement pstmt=pstmts.get("updateFileSet");
    try {
      pstmt.setInt(4, clientId);
      pstmt.setObject(3,(Object)atnow());
      pstmt.setInt(6,fsCount);
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

  void newFileSet(int clientId,int fsCount, List<File> fileSet) throws SQLException {
    //path,fsCount,checksum,state,updated,client_id
    PreparedStatement pstmt=pstmts.get("newFileSet");
    pstmt.setInt(2,fsCount);
    pstmt.setObject(5,(Object)atnow());
    pstmt.setInt(6,clientId);
    for(File file:fileSet){
      pstmt.setString(1,file.getFileName());
      Checksum chs=file.getChecksum();
      if(chs!=null)
        pstmt.setBytes(3,chs.getDigest());
      else
        pstmt.setBytes(3,null);
      pstmt.setString(4,file.getState().toString());
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

  int getFileSetId(int clientId) {
    PreparedStatement pstmt=pstmts.get("getFileSetId");
    int fsId=-1;
    try {
      pstmt.setInt(1, clientId);
      if (pstmt.execute()) {
        ResultSet rs = pstmt.getResultSet();
        if (rs.next())
          fsId = rs.getInt("max(id)");
      }
    }catch(SQLException ignored){}
    return fsId;
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
