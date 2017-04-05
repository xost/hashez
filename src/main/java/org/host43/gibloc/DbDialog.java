package org.host43.gibloc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;

/**
 * Created by stas on 22.12.2016.
 */
class DbDialog {
  private static DbDialog instance;
  private Map<String, PreparedStatement> pstmts;

  private final Logger log=LogManager.getLogger(this.getClass());

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
    Properties connProps=new Properties();
    connProps.setProperty("user",username);
    connProps.setProperty("password",password);
    connProps.setProperty("useUnicode","true");
    connProps.setProperty("characterEncoding","utf8");
    Connection dbConn = DriverManager.getConnection(connection,connProps);
    pstmts = new Hashtable<>();
    pstmts.put("getClientId", dbConn.prepareStatement(
        "select max(id) from hashez_client where client=?"));
    pstmts.put("getFileSet", dbConn.prepareStatement(
        "select path,checksum,state from hashez_file where fileset_id=?"));
    pstmts.put("getFileId", dbConn.prepareStatement(
        "select id from hashez_file where fileset_id=? and path=?"));
    pstmts.put("lastEvent", dbConn.prepareStatement(
        "select max(id) from hashez_event where client_id=?"));
    pstmts.put("getFileSetId", dbConn.prepareStatement(
        "select max(id) from hashez_fileset where client_id=?"));
    pstmts.put("getBadFilesId", dbConn.prepareStatement(
        "select max(id) from hashez_badfiles where fileset_id=?"));
    pstmts.put("descr", dbConn.prepareStatement(
        "select descr from hashez_client where id=?"));
    pstmts.put("updateFileSet", dbConn.prepareStatement(
        "update hashez_file set checksum=?,state=?,updated=? where fileset_id=? and path=?"));
    pstmts.put("saveBad", dbConn.prepareStatement(
        "insert into hashez_badfiles(path,checksum,state,event_id) values (?,?,?,?)"));
    pstmts.put("createCli", dbConn.prepareStatement(
        "insert into hashez_client(client,descr,registred) values(?,?,?)"));
    // два запроса.
    // 1. создать запить в таблице hashez_fileset
    // 2. добавить файлы в таблицу hashez_file
    pstmts.put("newFileSet", dbConn.prepareStatement(
        "insert into hashez_fileset(registred,client_id) values(?,?)"));
    pstmts.put("fillFileSet", dbConn.prepareStatement(
        "insert into hashez_file(path,fileset_id,checksum,state,updated) values(?,?,?,?,?)"));
    //
    pstmts.put("newEvent", dbConn.prepareStatement(
        "insert into hashez_event (EventType,result,registred,client_id,fileset_id) values(?,?,?,?,?)"));
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

  Set<File> getFileSet(int fileSetId) {
    Set<File> fileSet = new HashSet<>();
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

  Set<File> updateFileSet(int clientId,int fileSetId, Set<File> fileSet) { //переписать
    //Возвращаем список файлов которые не удалось обновить
    //update hashez_file set checksum=?,state=?,updated=? where fileset_id=? and path=?
    Set<File> failFiles = new HashSet<>();
    PreparedStatement pstmt = pstmts.get("updateFileSet");
    try {
      pstmt.setObject(3, (Object) atNow());
      pstmt.setInt(4,fileSetId);
    } catch (SQLException e) {
      return fileSet;
    }
    State state;
    Checksum chs;
    for (File file : fileSet) {
      state = file.getState();
      chs=file.getChecksum();
      try {
        pstmt.setString(5,file.toString());
        if(chs!=null)
          pstmt.setBytes(1,chs.getDigest());
        else
          pstmt.setBytes(1,null);
        pstmt.setString(2, state.toString());
        pstmt.execute();
      } catch (SQLException e) {
        failFiles.add(file);
      }
    }
    return failFiles;
  }

  void saveBad(int eventId, Set<File> fileSet) throws SQLException {
    //insert into hashez_badfiles(path,checksum,state,event_id) values (?,?,?,?)
    PreparedStatement pstmt = pstmts.get("saveBad");
    pstmt.setInt(4, eventId);
    byte[] digest;
    for (File file : fileSet) {
      pstmt.setString(1, file.getFileName());
      digest = file.getChecksum().getDigest();
      if (digest == null) pstmt.setNull(2, Types.ARRAY);
      else pstmt.setBytes(2, digest);
      pstmt.setString(3, file.getState().toString());
      pstmt.execute();
    }
  }

  int newCli(String clientName, String descr) throws SQLException, ClientNotFoundException {
    PreparedStatement pstmt = pstmts.get("createCli");
    int clientId=-1;
    pstmt.setString(1, clientName);
    pstmt.setString(2, descr);
    pstmt.setObject(3, atNow());
    pstmt.execute();
    clientId=getClientId(clientName);
    return clientId;
  }

  int newFileSet(int clientId, Set<File> fileSet) throws SQLException {
    int fileSetId=-1;
    fileSetId = newFileSet(clientId);
    fillFileSet(fileSetId, fileSet);
    return fileSetId;
  }

  void newEvent(EventType type,Results result,Integer clientId,Integer fileSetId){
    //insert into hashez_event (EventType,result,registred,client_id,fileset_id,badfiles_id) values(?,?,?,?,?,?)
    PreparedStatement pstmt = pstmts.get("newEvent");
    try {
      pstmt.setString(1, type.toString());
      pstmt.setString(2, result.toString());
      pstmt.setObject(3, atNow());
      if(clientId==null)pstmt.setNull(4,Types.INTEGER);else pstmt.setInt(4,clientId);
      if(fileSetId==null)pstmt.setNull(5,Types.INTEGER);else pstmt.setInt(5,fileSetId);
      pstmt.execute();
    }catch(SQLException e){
      log.error("Can not insert new event."+e);
      throw new RuntimeException(e);
    }
  }

  int getClientId(String client) throws ClientNotFoundException {
    PreparedStatement pstmt = pstmts.get("getClientId");
    Object id=-1;
    try {
      pstmt.setString(1, client);
      if (pstmt.execute()) {
        ResultSet rs = pstmt.getResultSet();
        if (rs.next()) {
          id=rs.getObject("max(id)");
          if(id==null)
            throw new ClientNotFoundException("Client \""+client+"\" not found. Use command \"newCli\" for create new Client.");
        }
      }
    }catch(SQLException e){
      throw new ClientNotFoundException(e);
    }
    return (int)id;
  }

  private int getBadFilesId(int fileSetId) {
    PreparedStatement pstmt=pstmts.get("getBadFilesId");
    int badFilesId=-1;
    try {
      pstmt.setInt(1, fileSetId);
      if (pstmt.execute()) {
        ResultSet rs = pstmt.getResultSet();
        if(rs.next())
          badFilesId=rs.getInt("max(id)");
      }
    }catch(SQLException e){
      throw new RuntimeException(e);
    }
    return badFilesId;
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
    } catch (SQLException e) {
      throw new RuntimeException(e);
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
          id=rs.getInt("max(id)");
        return id;
      }
    } catch (SQLException ignored) {
    }
    return -1;
  }

  private Timestamp atNow() {
    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"));
    return new Timestamp(now.getTimeInMillis());
  }

  private int newFileSet(int clientId) throws SQLException {
    PreparedStatement pstmt = pstmts.get("newFileSet");
    int fileSetId=-1;
    pstmt.setObject(1,atNow());
    pstmt.setInt(2, clientId);
    pstmt.execute();
    fileSetId=getFileSetId(clientId);
    return fileSetId;
  }

  private void fillFileSet(int fileSetId, Set<File> fileSet) throws SQLException {
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
