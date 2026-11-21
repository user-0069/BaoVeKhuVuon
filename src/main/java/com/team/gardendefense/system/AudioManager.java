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
     * Phát âm thanh đóng băng toàn sân (hiệu ứng tinh thể băng giá ngân vang).
     */
    /**
     * Phát âm thanh gầm rú khởi động xe cắt cỏ khi chuẩn bị chọn hàng.
     */
    public void playMowerRevSound() {
        if (!sfxEnabled) return;
        soundPool.submit(this::generateMowerRevTone);
    }

    /**
     * Phát âm thanh động cơ xe cắt cỏ gầm rú xuất kích càn quét.
     */
    public void playMowerLaunchSound() {
        if (!sfxEnabled) return;
        soundPool.submit(() -> {
            boolean played = playAudioFile("src/main/resources/assets/sounds/lawnmower.wav");
            if (!played) {
                generateMowerLaunchTone();
            }
        });
    }

    /**
     * Phát âm thanh xe cắt cỏ nghiền nát zombie.
     */
    public void playMowerKillSound() {
        if (!sfxEnabled) return;
        soundPool.submit(this::generateMowerKillTone);
    }

    private void generateMowerRevTone() {
        try {
            float sampleRate = 44100f;
            int durationMs = 260;
            int totalSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buffer = new byte[totalSamples];

            for (int i = 0; i < totalSamples; i++) {
                double time = i / (double) sampleRate;
                float progress = (float) i / totalSamples;
                double freq = 120.0 + 160.0 * progress;
                double wave = Math.sin(2.0 * Math.PI * freq * time) * 0.6 +
                              Math.sin(2.0 * Math.PI * (freq * 2.0) * time) * 0.3 +
                              (Math.random() * 2.0 - 1.0) * 0.15;
                double decay = Math.min(1.0, progress * 4.0) * (1.0 - progress * 0.3);
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

    private void generateMowerLaunchTone() {
        try {
            float sampleRate = 44100f;
            int durationMs = 500;
            int totalSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buffer = new byte[totalSamples];

            for (int i = 0; i < totalSamples; i++) {
                double time = i / (double) sampleRate;
                double rumble = Math.sin(2.0 * Math.PI * 160.0 * time) * 0.5 +
                                Math.sin(2.0 * Math.PI * 320.0 * time) * 0.3 +
                                (Math.random() * 2.0 - 1.0) * 0.25;
                double pulse = 0.8 + 0.2 * Math.sin(2.0 * Math.PI * 25.0 * time);
                double decay = Math.exp(-2.5 * (i / (double) totalSamples));
                buffer[i] = (byte) (rumble * pulse * decay * 115.0);
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

    private void generateMowerKillTone() {
        try {
            float sampleRate = 44100f;
            int durationMs = 220;
            int totalSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buffer = new byte[totalSamples];

            for (int i = 0; i < totalSamples; i++) {
                double time = i / (double) sampleRate;
                double crunch = (Math.random() * 2.0 - 1.0) * 0.7;
                double thud = Math.sin(2.0 * Math.PI * 130.0 * time) * 0.4;
                double decay = Math.exp(-9.0 * (i / (double) totalSamples));
                buffer[i] = (byte) ((crunch + thud) * decay * 120.0);
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
     * Phát âm thanh đào cây bằng Xẻng thường.
     */
    public void playShovelSound() {
        if (!sfxEnabled) return;
        soundPool.submit(() -> {
            boolean played = playAudioFile("src/main/resources/assets/sounds/shovel.wav");
            if (!played) {
                generateShovelTone();
            }
        });
    }

    /**
     * Phát âm thanh đào cây bằng Xẻng Vàng (âm thanh xẻng xúc đất kết hợp tiếng chuông vàng thu hoạch Mặt trời).
     */
    public void playGoldenShovelSound() {
        if (!sfxEnabled) return;
        soundPool.submit(this::generateGoldenShovelTone);
    }

    private void generateShovelTone() {
        try {
            float sampleRate = 44100f;
            int durationMs = 180;
            int totalSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buffer = new byte[totalSamples];

            for (int i = 0; i < totalSamples; i++) {
                double time = i / (double) sampleRate;
                double noise = (Math.random() * 2.0 - 1.0) * 0.45;
                double clang = Math.sin(2.0 * Math.PI * 460.0 * time) * 0.35 +
                               Math.sin(2.0 * Math.PI * 880.0 * time) * 0.2;
                double decay = Math.exp(-12.0 * (i / (double) totalSamples));
                buffer[i] = (byte) ((noise + clang) * decay * 110.0);
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

    private void generateGoldenShovelTone() {
        try {
            float sampleRate = 44100f;
            int durationMs = 380;
            int totalSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buffer = new byte[totalSamples];

            // Tiếng chuông vàng thu hoạch mặt trời (E6 -> G6 -> B6 -> E7)
            float[] notes = {1318.5f, 1567.98f, 1975.5f, 2637.0f};
            int samplesPerNote = totalSamples / notes.length;

            for (int i = 0; i < totalSamples; i++) {
                int noteIdx = Math.min(i / samplesPerNote, notes.length - 1);
                float freq = notes[noteIdx];
                double time = i / (double) sampleRate;
                double wave = Math.sin(2.0 * Math.PI * freq * time) * 0.65 +
                              Math.sin(2.0 * Math.PI * (freq * 1.5) * time) * 0.25;
                int noteLocal = i % samplesPerNote;
                double decay = Math.exp(-5.0 * (noteLocal / (double) samplesPerNote));
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

    public void playFreezeSound() {
        if (!sfxEnabled) return;
        soundPool.submit(() -> {
            boolean played = playAudioFile("src/main/resources/assets/sounds/frozen.wav");
            if (!played) {
                generateFreezeTone();
            }
        });
    }

    /**
     * Phát âm thanh tăng lực vui nhộn khi cho Pet ăn kẹo Chocolate.
     */
    public void playPowerUpSound() {
        if (!sfxEnabled) return;
        soundPool.submit(this::generatePowerUpTone);
    }

    private void generatePowerUpTone() {
        try {
            float sampleRate = 44100f;
            int durationMs = 350;
            int totalSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buffer = new byte[totalSamples];

            // 4 nốt arpeggio tăng dần: C5 (523Hz), E5 (659Hz), G5 (784Hz), C6 (1046Hz)
            float[] notes = {523.25f, 659.25f, 783.99f, 1046.50f};
            int samplesPerNote = totalSamples / notes.length;

            for (int i = 0; i < totalSamples; i++) {
                int noteIdx = Math.min(i / samplesPerNote, notes.length - 1);
                float freq = notes[noteIdx];
                double time = i / (double) sampleRate;
                double wave = Math.sin(2.0 * Math.PI * freq * time) * 0.7 +
                              Math.sin(2.0 * Math.PI * (freq * 2.0) * time) * 0.3;
                int noteLocalIdx = i % samplesPerNote;
                double decay = Math.exp(-4.2 * (noteLocalIdx / (double) samplesPerNote));
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

    private void generateFreezeTone() {
        try {
            float sampleRate = 44100f;
            int durationMs = 600;
            int totalSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buffer = new byte[totalSamples];

            for (int i = 0; i < totalSamples; i++) {
                double time = i / (double) sampleRate;
                double sweep = 1.0 - (i / (double) totalSamples) * 0.4;
                // High crystalline bells + resonant ice crack
                double wave = Math.sin(2.0 * Math.PI * 2200.0 * sweep * time) * 0.35 +
                              Math.sin(2.0 * Math.PI * 3300.0 * sweep * time) * 0.25 +
                              Math.sin(2.0 * Math.PI * 4400.0 * sweep * time) * 0.15 +
                              Math.sin(2.0 * Math.PI * 880.0 * time) * 0.25;
                double shimmer = 0.85 + 0.15 * Math.sin(2.0 * Math.PI * 28.0 * time);
                double decay = Math.exp(-4.5 * (i / (double) totalSamples));
                buffer[i] = (byte) (wave * shimmer * decay * 110.0);
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

