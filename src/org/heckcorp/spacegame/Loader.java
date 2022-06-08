package org.heckcorp.spacegame;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Loader {
  public static InputStream getResource(String filename) throws FileNotFoundException {
    @Nullable InputStream result = null;
    File file = new File(filename);

    if (file.exists()) {
      System.out.println("Loading resource: " + filename);
      result = new FileInputStream(file);
    } else {
      System.out.println("Loading resource from jar: " + filename);
      @Nullable ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (loader != null) {
        result = loader.getResourceAsStream(filename);
      }
    }

    assert result != null : "@AssumeAssertion(nullness)";
    return result;
  }
}
