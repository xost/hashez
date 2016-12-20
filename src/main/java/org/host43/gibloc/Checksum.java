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
  private MessageDigest md=null;
  private State state;

  public Checksum(String filename,State state){
    Path file = Paths.get(filename);
    this.state=state;
    byte[] buffer = new byte[8192];
    try {
      md = MessageDigest.getInstance("MD5");
      FileInputStream fis = new FileInputStream(file.toFile());
      while (fis.read(buffer) != -1) {
        md.update(buffer);
      }
    }catch(NoSuchAlgorithmException e){
      this.state=State.CRYPTOERROR;
    } catch (IOException e) {
      this.state = State.FILESYSTEMERROR;
    }
  }

  public Checksum(String filename){
    this(filename,State.OK);
  }

  public Checksum(byte[] digest) {
    if(digest==null)
      state=State.EMPTY;
    else{
      state=State.OK;
      md.digest(digest);
    }
  }

  public byte[] getDigest() {
    switch(state){
      case OK:
      case UPDATED:
        return md.digest();
      default:
        return null;
    }
  }

  public void setState(State state){
    this.state=state;
  }

  public State getState(){
    return state;
  }

  public boolean equals(byte[] right) {
    return MessageDigest.isEqual(md.digest(), right);
  }

  public boolean equals(Checksum right) {
    return equals(right.getDigest());
  }

  public String toHexString() {
    char[] hexSigns = "0123456789ABCDEF".toCharArray();
    byte[] digest = md.digest();
    StringBuilder hexString = new StringBuilder();
    char high;
    char low;
    for (byte b : digest) {
      high = hexSigns[(b & 0xF0) >> 4];
      low = hexSigns[b & 0x0F];
      hexString.append(String.format("%s%s", high, low));
    }
    return hexString.toString();
  }
}
