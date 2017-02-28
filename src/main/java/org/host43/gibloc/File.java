package org.host43.gibloc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by stas on 16.12.2016.
 */
class File {
  private String filename=null;
  private Checksum checksum=null;
  private State state=null;

  File(String filename,byte[] digest,State state){
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

  static List<File> generateFileSet(InputStream in){
    List<File> fileSet=new ArrayList<>();
    Scanner reader=new Scanner(new BufferedInputStream(in));
    String line;
    while(reader.hasNextLine()){
      line=reader.nextLine().trim();
      if(!line.isEmpty())
        fileSet.add(new File(line));
    }
    return fileSet;
  }

  String getFileName(){
    return filename;
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

  File calculate() {
    //Переписать !
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

  @Override
  public boolean equals(Object right){
    File objFile=(File)right;
    return checksum.equals(objFile.getChecksum()) &&
        filename.equals(objFile.getFileName()) &&
        state.equals(objFile.getState());
  }

  boolean theSame(File right){
    return filename.equals(right.getFileName());
  }
}
