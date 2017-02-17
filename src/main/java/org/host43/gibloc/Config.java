package org.host43.gibloc;

import org.apache.commons.cli.CommandLine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by stas on 17.02.2017.
 */
class Config {
  private static Config instance;

  private Properties props;

  static Config getInstance(CommandLine cl) throws BadParametersException {
    if(instance==null)
      instance=new Config(cl);
    return instance;
  }

  private Config(CommandLine cl) throws BadParametersException {
    String configFileName;
    configFileName = cl.getOptionValue("cfg","hashezProperties.xml");
    props=new Properties();
    try(InputStream fis=new FileInputStream(configFileName)){
      props.loadFromXML(fis);
    }catch(IOException e){
      throw new RuntimeException("Can not read config file "+e.toString());
    }
    if(props.get("connection")==null||props.get("jdbcDriver")==null||
        props.get("username")==null||props.get("password")==null||
        props.get("cliName")==null||props.get("description")==null)
      throw new BadParametersException("One or more parameters was not given");
  }

   String connection(){
    return (String) props.get("connection");
  }

   String jdbcDriver(){
    return (String) props.get("jdbcDriver");
  }

   String username(){
    return (String) props.get("username");
  }

   String password(){
    return (String) props.get("password");
  }

   String cliName(){
    return (String) props.get("cliName");
  }

   String description(){
    return (String) props.get("description");
  }
}
