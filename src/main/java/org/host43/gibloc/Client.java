package org.host43.gibloc;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stas on 16.12.2016.
 */
class Client {

  private int clientId=-1;
  private String clientName;
  private String descr;
  private List<File> fileSet=null;
  private List<File> diffFiles=null;
  private int lastEvent;
  private boolean fsChanged=false;
  private boolean checked=false;

  Client(String client,DbDialog dbd) throws ClientNotFoundException, SQLException, NoSuchAlgorithmException {
    clientName=client;
    clientId=dbd.getClientId(clientName);
    descr=dbd.getDescription(clientId);
    fileSet = dbd.getFileSet(clientId);
    lastEvent = dbd.lastEvent(clientId);
  }

  void recalculate() {
    List<File> diffFiles=new ArrayList<>();
    fileSet.forEach(file->{
      File old=file.calculate2();
      if(old!=null)
        diffFiles.add(file);
    });
    this.diffFiles=diffFiles;
    checked=true;
  }

  List<File> getFileSet(){
    return fileSet;
  }

  void setFileSet(List<File> fileSet){
    fsChanged=false;
    this.fileSet.forEach(left->{
      boolean eq=false;
      for(File right:fileSet){
        if(left.theSame(right)){
          eq=true;
          break;
        }
      }
      fsChanged=eq;
    });
    if(fsChanged)
      this.fileSet=fileSet;
  }

  List<File> getDiffFiles(){
    return diffFiles;
  }

  private boolean compareWithStored(DbDialog dbd) throws SQLException, NoSuchAlgorithmException {
    List<File> stored=dbd.getFileSet(clientId);
    boolean isEquals=false;
    if(stored.size()==fileSet.size()){
      for(File left:fileSet){
        boolean f=false;
        for(File right:stored){
          if(left.theSame(right)){
            f=true;
            break;
          }
        }
        isEquals=f;
      }
    }else{
      isEquals=false;
    }
    return isEquals;
  }

  void update(DbDialog dbd) throws SQLException {
    if(fsChanged){
      try {
        clientId=dbd.newCli(clientName, descr);
        dbd.newFileSet(clientId,fileSet);
      }catch(SQLException e) {
        throw new SQLException(e);
      }
      lastEvent=dbd.newEvent(clientId,eventType.NEWCLIENT);
      lastEvent=dbd.newEvent(clientId,eventType.NEWFILESET);

      fsChanged=false;
      checked=false;
    }else{
      if(checked){
        dbd.updateFileSet(clientId,diffFiles);
      }
    }
  }

  private boolean checkIsOK(){
    boolean isOK=true;
    State state;
    for(File file:diffFiles){
      state=file.getState();
      if(state!=State.OK)
        isOK=false;
    }
    return isOK;
  }

  void create(String client,String descr,DbDialog dbd) throws ClientCreationException {
    try {
      clientId=dbd.newCli(client,descr);
      dbd.newFileSet(clientId,fileSet);
    } catch (SQLException e) {
      throw new ClientCreationException("Client \""+client+"\" creation error !!!");
    }
  }
}
