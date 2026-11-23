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
        if (path == null || path.isEmpty()) return null;
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }
        try {
            // 1. Thử đọc trực tiếp từ đường dẫn tệp
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                Image img = ImageIO.read(file);
                if (img != null) {
                    imageCache.put(path, img);
                    return img;
                }
            }

            // Chuẩn hóa đường dẫn tài nguyên ClassLoader
            String resPath = path.replace('\\', '/');
            if (resPath.startsWith("src/main/resources/")) {
                resPath = resPath.substring("src/main/resources".length());
            } else if (resPath.startsWith("BaoVeKhuVuon/src/main/resources/")) {
                resPath = resPath.substring("BaoVeKhuVuon/src/main/resources".length());
            }
            if (!resPath.startsWith("/")) {
                resPath = "/" + resPath;
            }

            // 2. Thử nạp từ ClassLoader / classpath
            InputStream is = ImageLoader.class.getResourceAsStream(resPath);
            if (is == null && Thread.currentThread().getContextClassLoader() != null) {
                String noSlash = resPath.startsWith("/") ? resPath.substring(1) : resPath;
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(noSlash);
            }
            if (is != null) {
                try (InputStream s = is) {
                    Image img = ImageIO.read(s);
                    if (img != null) {
                        imageCache.put(path, img);
                        return img;
                    }
                }
            }

            // 3. Thử tìm trên hệ thống tệp theo các thư mục gốc khả thi (khi chạy từ IDE, thư mục cha hoặc target)
            String[] candidateRoots = {
                "src/main/resources",
                "BaoVeKhuVuon/src/main/resources",
                "../src/main/resources",
                "target/classes",
                "BaoVeKhuVuon/target/classes",
                "../target/classes"
            };
            for (String root : candidateRoots) {
                File candidateFile = new File(root + resPath);
                if (candidateFile.exists() && candidateFile.isFile()) {
                    Image img = ImageIO.read(candidateFile);
                    if (img != null) {
                        imageCache.put(path, img);
                        return img;
                    }
                }
            }

            System.err.println("Image not found: " + path);
            imageCache.put(path, null);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            imageCache.put(path, null);
            return null;
        }
    }
}
