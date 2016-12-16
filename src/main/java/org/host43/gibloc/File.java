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

  public Checksum calculate() throws IOException, NoSuchAlgorithmException {
    return new Checksum(file.toString());
  }

  public void update(Checksum checksum) throws IOException, NoSuchAlgorithmException {
    this.checksum=checksum;
  }
}
