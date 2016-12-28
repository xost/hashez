package org.host43.gibloc;

import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by stas on 15.12.2016.
 */
public class Hashez {
  public static void main(String[] args) throws SQLException, ClassNotFoundException, NoSuchAlgorithmException, ClientNotFoundException {
    String clientName="xoxland1";
    DbDialog dbd= null;
    dbd = DbDialog.getInstance();
    Client client=new Client(clientName,dbd);
    client.recalculate();
    outFiles(client.getDiffFiles());
    client.update(dbd);
  }

  private static void outFiles(List<File> lst){
    for(File f:lst) {
      try{
        System.out.println(f.getChecksum().toHexString()+":"+f.getState().toString());
      }catch(NullPointerException e){
        System.out.println("NULL"+":"+f.getState().toString());
      }
    }
  }
}
