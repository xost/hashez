package org.host43.gibloc;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stas on 16.12.2016.
 */
class Client {

  private String client;
  private List<File> fileSet;
  private List<File> diffFiles;

  Client(String client,DbDialog dbd) throws ClientNotFoundException {
    try {
      fileSet = dbd.getFileSet(client);
    }catch(NoSuchAlgorithmException | SQLException e){
      throw new ClientNotFoundException();
    }
  }

  Client(String client,List<String> filenNames){

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

  List<File> getDiffFiles(){
    return diffFiles;
  }

  void update(DbDialog dbd){
    List<File>failFiles=dbd.update(client,diffFiles);
    if(!failFiles.isEmpty()){
      for(File file:failFiles)
        System.out.println(file.toString());
    }
  }
}
