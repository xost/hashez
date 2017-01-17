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
    String clientName="xoxland1";
    DbDialog dbd=DbDialog.getInstance();
    List<File> fileSet=new ArrayList<>();
    fileSet.add(new File("/home/xost/Documents/gr2.odg"));
    fileSet.add(new File("/home/xost/Documents/kukushka.pdf"));
    fileSet.add(new File("/home/xost/Downloads/ETicket.pdf"));
    fileSet.add(new File("/home/xost/Downloads/programmers_way.pdf"));
    fileSet.add(new File("/home/xost/PDF"));
    //Client client=new Client(clientName,dbd,fileNames);
    Client client=new Client(clientName,dbd);
    client.recalculate();
    outFiles(client.getFileSet());
    System.out.println();
    outFiles(client.getDiffFiles());
    client.update(dbd);
    client.setFileSet(fileSet);
    outFiles(client.getFileSet());
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
