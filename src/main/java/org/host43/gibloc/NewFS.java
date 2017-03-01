package org.host43.gibloc;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Scanner;

/**
 * Created by stas on 03.02.2017.
 */
class NewFS implements UAction {

  private Set<File> fileSet=new HashSet<>();
  private DbDialog dbd;
  private Client cli;

  private Logger log= LogManager.getLogger(this.getClass());

  NewFS(CommandLine cl) throws BadParametersException {
    String fileSetFilename=cl.getOptionValue("i");
    if(fileSetFilename==null){
      throw new BadParametersException("Parameter --in was expected");
    }
    try {
      fileSet=File.generateFileSet(new FileInputStream(fileSetFilename));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    Config cfg=Config.getInstance(cl);
    dbd=DbDialog.getInstance(cfg.connection(),
        cfg.jdbcDriver(),
        cfg.username(),
        cfg.password());
    try {
      cli=new Client(cfg.cliName(),dbd);
    } catch (ClientNotFoundException e) {
      log.error(e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void perform() {
    cli.setFileSet(fileSet);
    cli.saveFileSet(dbd);
  }
}
