package com.team.gardendefense.utils;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
                // Thu nap tu ClassLoader / Resource neu working directory khac
                String resPath = path;
                if (resPath.startsWith("src/main/resources/")) {
                    resPath = resPath.substring("src/main/resources".length());
                }
                if (!resPath.startsWith("/")) {
                    resPath = "/" + resPath;
                }
                InputStream is = ImageLoader.class.getResourceAsStream(resPath);
                if (is != null) {
                    Image img = ImageIO.read(is);
                    imageCache.put(path, img);
                    return img;
                }
                System.err.println("Image not found: " + path);
                imageCache.put(path, null);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            imageCache.put(path, null);
            return null;
        }
    }
}
