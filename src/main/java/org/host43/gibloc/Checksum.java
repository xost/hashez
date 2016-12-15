package org.host43.gibloc;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by xost on 12/14/16.
 */
public class Checksum {
  private MessageDigest md;

    public Checksum(String filename) throws NoSuchAlgorithmException, IOException {
      Path file = Paths.get(filename);
      byte[] buffer=new byte[8192];
      md=MessageDigest.getInstance("MD5");
      FileInputStream fis=new FileInputStream(file.toFile());
      while(fis.read(buffer)!=-1){
        md.update(buffer);
      }
    }

    public Checksum(byte[] digest){
      md.digest(digest);
    }

    public byte[] getDigest(){
        return md.digest();
    }

    public boolean equals(byte[] rightDigest){
      return MessageDigest.isEqual(md.digest(),rightDigest);
    }

    public String toHexString(){
      char[] hexSigns="0123456789ABCDEF".toCharArray();
      byte[] digest=md.digest();
      StringBuilder hexString=new StringBuilder();
      char high;
      char low;
      for(byte b:digest){
        high=hexSigns[(b&0xF0)>>4];
        low=hexSigns[b&0x0F];
        hexString.append(String.format("%s%s",high,low));
      }
      return hexString.toString();
    }
}
