package org.host43.gibloc;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by stas on 16.12.2016.
 */
public class File {
  private String filename=null;
  private Checksum checksum=null;
  private State state=null;

  public File(String filename,byte[] digest,State state) throws NoSuchAlgorithmException {
    this.filename=filename;
    this.state=state;
    switch(this.state){
      case OK:
      case UPDATED:
        this.checksum=new Checksum(digest);
        break;
    }
  }

  @Override
  public String toString(){
    return filename;
  }

  public Checksum getChecksum(){
    return checksum;
  }

  public State getState(){
    return state;
  }

  public File calculate(){
    Checksum newChs= null;
    try{
      newChs=new Checksum(filename);
    }catch(NoSuchAlgorithmException e){
      state=State.CRYPTOERROR;
      return this;
    }catch(IOException e){
      state=State.FILESYSTEMERROR;
      return this;
    }
    if(state!=State.CRYPTOERROR &&
        state!=State.FILESYSTEMERROR &&
        state!=State.EMPTY){
      if(checksum.equals(newChs)){
        state=State.OK;
        return null;
      }
    }
    state=State.UPDATED;
    checksum=newChs;
    System.out.println(checksum.toHexString());
    return this;
  }
}
