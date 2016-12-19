package org.host43.gibloc;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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

  //Обработать исключения раньше
  public List<File> recalculate() throws IOException, NoSuchAlgorithmException {
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
