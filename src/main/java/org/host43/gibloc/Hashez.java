package org.host43.gibloc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by stas on 15.12.2016.
 */
public class Hashez {
  private static Logger log= LogManager.getLogger("Hashez");

  public static void main(String[] args) {
    UAction mode=null;
    if(args.length<1){
      System.out.println("Error options");
    }else{
      try {
        mode=Action.getAction(args);
      } catch (CommandLineException e) {
        log.error(e);
      }
    }
    assert mode != null;
    mode.perform();
  }
}
