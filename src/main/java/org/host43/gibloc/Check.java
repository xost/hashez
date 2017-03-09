package org.host43.gibloc;

import org.apache.commons.cli.CommandLine;

import java.util.Properties;

/**
 * Created by stas on 03.02.2017.
 */
public class Check implements UAction{

  private Client cli;
  private DbDialog dbd;

  Check(CommandLine cl) throws BadParametersException, ClientNotFoundException {
    Config cfg=Config.getInstance(cl);
    dbd=DbDialog.getInstance(cfg.connection(),
        cfg.jdbcDriver(),
        cfg.username(),
        cfg.password());
    cli=new Client(cfg.cliName(),dbd);
  }
  @Override
  public void perform() {
    System.out.println("FileSet: ");
    File.outFileSet(cli.getFileSet(),System.out);
    //Пересчитать
    cli.recalculate();
    //Записать DiffFileSet
    //Обновить FileSet
    cli.updateFileSet(dbd);
    System.out.println("FileSet: ");
    File.outFileSet(cli.getFileSet(),System.out);
    System.out.println("DiffFileSet: ");
    File.outFileSet(cli.getDiffFiles(),System.out);
  }
}
