package org.host43.gibloc;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

class Config {
  private static Config instance;
  private CommandLine cl;
  private Properties props;
  private static Logger log= LogManager.getLogger(Hashez.class);

  static Config getInstance(CommandLine cl) throws BadParametersException {
    if(instance==null)
      instance=new Config(cl);
    return instance;
  }

  private Config(CommandLine cl) throws BadParametersException {
    this.cl=cl;
    String configFileName;
    configFileName = cl.getOptionValue("cfg","hashezProperties.xml");
    props=new Properties();

    try(InputStream fis=this.getClass().getResourceAsStream("/"+configFileName)){
      props.loadFromXML(fis);
    }catch(IOException e){
      e=new IOException("Can not read config file: "+e.toString(),e);
      log.error(e);
      throw new RuntimeException(e);
    }
    String cliName=cl.getOptionValue("c",null);
    String descr=cl.getOptionValue("d",null);
    if(cliName!=null)props.setProperty("cliName", cliName);
    if(descr!=null)props.setProperty("description", descr);
    if(props.get("connection")==null||props.get("jdbcDriver")==null||
        props.get("username")==null||props.get("password")==null||
        props.get("cliName")==null||props.get("description")==null)
      throw new BadParametersException("One or more parameters was not given");
  }

  void saveConfig() {
    String configFileName=cl.getOptionValue("cfg","hashezProperties.xml");
    try(OutputStream fos=new FileOutputStream(configFileName)){
      props.storeToXML(fos,"hashez_config file");
    }catch(IOException e){
      log.error(e);
      throw new RuntimeException(e);
    }
  }

  String connection() {
    return props.getProperty("connection");
  }

  String jdbcDriver() {
    return props.getProperty("jdbcDriver");
  }

  String username() {
    return props.getProperty("username");
  }

  String password() {
    return props.getProperty("password");
  }

  String cliName() {
    return props.getProperty("cliName");
  }

  String description(){return props.getProperty("description");
  }

  boolean save(){
    return cl.hasOption("s");
  }
}
