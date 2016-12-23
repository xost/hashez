package org.host43.gibloc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by stas on 15.12.2016.
 */
public class Hashez {
  public static void main(String[] args) throws SQLException, ClassNotFoundException {
    DbDialog dbd=new DbDialog();
    List<File> clFiles=dbd.getClientFileset("xoxland");

}
