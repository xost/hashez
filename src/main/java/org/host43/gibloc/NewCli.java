package org.host43.gibloc;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by stas on 03.02.2017.
 */
public class NewCli implements UAction {
  private String connection;
  private String username;
  private String password;
  private String cliName;
  private String description;
  private String fileSetFilename;

  private Logger log= LogManager.getLogger(this.getClass());

  NewCli(CommandLine cl) throws BadParametersException {
    String configFileName=cl.getOptionValue("cfg","hashezProperties.xml");
    Properties props=new Properties();
    try {
      InputStream fis=new FileInputStream(configFileName);
      props.loadFromXML(fis);
      connection=props.getProperty("connection");
      username=props.getProperty("username");
      password=props.getProperty("password");
      cliName=props.getProperty("cliName");
      description=props.getProperty("description");
      fileSetFilename=cl.getOptionValue("i");
    } catch (IOException|NullPointerException e) {
      throw new BadParametersException(e);
    }
    if(fileSetFilename==null)
      throw new BadParametersException("Parameter --in was expected");
  }
  @Override
  public void perform() throws ActionException {
    DbDialog dbd;
    try {
      dbd=DbDialog.getInstance(connection,username,password);
      List<File> fileSet=new ArrayList<>();
      InputStream fis=new FileInputStream(fileSetFilename);
      Scanner reader=new Scanner(fis);
      String line;
      while(reader.hasNextLine()){
        line=reader.nextLine();
        fileSet.add(new File(line));
      }
      Client cli=Client.createClient(cliName,description,fileSet,dbd);
      printFileSet(cli.getFileSet());
    } catch (IOException e) {
      e.printStackTrace();
      throw new ActionException(e);
    }
  }

  private void printFileSet(List<File> fileSet){
    fileSet.forEach(file->{
      System.out.println(file.getFileName());
      System.out.println(":"+file.getChecksum().toString());
      System.out.println(":"+file.getState());
    });
  }
}
