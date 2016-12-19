package org.host43.gibloc;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by stas on 15.12.2016.
 */
public class Hashez {
  public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
    List<String> files=Arrays.asList("C:\\autoexec.bat",
        "C:\\browser.html",
        "C:\\config.sys");
    List<File> fileSet=new ArrayList<>();
    for(String filename:files){
      fileSet.add(new File(filename,null));
    }
    Client cl=new Client(fileSet);
    List<File> diffFiles=cl.recalculate();
    for(File file:diffFiles){
      System.out.println("File %s is different !");
    }
  }
  //  for(String s:files){
  //    Checksum cs=new Checksum(s);
  //    System.out.println(cs.toHexString());
  //  }
  //}
}
