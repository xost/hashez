package org.host43.gibloc;

import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stas on 15.12.2016.
 */
public class Hashez {
  public static void main(String[] args) throws SQLException, ClassNotFoundException, NoSuchAlgorithmException, ClientNotFoundException, ClientCreationException {
    String clientName="xoxland";
    DbDialog dbd=DbDialog.getInstance();
    //List<String> fileNames=new ArrayList<>();
    //fileNames.add("/home/xost/Documents/gr2.odg");
    //fileNames.add("/home/xost/Documents/kukushka.pdf");
    //fileNames.add("/home/xost/Downloads/ETicket.pdf");
    //fileNames.add("/home/xost/Downloads/programmers_way.pdf");
    //fileNames.add("/home/xost/PDF");
    //Client client=new Client(clientName,dbd,fileNames);
    Client client=new Client(clientName,dbd);
    client.recalculate();
    outFiles(client.getFileSet());
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
