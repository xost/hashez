package org.host43.gibloc;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Scanner;

/**
 * Created by stas on 03.02.2017.
 */
class NewCli implements UAction {

  private String fileSetFilename;
  private Config cfg;

  private Logger log= LogManager.getLogger(this.getClass());

  NewCli(CommandLine cl) throws BadParametersException {
    fileSetFilename=cl.getOptionValue("i");
    if(fileSetFilename==null)
      throw new BadParametersException("Parameter --in was expected");

    cfg=Config.getInstance(cl);
  }

  @Override
  public void perform(){
    DbDialog dbd;
    dbd=DbDialog.getInstance(cfg.connection(),
        cfg.jdbcDriver(),
        cfg.username(),
        cfg.password());

    Set<File> fileSet= null;
    try {
      fileSet = File.generateFileSet(new FileInputStream(fileSetFilename));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }

    Client cli= null;
    try {
      cli = Client.createClient(cfg.cliName(),cfg.description(),fileSet,dbd);
    } catch (ClientNotFoundException e) {
      log.error(e);
      throw new RuntimeException(e);
    }
    assert cli != null;
    printFileSet(cli.getFileSet());
  }

  private void printFileSet(Set<File> fileSet){
    fileSet.forEach(file->{
      System.out.println(file.getFileName());
      System.out.println(":"+file.getChecksum().toString());
      System.out.println(":"+file.getState());
    });
  }
}
