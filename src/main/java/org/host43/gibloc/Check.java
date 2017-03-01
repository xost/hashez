package org.host43.gibloc;

import org.apache.commons.cli.CommandLine;

import java.util.Properties;

/**
 * Created by stas on 03.02.2017.
 */
public class Check implements UAction{
  Check(CommandLine cl) throws BadParametersException {
    Config cfg=Config.getInstance(cl);
  }
  @Override
  public void perform() {

  }
}
