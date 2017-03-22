package org.host43.gibloc;

import org.apache.commons.cli.CommandLine;

import java.util.Properties;
import java.util.Set;

/**
 * Created by stas on 03.02.2017.
 */
public class Check implements UAction{

  private Client cli;
  private DbDialog dbd;
  private boolean save;

  Check(CommandLine cl) throws BadParametersException, ClientNotFoundException {
    Config cfg=Config.getInstance(cl);
    save=cfg.save();
    dbd=DbDialog.getInstance(cfg.connection(),
        cfg.jdbcDriver(),
        cfg.username(),
        cfg.password());
    cli=new Client(cfg.cliName(),dbd);
  }
  @Override
  public void perform() {
    cli.recalculate();
    if(save)
      cli.updateFileSet();
    File.outFileSet(cli.getBadFiles(),System.out);
  }
}
