package org.host43.gibloc;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.Scanner;

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
    } catch (NoSuchAlgorithmException|IOException e) {
      state=State.ERROR;
    }
  }

  static Set<File> generateFileSet(InputStream in){
    Set<File> fileSet=new HashSet<>();
    Set<String> filenames=new HashSet<>();
    Scanner reader=new Scanner(new BufferedInputStream(in));
    String line;
    while(reader.hasNextLine()){
      line=reader.nextLine().trim();
      if(!line.isEmpty())
        filenames.add(line);
    }
    filenames.forEach(fn->fileSet.add(new File(fn)));
    return fileSet;
  }

  static void outFileSet(Set<File> fileSet, OutputStream out){
    fileSet.forEach(file->{
      System.out.println(file.toString()+" : "+
          file.getChecksum().toHexString()+" : "
          +file.getState());
    });
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

  File calculate(){
    Checksum savedChecksum=new Checksum(checksum.getDigest());
    checksum=new Checksum((byte[])null);
    try{
      checksum=new Checksum(filename);
    } catch (NoSuchAlgorithmException|IOException e) {
      state=State.ERROR;
      return this;
    }
    if(checksum.equals(savedChecksum)){
      switch(state){
        case OK:
          return null;
        case CHANGED:
          state=State.OK;
          return null;
          //break;
        case ERROR:
          state=State.CHANGED;
          break;
      }
    }else{
      state=State.CHANGED;
    }
    return this;
  }

  //@Override
  //public boolean equals(Object right){
  //  File objFile=(File)right;
  //  return checksum.equals(objFile.getChecksum()) &&
  //      filename.equals(objFile.getFileName()) &&
  //      state.equals(objFile.getState());
  //}

  boolean theSame(File right){
    return filename.equals(right.getFileName());
  }
}
