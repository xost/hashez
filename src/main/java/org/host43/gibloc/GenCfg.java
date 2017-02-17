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
  private String connection;
  private String driver;
  private String username;
  private String password;
  private String cliName;
  private String description;
  private String filename;

  private Logger log= LogManager.getLogger(this.getClass());

  GenCfg(CommandLine cl) throws BadParametersException {

    connection=cl.getOptionValue("conn");
    username=cl.getOptionValue("u");
    password=cl.getOptionValue("p");
    cliName=cl.getOptionValue("c");
    description=cl.getOptionValue("d");
    driver=cl.getOptionValue("dr");
    filename=cl.getOptionValue("cfg");
    if(connection==null || username==null ||
        password==null || cliName==null ||
        description==null || filename==null)
      throw new BadParametersException("Expected options are missing.");
  }
  @Override
  public void perform() {
    Properties props=new Properties();
    try {
      props.setProperty("connection", connection);
      props.setProperty("jdbcDriver", driver);
      props.setProperty("username", username);
      props.setProperty("password", password);
      props.setProperty("cliName", cliName);
      props.setProperty("description", description);
      OutputStream fos = new FileOutputStream(filename);
      props.storeToXML(fos,"Hashez config file");
    } catch (IOException e) {
      log.error(e);
      throw new RuntimeException(e);
    }
  }
}
