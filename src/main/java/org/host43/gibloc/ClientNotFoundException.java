package org.host43.gibloc;

/**
 * Created by stas on 01.03.2017.
 */
public class ClientNotFoundException extends Exception {
  public ClientNotFoundException(String s) {
    super(s);
  }
  public ClientNotFoundException(Exception e){
    super(e);
  }
}
