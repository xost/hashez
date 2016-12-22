package org.host43.gibloc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stas on 16.12.2016.
 */
public class Client {

  private String client;
  private List<File> fileSet;
  public Client(String client,List<File> fileSet){
      this.client=client;
      this.fileSet=fileSet;
  }

  public Client(String client){
    List<File> fileSet=null;
  }

  public List<File> recalculate() {
    List<File> diffFiles=new ArrayList<>();
    for(File file:fileSet){
      File old=file.calculate();
      if(old!=null){
        diffFiles.add(old);
      }
    }
    return diffFiles;
  }
}
