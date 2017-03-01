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
  private Set<File> diffFiles=new HashSet<>();
  private int lastEvent=0;
  private int fileSetId=0;
  private boolean fsChanged=false;
  private boolean checked=false;

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
    Set<File> diffFiles=new HashSet<>();
    fileSet.forEach(file->{
      File old=file.calculate();
      if(old!=null)
        diffFiles.add(file);
    });
    this.diffFiles=diffFiles;
    checked=true;
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
    if(fsChanged) { //Если отличается, то присвоить новый и очистить diffFiles
      this.fileSet = fileSet;
      this.diffFiles.clear();
    }
  }

  Set<File> getDiffFiles(){
    return diffFiles;
  }

  void saveFileSet(DbDialog dbd) {
    if(fsChanged){
      lastEvent = dbd.newEvent(clientId, eventType.NEWFILESET, "New FileSet saved");
      fileSetId = dbd.newFileSet(clientId, fileSet);
    }
  }

  void updateFileSet(DbDialog dbd) {
  }

  void saveDiff(DbDialog dbd) {
    if(checked) {
      lastEvent = dbd.newEvent(clientId, eventType.CHECK,null);
      dbd.saveDiff(lastEvent, diffFiles);
      diffFiles.clear();
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
