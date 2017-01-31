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
  private int lastEvent=0;
  private int fileSetId=0;
  private boolean fsChanged=false;
  private boolean checked=false;

  Client(String client,DbDialog dbd) throws ClientNotFoundException {
    clientName=client;
    clientId=dbd.getClientId(clientName);
    descr=dbd.getDescription(clientId);
    fileSet = dbd.getFileSet(clientId);
    fileSetId=dbd.getFileSetId(clientId);
    lastEvent = dbd.lastEvent(clientId);
  }

  void recalculate() {
    List<File> diffFiles=new ArrayList<>();
    fileSet.forEach(file->{
      File old=file.calculate();
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

  void newFileSet(DbDialog dbd) throws SQLException {
    //неправильно
    if(fsChanged){
      lastEvent=dbd.newEvent(clientId,eventType.NEWFILESET);
      dbd.newFileSet(clientId,++fsId,fileSet);
    }
  }

  void updateFileSet(DbDialog dbd) throws SQLException {
  }

  void saveDiff(DbDialog dbd) throws SQLException {
    lastEvent=dbd.newEvent(clientId,eventType.CHECK);
    if(checked){
      dbd.saveDiff(lastEvent,diffFiles);
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

}
