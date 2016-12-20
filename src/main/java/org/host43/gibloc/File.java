package org.host43.gibloc;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by stas on 16.12.2016.
 */
public class File {
  private Path file;
  private Checksum checksum;

  public File(String file,byte[] digest){
    this.file= Paths.get(file);
    this.checksum=new Checksum(digest);
  }

  public String getFilename(){
    return file.toString();
  }

  public Checksum getChecksum(){
    return checksum;
  }

  public File calculate(){
    Checksum newChs=new Checksum(file.toString());
    if(checksum.equals(newChs)){
      return null;
    }else{
      File oldFile=new File(file.toString(),checksum.getDigest());
      checksum=newChs;
      checksum.setState(State.UPDATED);
      return oldFile;
    }
  }
}
