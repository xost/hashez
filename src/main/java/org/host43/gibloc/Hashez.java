package org.host43.gibloc;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by stas on 15.12.2016.
 */
public class Hashez {
  public static void main(String[] args) throws SQLException, ClassNotFoundException, NoSuchAlgorithmException {
    String clientName="xoxland1";
    DbDialog dbd=DbDialog.getInstance();
    Client client=new Client(clientName,dbd.getFileset(clientName));
    List<File> diffFiles=client.recalculate();
    outFiles(client.getFileSet());
    outFiles(client.getFileSet());
    outFiles(client.getFileSet());
  }

  private static void outFiles(List<File> lst){
    for(File f:lst) {
      try{
        System.out.println(f.getChecksum().toHexString());
      }catch(NullPointerException e){
        System.out.println("NULL");
      }
    }
  }
}
