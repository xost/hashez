package org.host43.gibloc;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.lang.System.in;

/**
 * Created by stas on 02.02.2017.
 */
class Action {
  private static Logger log = LogManager.getLogger("Action");
  private Action(){}

  static UAction getAction(String[] args) throws CommandLineException {
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
      log.error(e);
      throw new CommandLineException(e);
    }

    try {
      switch (args[0]) {
        //case "newCli":
        //  return new NewCli(cl);
        //case "newFS":
        //  return new NewFS(cl);
        //case "check":
        //  return new Check(cl);
        case "genCfg":
          return new GenCfg(cl);
        //case "gui":
        //  return new Gui(cl);
        default:
          throw new CommandLineException("Unknown command");
      }
    }catch(CommandLineException e){
      printCLHelp(opts);
      throw new CommandLineException(e);
    }
  }

  private static void printCLHelp(Options opts){
    HelpFormatter hfmt=new HelpFormatter();
    hfmt.printHelp("java -jar hashez.jar",opts);
  }
}
