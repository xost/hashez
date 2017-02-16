package org.host43.gibloc;

/**
 * Created by stas on 28.12.2016.
 */
public class ClientNotFoundException extends Exception {
  ClientNotFoundException(String reason){
    super(reason);
  }
  ClientNotFoundException(Exception e){
    super(e);
  }
}
