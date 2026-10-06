package com.team.gardendefense.utils;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class ImageLoader {
    private static final Map<String, Image> imageCache = new HashMap<>();

    public static Image getImage(String path) {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }
        try {
            File file = new File(path);
            if (file.exists()) {
                Image img = ImageIO.read(file);
                imageCache.put(path, img);
                return img;
            } else {
                System.err.println("Image not found: " + path);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
