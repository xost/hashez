package org.host43.gibloc;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;

/**
 * Created by stas on 09.02.2017.
 */
public class GenCfg implements UAction {
  private OutputStream fis;
  private String connection;
  private String username;
  private String password;
  private String cliName;
  private String description;
  private String filename;

  private Logger log= LogManager.getLogger(this.getClass());

  GenCfg(CommandLine cl) throws CommandLineException {

    connection=cl.getOptionValue("conn");
    username=cl.getOptionValue("u");
    password=cl.getOptionValue("p");
    cliName=cl.getOptionValue("c");
    description=cl.getOptionValue("d");
    filename=cl.getOptionValue("cfg");
    if(connection==null || username==null ||
        password==null || cliName==null ||
        description==null || filename==null)
      throw new CommandLineException("Expected options are missing");
  }
  @Override
  public void perform() {
    Properties props=new Properties();
    try{
      props.setProperty("connection",connection);
      props.setProperty("username",username);
      props.setProperty("password",password);
      props.setProperty("cliName",cliName);
      props.setProperty("description",description);
      props.storeToXML(fis,"Hashez config file");
      fis=new FileOutputStream(filename);
    } catch (NullPointerException | IOException e) {
      log.error(e);
      throw new RuntimeException(e);
    }
  }
}
