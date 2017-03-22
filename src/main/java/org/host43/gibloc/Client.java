package org.host43.gibloc;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by stas on 16.12.2016.
 */
class Client {

  DbDialog dbd;
  private int clientId=-1;
  private String clientName;
  private String descr;
  private Set<File> fileSet=new HashSet<>();
  private Set<File> badFiles=new HashSet<>();
  private int lastEvent=0;
  private int fileSetId=0;
  private boolean fsChanged=false;
  private boolean needUpdate=false;

  Client(String client,DbDialog dbd) throws ClientNotFoundException {
    this.dbd=dbd;
    clientName=client;
    clientId=dbd.getClientId(clientName);
    if(clientId==-1)
      throw new ClientNotFoundException("Client \""+clientName+"\" not found.");
    descr=dbd.getDescription(clientId);
    fileSetId=dbd.getFileSetId(clientId);
    fileSet = dbd.getFileSet(fileSetId);
    lastEvent = dbd.lastEvent(clientId);
  }

  static Client createClient(String cliName,String desr,Set<File> fileSet,DbDialog dbd) throws ClientNotFoundException {
    try {
      int clientId = dbd.newCli(cliName, desr);
      dbd.newEvent(EventType.NEWCLIENT,Results.PASS,clientId,null,null);
      try {
        dbd.newFileSet(clientId, fileSet);
      }catch(SQLException e){
        dbd.newEvent(EventType.NEWFILESET,Results.FAIL,clientId,null,null);
      }
    }catch(SQLException e){
      dbd.newEvent(EventType.NEWCLIENT,Results.FAIL,null,null,null);
      return null;
    }
    return new Client(cliName,dbd);
  }

  void recalculate() {
    Set<File> badFiles=new HashSet<>();
    fileSet.forEach(file->{
      File old=file.calculate();
      if(old!=null)
        badFiles.add(file);
    });
    this.badFiles=badFiles;
    if(!badFiles.isEmpty()) {
      needUpdate=true;
      int badFilesId = dbd.saveBad(lastEvent, fileSetId, badFiles);
      dbd.newEvent(EventType.CHECK, Results.FAIL, clientId, fileSetId, badFilesId);
    }else
      dbd.newEvent(EventType.CHECK,Results.PASS,clientId,fileSetId,null);
  }

  Set<File> getFileSet(){
    return fileSet;
  }

  void setFileSet(Set<File> fileSet){
    fsChanged=false;  //Проверить отличается ли набор файлов
    if(fileSet.size()==this.fileSet.size()) {
      this.fileSet.forEach(left -> {
        boolean eq = false;
        for (File right : fileSet) {
          if (left.theSame(right)) {
            eq = true;
            break;
          }
        }
        fsChanged = !eq;
      });
    }else
      fsChanged=true;
    if(fsChanged) { //Если отличается, то присвоить новый и очистить badFiles
      this.fileSet = fileSet;
    }
  }

  Set<File> getBadFiles(){
    return badFiles;
  }

  void saveFileSet() {
    if(fsChanged){
      try {
        fileSetId = dbd.newFileSet(clientId, fileSet);
        dbd.newEvent(EventType.NEWFILESET,Results.PASS,clientId,fileSetId,null);
      } catch (SQLException e) {
        dbd.newEvent(EventType.NEWFILESET,Results.FAIL,clientId,null,null);
        throw new RuntimeException(e);
      }
    }
  }

  Set<File> getFailedFiles(Set<File> fileSet){
    Set<File> failedFiles=new HashSet<>();
    fileSet.forEach(file->{
      if(file.getState()!=State.OK || file.getState()!=State.CHANGED)
        failedFiles.add(file);
    });
    return failedFiles;
  }

  void updateFileSet() {
    if(needUpdate) {
      Set<File> notUpdatedFiles = dbd.updateFileSet(clientId, fileSetId, fileSet);
      if (!notUpdatedFiles.isEmpty())
        dbd.newEvent(EventType.UPDATE, Results.FAIL, clientId, fileSetId, null);
      else
        dbd.newEvent(EventType.UPDATE, Results.PASS, clientId, fileSetId, null);
      needUpdate=false;
    }
  }
}
