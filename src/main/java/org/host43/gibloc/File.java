package org.host43.gibloc;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by stas on 16.12.2016.
 */
class File {
  private String filename=null;
  private Checksum checksum=null;
  private State state=null;

  File(String filename,byte[] digest,State state) throws NoSuchAlgorithmException {
    this.filename=filename;
    this.state=state;
    this.checksum=new Checksum(digest);
  }

  File(String filename){
    this.filename=filename;
    state=State.OK;
    checksum=new Checksum((byte[]) null);
    try{
      checksum=new Checksum(filename);
    } catch (NoSuchAlgorithmException e) {
      state=State.CRYPTOERROR;
    } catch (IOException e) {
      state=State.FILESYSTEMERROR;
    }
  }

  @Override
  public String toString(){
    return filename;
  }

  Checksum getChecksum(){
    return checksum;
  }

  State getState(){
    return state;
  }

  File calculate(){
    Checksum newChs;
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
        if(state==State.UPDATED){
          state= State.OK;
          return this;
        }else{
          return null;
        }
      }
    }
    state=State.UPDATED;
    checksum=newChs;
    return this;
  }

  File calculate2() {
    State newSt=State.OK;
    Checksum newChs=new Checksum((byte[])null);
    try{
      newChs=new Checksum(filename);
    }catch(IOException e){
      newSt=State.FILESYSTEMERROR;
    } catch (NoSuchAlgorithmException e) {
      newSt=State.CRYPTOERROR;
    }
    if(checksum.equals(newChs)){
      if(state==newSt)
        return null;
      else
        state=newSt;
    }else{
      checksum=newChs;
      if(newSt==State.OK)
        state=State.UPDATED;
      else
        state=newSt;
    }
    return this;
  }
}
