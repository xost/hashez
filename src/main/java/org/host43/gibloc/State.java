package org.host43.gibloc;

/**
 * Created by stas on 19.12.2016.
 */
public enum State {
  OK(0),
  UPDATED(1),
  FILESYSTEMERROR(2),
  CRYPTOERROR(3),
  EMPTY(4)
  ;

  private final int state;

  private State(int state){
    this.state = state;
  }

  public int getState(){
    return this.state;
  }
}