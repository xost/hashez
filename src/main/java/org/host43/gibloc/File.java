package org.host43.gibloc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

/**
 * Created by stas on 16.12.2016.
 */
public class File {
  Path file;
  Checksum checksum;

  public File(String file,byte[] digest){
    this.file= Paths.get(file);
    this.checksum=new Checksum(digest);
  }

  public String getFile(){
    return file.toString();
  }

  public Checksum getChecksum(){
    return checksum;
  }

  public File calculate() throws IOException, NoSuchAlgorithmException {
      Checksum newChs=new Checksum(file.toString());
      if(checksum.equals(newChs)){
        return null;
      }else{
        File oldFile=new File(file.toString(),checksum.getDigest());
        checksum=newChs;
        return oldFile;
      }
  }

  public void update(Checksum checksum) {
    this.checksum=checksum;
  }
}
