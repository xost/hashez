package org.host43.gibloc;

/**
 * Created by stas on 03.02.2017.
 */
public class BadParametersException extends Exception {
  BadParametersException(String msg){
    super(msg);
  }
  BadParametersException(Exception e){
    super(e);
  }
}
