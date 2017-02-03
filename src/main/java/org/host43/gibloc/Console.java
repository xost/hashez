package org.host43.gibloc;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by stas on 02.02.2017.
 */
class Console implements UAction {
  Logger log= LogManager.getLogger(this.getClass());
  private Command cmd;

  Console(List<String> args) throws CommandLineException {
    Options opts=new Options();
    opts.addOption("cfg","config",true,"xml config file");
    opts.addOption("c","client",true,"client name");
    opts.addOption("d","description",true,"description");
    opts.addOption("i","in",true,"file which contains list of files for fileSet");
    opts.addOption("s","save",false,"save results");

    CommandLine cl=null;
    try {
      CommandLineParser prsr=new GnuParser();
      cl=prsr.parse(opts,(String[])args.toArray());
    } catch (ParseException e) {
      log.error(e);
      throw new CommandLineException(e);
    }

    Properties props=new Properties();
    props.setProperty("cliName",cl.getOptionValue("c"));
    props.setProperty("descr",cl.getOptionValue("d"));
    props.setProperty("in",cl.getOptionValue("in"));
    props.setProperty("s",cl.getOptionValue("s"));
    String configFile=cl.getOptionValue("cfg","hashezConfig.xml");
    try {
      InputStream in = new FileInputStream(configFile);
      props.loadFromXML(in);
    }catch(IOException e){
      log.error(e);
      throw new CommandLineException(e);
    }

    String command=args.get(0);
    switch (command){ // один интерфейс, три новых класса и по кейсу вызов одиного из экземпляров
      case "newCli":
        cmd=new NewCli(cl);
        break;
      case "newFS":
        cmd=new NewFS(cl);
        break;
      case "check":
        cmd=new Check(cl);
      default:
        throw new CommandLineException("Unknown command");
    }
  }

  @Override
  public void perform() {
    cmd.perform();
  }
}
