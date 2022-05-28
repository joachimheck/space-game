package org.heckcorp.domination.desktop.view;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Util {
    
    /**
     * @pre filename != null
     */
    public static BufferedImage getImage(String filename) throws IOException
    {
        return getImages(filename, 1, 1)[0];
    }

    /**
     * @param filename the relative path to the image file.
     * @pre filename != null
     * @pre cols > 0 && rows > 0
     * @pre the width of the image referenced by filename must be a
     *   multiple of cols, and the height must be a multiple of rows. 
     */
    public static BufferedImage[] getImages(String filename, int cols, int rows)
        throws IOException
    {
        BufferedImage fileImage = ImageIO.read(getResource(filename));
        BufferedImage[] images = new BufferedImage[cols * rows];
        
        assert fileImage.getWidth() % cols == 0 &&
            fileImage.getHeight() % rows == 0;
        
        int width = fileImage.getWidth() / cols;
        int height = fileImage.getHeight() / rows;
        
        // Copy each sub-image into a separate image.
        for (int i=0; i<cols; i++) {
            for (int j=0; j<rows; j++) {
                int n = rows * i + j;
                int sourceX = width * i;
                int sourceY = height * j;
                
                images[n] =
                    new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
                Graphics g = images[n].getGraphics();
                g.drawImage(fileImage, 0, 0, width, height,
                    sourceX, sourceY, sourceX + width, sourceY + height, null);
            }
        }
        
        return images;
    }

    public static InputStream getResource(String filename)
        throws FileNotFoundException
    {
        File resource = new File(filename);
        InputStream result;
        
        if (resource.exists()) {
            System.out.println("Loading resource: " + filename);
            result = new FileInputStream(resource);
        } else {
            System.out.println("Loading resource from jar: " + filename);
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            result = loader.getResourceAsStream(filename);
        }
        
        return result;
    }
    

}
