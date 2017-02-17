package org.host43.gibloc;

import java.sql.*;
import java.util.*;

/**
 * Created by stas on 22.12.2016.
 */
class DbDialog {
  private static DbDialog instance;
  private Map<String, PreparedStatement> pstmts;

  static synchronized DbDialog getInstance(String connection,
                                           String jdbcDriver,
                                           String username,
                                           String password){
    if (instance == null)
      try {
        instance = new DbDialog(connection,jdbcDriver,username,password);
      }catch(ClassNotFoundException | SQLException e){
        throw new RuntimeException(e);
      }
    return instance;
  }

  private DbDialog(String connection,
                   String jdbcDriver,
                   String username,
                   String password)
      throws SQLException, ClassNotFoundException {
    Class.forName(jdbcDriver);
    Connection dbConn = DriverManager.getConnection(connection,username, password);
    pstmts = new Hashtable<>();
    pstmts.put("getClientId", dbConn.prepareStatement(
        "select max(id) from hashez_client where client=?"));
    pstmts.put("getFileSet", dbConn.prepareStatement(
        "select path,checksum,state from hashez_file where fileset_id=?")); //!!!
    pstmts.put("getFileId", dbConn.prepareStatement(
        "select id from hashez_file where fileset_id=? and path=?"));
    pstmts.put("lastEvent", dbConn.prepareStatement(
        "select max(id) from hashez_event where client_id=?"));
    pstmts.put("getFileSetId", dbConn.prepareStatement(
        "select max(id) from hashez_fileset where client_id=?"));
    pstmts.put("descr", dbConn.prepareStatement(
        "select descr from hashez_client where id=?"));
    pstmts.put("updateFileSet", dbConn.prepareStatement(
        "update hashez_file set checksum=?,state=?,happened=? where fileset_id=? and path=?")); //!!!
    pstmts.put("saveDiff", dbConn.prepareStatement(
        "insert into hashez_diff(event_id,fileset_id,path,state) values (?,?,?,?)"));//!!!
    pstmts.put("createCli", dbConn.prepareStatement(
        "insert into hashez_client(client,descr,registred) values(?,?,?)"));//!!!
    // два запроса.
    // 1. создать запить в таблице hashez_fileset
    // 2. добавить файлы в таблицу hashez_file
    pstmts.put("newFileSet", dbConn.prepareStatement(
        "insert into hashez_fileset(registred,client_id) values(?,?)")); //!!!
    pstmts.put("fillFileSet", dbConn.prepareStatement(
        "insert into hashez_file(path,fileset_id,checksum,state,updated) values(?,?,?,?,?)"));//!!!
    //
    pstmts.put("newEvent", dbConn.prepareStatement(
        "insert into hashez_event (client_id,eventType,comment,registred) values(?,?,?,?)"));
  }

  String getDescription(int clientId) {
    PreparedStatement pstmt = pstmts.get("descr");
    String descr = "";
    try {
      pstmt.setInt(1, clientId);
      pstmt.execute();
      ResultSet rs = pstmt.getResultSet();
      while (rs.next()) {
        descr = rs.getString("descr");
      }
    } catch (SQLException ignored) {
    }
    return descr;
  }

