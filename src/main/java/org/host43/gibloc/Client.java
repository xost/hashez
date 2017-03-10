package org.host43.gibloc;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by stas on 16.12.2016.
 */
class Client {

  private int clientId=-1;
  private String clientName;
  private String descr;
  private Set<File> fileSet=new HashSet<>();
  private Set<File> badFiles=new HashSet<>();
  private int lastEvent=0;
  private int fileSetId=0;
  private boolean fsChanged=false;

  Client(String client,DbDialog dbd) throws ClientNotFoundException {
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
    int clientId=dbd.newCli(cliName,desr);
    int fileSetId=-1;
    if(clientId!=-1)
      fileSetId=dbd.newFileSet(clientId,fileSet);
    if(fileSetId!=-1)
      return new Client(cliName,dbd);
    return null;
  }

  void recalculate() {
    Set<File> badFiles=new HashSet<>();
    fileSet.forEach(file->{
      File old=file.calculate2();
      if(old!=null)
        badFiles.add(file);
    });
    this.badFiles=badFiles;
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
      this.badFiles.clear();
    }
  }

  Set<File> getBadFiles(){
    return badFiles;
  }

  void saveFileSet(DbDialog dbd) {
    if(fsChanged){
      fileSetId = dbd.newFileSet(clientId, fileSet);
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

  void updateFileSet(DbDialog dbd) {
    if(!badFiles.isEmpty()){
      dbd.updateFileSet(fileSetId,fileSet);
      dbd.saveBad(lastEvent, fileSetId, badFiles);
      badFiles.clear();
    }else{
      dbd.updateFileSet(fileSetId,fileSet);
    }
  }

}
