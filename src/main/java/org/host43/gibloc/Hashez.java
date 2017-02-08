package org.host43.gibloc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Created by stas on 15.12.2016.
 */
public class Hashez {
  private static Logger log= LogManager.getLogger("Hashez");

  public static void main(String[] args){
    UAction mode=null;
    List<String> argsArr=Arrays.asList(args);
    if(argsArr.size()<1){
      System.out.println("Error options");
    }else{
      try{
        if(!argsArr.get(0).equals("gui")){
          mode=new Console(argsArr);
        }else{
          mode=new Gui();

        }
      }catch(CommandLineException e){
        log.error(e);
        System.exit(-1);
      }
    }
    assert mode != null;
    mode.perform();
  }
}
