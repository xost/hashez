package org.host43.gibloc;

enum EventType{
  CHECK,
  UPDATE,
  NEWCLIENT,
  NEWFILESET,
  ;
}

enum Results{
  PASS,
  FAIL,
  ;
}

enum State {
  OK,
  CHANGED,
  ERROR,
  ;
}
