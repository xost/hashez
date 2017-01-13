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
  private List<File> fileSet=null;
  private List<File> diffFiles=null;

  Client(String client,DbDialog dbd) throws ClientNotFoundException, SQLException, NoSuchAlgorithmException {
    clientId=dbd.getClientId(client);
    fileSet = dbd.getFileSet(clientId);
  }

  Client(String client,DbDialog dbd,List<String> fileNames) throws SQLException {
    this.clientId=dbd.getClientId(client);
    fileSet=new ArrayList<>();
    for(String fileName:fileNames){
      fileSet.add(new File(fileName));
    }
  }

  void recalculate() {
    List<File> diffFiles=new ArrayList<>();
    for(File file:fileSet){
      File old=file.calculate2();
      if(old!=null)
        diffFiles.add(old);
    }
    this.diffFiles=diffFiles;
  }

  List<File> getFileSet(){
    return fileSet;
  }

  void setFileSet(List<File> fileSet){
    this.fileSet=fileSet;
    recalculate();
  }

  List<File> getDiffFiles(){
    return diffFiles;
  }

  void save(DbDialog dbd){
    //dbd.save();
  }

  void reset(DbDialog dbd){
    //dbd.newEvent(eventState=CHECK);
    //dbd.reset();
  }

  List<File> update(DbDialog dbd) throws SQLException {
    //Если clientId==-1 выбросить исключение о том что клиент не существует
    List<File>failFiles=dbd.update(clientId,diffFiles);
    return failFiles;
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

//1. update (в model(File) новый fileSet в model(Check) - diffFileSet после чего diffFileSet очистить event - update)
//2. check (diffFileSet -> model(Check) , diffFileSet - empty )
