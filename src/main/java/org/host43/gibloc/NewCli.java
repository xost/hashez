package org.host43.gibloc;

import org.apache.commons.cli.CommandLine;

/**
 * Created by stas on 03.02.2017.
 */
public class NewCli implements Command {
  private String cliName=null;
  private String descr=null;

  NewCli(CommandLine cl) throws CommandLineException {
    String cfgFile=cl.getOptionValue("cfg","hashezConfig.xml");
    cliName=cl.getOptionValue("c");
    descr=cl.getOptionValue("d");
    if(cliName==null || descr==null)
      throw new CommandLineException("Command 'newCli' must contains '-c client name'" +
          "and '-d description' arguments");
  }
  @Override
  public void perform() {

  }
}
