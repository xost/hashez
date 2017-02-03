package org.host43.gibloc;

import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by stas on 15.12.2016.
 */
public class Hashez {
  public static void main(String[] args) throws CommandLineException {
    UAction mode=null;
    List<String> argsArr=Arrays.asList(args);
    if(argsArr.size()<1){
      System.out.println("Error options");
    }else{
      if(!argsArr.get(0).equals("gui")){
        mode=new Console(argsArr);
      }else{
        mode=new Gui();
      }
    }
    assert mode != null;
    mode.perform();
  }
}
