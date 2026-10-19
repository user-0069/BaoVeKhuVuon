package com.team.gardendefense.system;

import javax.sound.sampled.*;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Quản lý phát âm thanh hiệu ứng (SFX) cho hệ thống Coin và Shop.
 * Hỗ trợ pitch combo tăng dần cao độ khi nhặt liên tục và phát file âm thanh.
 */
public class AudioManager {
    private static final AudioManager instance = new AudioManager();
    public static AudioManager getInstance() { return instance; }

    private final ExecutorService soundPool = Executors.newFixedThreadPool(3);
    private int comboCount = 0;
    private long lastCollectTime = 0;
    private static final long COMBO_RESET_DELAY = 1200; // 1.2 giây
    private boolean sfxEnabled = true;

    private AudioManager() {}

    public boolean isSfxEnabled() {
        return sfxEnabled;
    }

    public void setSfxEnabled(boolean enabled) {
        this.sfxEnabled = enabled;
    }

    public void toggleSfx() {
        this.sfxEnabled = !this.sfxEnabled;
    }

    /**
     * Phát âm thanh nhặt xu với Pitch tăng dần theo combo liên tiếp (PvZ style).
     */
    public void playCoinSound() {
        if (!sfxEnabled) return;
        long now = System.currentTimeMillis();
        if (now - lastCollectTime > COMBO_RESET_DELAY) {
            comboCount = 0;
        }
        comboCount = Math.min(comboCount + 1, 10);
        lastCollectTime = now;

        final int currentCombo = comboCount;
        soundPool.submit(() -> generateTingSound(currentCombo));
    }

    /**
     * Phát âm thanh nhặt kim cương (lấp lánh và ngân vang).
     */
    public void playDiamondSound() {
        if (!sfxEnabled) return;
        soundPool.submit(() -> {
            boolean played = playAudioFile("src/main/resources/assets/sounds/diamond.au");
            if (!played) {
                // Nếu file au không load được, tự động phát âm thanh kim cương ngân dài đặc biệt
                generateDiamondTone();
            }
        });
    }

    /**
     * Phát âm thanh giao dịch mua hàng thành công.
     */
    public void playBuySound() {
        if (!sfxEnabled) return;
        soundPool.submit(() -> {
            generateChimeTone(880, 1175, 120);
        });
    }

    /**
     * Tổng hợp sóng sin âm thanh 'ting' trong trẻo với tần số nâng cao theo combo.
     */
    private void generateTingSound(int combo) {
        try {
            float baseFreq = 1600f + (combo * 120f); // Tăng dần từ ~1720Hz đến ~2800Hz
            float sampleRate = 44100f;
            int durationMs = 180;
            int totalSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buffer = new byte[totalSamples];

            for (int i = 0; i < totalSamples; i++) {
                double time = i / (double) sampleRate;
                // Sóng sine kết hợp sóng họa âm bậc 2
                double wave = Math.sin(2.0 * Math.PI * baseFreq * time) * 0.75 +
                              Math.sin(2.0 * Math.PI * (baseFreq * 2.0) * time) * 0.25;

                // Hàm suy giảm âm lượng theo đường cong mũ (Exponential Decay)
                double decay = Math.exp(-14.0 * (i / (double) totalSamples));
                buffer[i] = (byte) (wave * decay * 115.0);
            }

            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
            }
        } catch (Exception ignored) {}
    }

    /**
     * Sóng âm ngân vang cao cấp cho Kim Cương.
     */
    private void generateDiamondTone() {
        try {
            float sampleRate = 44100f;
            int durationMs = 350;
            int totalSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buffer = new byte[totalSamples];

            for (int i = 0; i < totalSamples; i++) {
                double time = i / (double) sampleRate;
                double wave = Math.sin(2.0 * Math.PI * 2093.0 * time) * 0.5 + // C7
                              Math.sin(2.0 * Math.PI * 2637.0 * time) * 0.3 + // E7
                              Math.sin(2.0 * Math.PI * 3136.0 * time) * 0.2;  // G7
                double decay = Math.exp(-8.0 * (i / (double) totalSamples));
                buffer[i] = (byte) (wave * decay * 110.0);
            }

            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
            }
        } catch (Exception ignored) {}
    }

    private void generateChimeTone(float freq1, float freq2, int durationMs) {
        try {
            float sampleRate = 44100f;
            int totalSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buffer = new byte[totalSamples];

            for (int i = 0; i < totalSamples; i++) {
                double time = i / (double) sampleRate;
                float freq = (i < totalSamples / 2) ? freq1 : freq2;
                double wave = Math.sin(2.0 * Math.PI * freq * time);
                double decay = Math.exp(-6.0 * (i / (double) totalSamples));
                buffer[i] = (byte) (wave * decay * 100.0);
            }

            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
            }
        } catch (Exception ignored) {}
    }

    private boolean playAudioFile(String path) {
        try {
            File soundFile = new File(path);
            if (!soundFile.exists()) return false;
            AudioInputStream stream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            clip.start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

