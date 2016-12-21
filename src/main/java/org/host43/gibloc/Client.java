package org.host43.gibloc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stas on 16.12.2016.
 */
public class Client {
  List<File> fileSet;
  public Client(List<File> fileSet){
      this.fileSet=fileSet;
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
