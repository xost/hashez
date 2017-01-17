package org.host43.gibloc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by xost on 12/14/16.
 */
class Checksum {
  byte[] digest;

  Checksum(String filename) throws NoSuchAlgorithmException, IOException {
    Path file = Paths.get(filename);
    byte[] buffer = new byte[8192];
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.reset();
    FileInputStream fis = new FileInputStream(file.toFile());
    while (fis.read(buffer) != -1) {
      md.update(buffer);
    }
    digest = md.digest();
    fis.close();
  }

  Checksum(byte[] digest) {
    this.digest=digest;
  }

  byte[] getDigest() {
    return digest;
  }

  boolean equals(Checksum right) {
    return Arrays.equals(digest,right.getDigest());
  }

  String toHexString() {
    char[] hexSigns = "0123456789ABCDEF".toCharArray();
    byte[] digest = getDigest();
    StringBuilder hexString = new StringBuilder();
    char high;
    char low;
    for (byte b : digest) {
      high = hexSigns[(b & 0xF0) >> 4];
      low = hexSigns[b & 0x0F];
      hexString.append(String.format("%s%s", high, low));
    }
    return hexString.toString();
  }
}
