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
  private boolean fsChanged=false;

  Client(String client,DbDialog dbd) throws ClientNotFoundException, SQLException, NoSuchAlgorithmException {
    clientName=client;
    clientId=dbd.getClientId(clientName);
    descr=dbd.getDescription(clientId);
    fileSet = dbd.getFileSet(clientId);
  }

  Client(String client,String descr,List<String> fileNames,DbDialog dbd) throws SQLException {
    this.clientId=dbd.getClientId(client);
    this.descr=descr;
    fileSet=new ArrayList<>();
    fileNames.forEach(fileName->
        fileSet.add(new File(fileName))
    );
    fsChanged=true;
  }

  void recalculate() {
    List<File> diffFiles=new ArrayList<>();
    fileSet.forEach(file->{
      File old=file.calculate2();
      if(old!=null)
        diffFiles.add(file);
    });
    this.diffFiles=diffFiles;
  }

  List<File> getFileSet(){
    return fileSet;
  }

  void setFileSet(List<File> fileSet){
    this.fileSet=fileSet;
    fsChanged=true;
  }

  List<File> getDiffFiles(){
    return diffFiles;
  }

  void update(DbDialog dbd) throws SQLException, NoSuchAlgorithmException {
    //List<File>failFiles=dbd.update(clientId,diffFiles);
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
    if(fsChanged==true){
      try {
        clientId = dbd.newCli(clientName, descr);
        fileSet = dbd.getFileSet(clientId);
      }catch(SQLException | NoSuchAlgorithmException e){
        throw new SQLException(e);
      }
      dbd.newEvent(clientId,eventType.NEWCLIENT,Result.OK);
      dbd.newEvent(clientId,eventType.NEWFILESET,Result.OK);
      fsChanged=false;
    }
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