  List<File> getFileSet(int fileSetId) {
    List<File> fileSet = new ArrayList<>();
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
    } catch (SQLException ignored) {
    }
    return fileSet;
  }

  List<File> updateFileSet(int fileSetId, List<File> fileSet) { //переписать
    //Возвращаем список файлов которые не удалось обновить
    //checksum=?,state=?,happened=? where fileset_id=? and path=?
    List<File> failFiles = new ArrayList<>();
    PreparedStatement pstmt = pstmts.get("updateFileSet");
    try {
      pstmt.setInt(4, fileSetId);
      pstmt.setObject(3, (Object) atNow());
    } catch (SQLException e) {
      return fileSet;
    }
    State state;
    Checksum chs;
    for (File file : fileSet) {
      state = file.getState();
      chs=file.getChecksum();
      try {
        if(chs!=null)
          pstmt.setBytes(1,chs.getDigest());
        else
          pstmt.setBytes(1,null);
        pstmt.setString(2, state.toString());
        pstmt.setString(5, file.toString());
        pstmt.execute();
      } catch (SQLException e) {
        failFiles.add(file);
      }
    }
    return failFiles;
  }

  void saveDiff(int eventId, List<File> fileSet) {
    //insert into hashez_diff(event_id,path,state) values (?,?,?)
    PreparedStatement pstmt = pstmts.get("saveDiff");
    try {
      pstmt.setInt(1, eventId);
      for (File file : fileSet) {
        pstmt.setString(2, file.getFileName());
        pstmt.setString(3, file.getState().toString());
        pstmt.execute();
      }
    }catch(SQLException e){
      throw new RuntimeException(e);
    }
  }

  int newCli(String clientName, String descr){
    PreparedStatement pstmt = pstmts.get("createCli");
    int clientId=-1;
    try {
      pstmt.setString(1, clientName);
      pstmt.setString(2, descr);
      pstmt.setObject(3, atNow());
      pstmt.execute();
      clientId=getClientId(clientName);
      newEvent(clientId,eventType.NEWCLIENT,"clientName \""+clientName+"\" created");
    }catch(SQLException e){
      throw new RuntimeException(e);
    }
    return clientId;
  }

  int newFileSet(int clientId, List<File> fileSet){
    int fileSetId=-1;
    try {
      fileSetId = newFileSet(clientId);
      fillFileSet(fileSetId, fileSet);
      newEvent(clientId, eventType.NEWFILESET, "New FileSet saved");
    }catch(SQLException e){
      throw new RuntimeException(e);
    }
    return fileSetId;
  }

  int newEvent(int clientId, eventType type,String comment) throws SQLException {
    PreparedStatement pstmt = pstmts.get("newEvent");
    pstmt.setInt(1, clientId);
    pstmt.setString(2, type.toString());
    pstmt.setString(3,comment);
    pstmt.setObject(4, (Object) atNow());
    pstmt.execute();
    return lastEvent(clientId);
  }

  int getClientId(String client) {
    PreparedStatement pstmt = pstmts.get("getClientId");
    int id=-1;
    try {
      pstmt.setString(1, client);
      if (pstmt.execute()) {
        ResultSet rs = pstmt.getResultSet();
        if (rs.next())
          id = rs.getInt("max(id)");
      }
    }catch(SQLException ignored){}
    return id;
  }

  int getFileSetId(int clientId) {
    PreparedStatement pstmt = pstmts.get("getFileSetId");
    int fsId = -1;
    try {
      pstmt.setInt(1, clientId);
      if (pstmt.execute()) {
        ResultSet rs = pstmt.getResultSet();
        if (rs.next())
          fsId = rs.getInt("max(id)");
      }
    } catch (SQLException ignored) {
    }
    return fsId;
  }

  int lastEvent(int clientId) {
    PreparedStatement pstmt = pstmts.get("lastEvent");
    try {
      pstmt.setInt(1, clientId);
      if (pstmt.execute()) {
        ResultSet rs = pstmt.getResultSet();
        int id = -1;
        if (rs.next())
          rs.getInt("max(id)");
        return id;
      }
    } catch (SQLException ignored) {
    }
    return -1;
  }

  private Timestamp atNow() {
    Calendar now = Calendar.getInstance();
    return new Timestamp(now.getTimeInMillis());
  }

  private int newFileSet(int clientId) throws SQLException {
    PreparedStatement pstmt = pstmts.get("newFileSet");
    int fileSetId=-1;
    pstmt.setObject(1, (Object) atNow());
    pstmt.setInt(2, clientId);
    pstmt.execute();
    fileSetId=getFileSetId(clientId);
    return fileSetId;
  }

  private void fillFileSet(int fileSetId, List<File> fileSet) throws SQLException {
    //path,fileset_id,checksum,state,updated
    PreparedStatement pstmt = pstmts.get("fillFileSet");
    Checksum chs;
    pstmt.setInt(2, fileSetId);
    pstmt.setObject(5, (Object) atNow());
    for (File file : fileSet) {
      pstmt.setString(1, file.getFileName());
      pstmt.setString(4, file.getState().toString());
      chs = file.getChecksum();
      if (chs != null) {
        pstmt.setBytes(3, chs.getDigest());
      } else {
        pstmt.setBytes(3, null);
      }
      pstmt.execute();
    }
  }
}
