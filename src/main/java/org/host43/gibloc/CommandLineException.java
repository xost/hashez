package org.host43.gibloc;

/**
 * Created by stas on 03.02.2017.
 */
public class CommandLineException extends Exception {
  CommandLineException(String msg){
    super(msg);
  }
  CommandLineException(Exception e){
    super(e);
  }
}
