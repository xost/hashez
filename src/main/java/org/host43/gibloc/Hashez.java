package org.host43.gibloc;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
    for(String s:files){
      Checksum cs=new Checksum(s);
      System.out.println(cs.toHexString());
    }
  }
}
