package org.host43.gibloc;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by stas on 15.12.2016.
 */
public class Hashez {
  private static Logger log= LogManager.getLogger(Hashez.class);

  public static void main(String[] args) {
    UAction mode=null;
    if(args.length<1){
      System.out.println("Error options");
    }else{
      try {
        mode=getAction(args);
      } catch (BadParametersException e) {
        log.error(e);
        System.exit(-1);
      }
    }
    assert mode != null;
    try {
      mode.perform();
    } catch (ActionException e) {
      e.printStackTrace();
    }
  }

  private static UAction getAction(String[] args) throws BadParametersException {
    Options opts = new Options();
    opts.addOption("cfg", "config", true, "xml config file");
    opts.addOption("c", "client", true, "client name");
    opts.addOption("d", "description", true, "description");
    opts.addOption("conn", "connection", true, "connection string");
    opts.addOption("u", "username", true, "database username");
    opts.addOption("p", "password", true, "database user's password");
    opts.addOption("i", "in", true, "file which contains list of files for fileSet");
    opts.addOption("s", "save", false, "save results");

    CommandLine cl = null;
    try {
      CommandLineParser prsr = new GnuParser();
      cl = prsr.parse(opts,args);
    } catch (ParseException e) {
      throw new BadParametersException(e);
    }

    switch (args[0]) {
      case "newCli":
        return new NewCli(cl);
      //case "newFS":
      //  return new NewFS(cl);
      //case "check":
      //  return new Check(cl);
      case "genCfg":
        return new GenCfg(cl);
      //case "gui":
      //  return new Gui(cl);
      default:
        throw new BadParametersException("Unknown command");
    }
  }

  private static void printCLHelp(Options opts){
    HelpFormatter hfmt=new HelpFormatter();
    hfmt.printHelp("java -jar hashez.jar",opts);
  }
}
