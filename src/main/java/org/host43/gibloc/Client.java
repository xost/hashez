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

  Client(String client,DbDialog dbd) {
    clientName=client;
    clientId=dbd.getClientId(clientName);
    descr=dbd.getDescription(clientId);
    fileSet = dbd.getFileSet(clientId);
    fileSetId=dbd.getFileSetId(clientId);
    lastEvent = dbd.lastEvent(clientId);
  }

  static Client createClient(String cliName,String desr,List<File> fileSet,DbDialog dbd) throws SQLException {
    dbd.newCli(cliName,desr);
    int clientId=dbd.getClientId(cliName);
    int fileSetId=dbd.newFileSet(clientId,fileSet);
    return new Client(cliName,dbd);
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
    fsChanged=false;  //Проверить отличается ли набор файлов
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
    if(fsChanged) //Если отличается, то присвоить новый и очистить diffFiles
      this.fileSet=fileSet;
      this.diffFiles.clear();
  }

  List<File> getDiffFiles(){
    return diffFiles;
  }

  void saveFileSet(DbDialog dbd) {
    if(fsChanged){
      try {
        lastEvent = dbd.newEvent(clientId, eventType.NEWFILESET, null);
        fileSetId = dbd.newFileSet(clientId, fileSet);
      }catch(SQLException ignored){}
    }
  }

  void updateFileSet(DbDialog dbd) {
  }

  void saveDiff(DbDialog dbd) {
    if(checked) {
      try {
        lastEvent = dbd.newEvent(clientId, eventType.CHECK,null);
        dbd.saveDiff(lastEvent, diffFiles);
        //diffFiles.clear();
      } catch (SQLException ignored) {
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

}
