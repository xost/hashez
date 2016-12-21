package org.host43.gibloc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by stas on 15.12.2016.
 */
public class Hashez {
  public static void main(String[] args) {
    List<String> files=Arrays.asList("C:\\autoexec.bat",
        "C:\\browser.html",
        "C:\\config.sys");
    List<File> fileSet=new ArrayList<>();
    for(String filename:files){
      fileSet.add(new File(filename,null,State.EMPTY));
    }
    Client cl=new Client(fileSet);
    List<File> diffFiles=cl.recalculate();
    for(File file:diffFiles){
      System.out.printf("File \"%s\" is different !\n",file.toString());
    }
  }
  //  for(String s:files){
  //    Checksum cs=new Checksum(s);
  //    System.out.println(cs.toHexString());
  //  }
  //}
}
