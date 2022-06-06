package org.heckcorp.spacegame;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Util {
    public static InputStream getResource(String filename) throws FileNotFoundException {
        @Nullable InputStream result;
        File file = new File(filename);

        if (file.exists()) {
            System.out.println("Loading resource: " + filename);
            result = new FileInputStream(file);
        } else {
            System.out.println("Loading resource from jar: " + filename);
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            result = loader.getResourceAsStream(filename);
        }

        assert result != null;
        return result;
    }


}
