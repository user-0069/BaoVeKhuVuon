package com.team.gardendefense.system;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Quản lý phát âm thanh hiệu ứng (SFX) cho hệ thống chiến đấu, Coin và Shop.
 * Hỗ trợ cache file .wav vào RAM, pitch combo khi nhặt xu và tổng hợp sóng âm fallback.
 */
public class AudioManager {
    private static final AudioManager instance = new AudioManager();
    public static AudioManager getInstance() { return instance; }

    private static class CachedSound {
        final AudioFormat format;
        final byte[] data;

        CachedSound(AudioFormat format, byte[] data) {
            this.format = format;
            this.data = data;
        }
    }

    private static final String[] SPLAT_SOUNDS = {
        "src/main/resources/assets/sounds/splat.wav",
        "src/main/resources/assets/sounds/splat2.wav",
        "src/main/resources/assets/sounds/splat3.wav"
    };
    private static final String SNOW_PEA_SPARKLE_SOUND = "src/main/resources/assets/sounds/snow_pea_sparkles.wav";
    private static final String[] SNOW_PEA_SPARKLE_SOUNDS = { SNOW_PEA_SPARKLE_SOUND };
    private static final String CHERRY_BOMB_SOUND = "src/main/resources/assets/sounds/cherrybomb.wav";
    private static final String[] THROW_SOUNDS = {
        "src/main/resources/assets/sounds/throw.wav",
        "src/main/resources/assets/sounds/throw2.wav"
    };
    private static final String[] ROCK_HIT_SOUNDS = {
        "src/main/resources/assets/sounds/bonk.wav"
    };
    private static final String CHOMPER_BITE_SOUND = "src/main/resources/assets/sounds/bigchomp.wav";
    private static final String[] ZOMBIE_CHOMP_SOUNDS = {
        "src/main/resources/assets/sounds/chomp.wav",
        "src/main/resources/assets/sounds/chomp2.wav",
        "src/main/resources/assets/sounds/chompsoft.wav"
    };
    private static final String WIN_MUSIC_SOUND = "src/main/resources/assets/sounds/winmusic.wav";
    private static final String LOSE_MUSIC_SOUND = "src/main/resources/assets/sounds/losemusic.wav";
    private static final long PEA_HIT_THROTTLE_MS = 45;
    private static final long FREEZE_HIT_THROTTLE_MS = 70;
    private static final long THROW_THROTTLE_MS = 80;
    private static final long ROCK_HIT_THROTTLE_MS = 80;
    private static final long CHOMP_THROTTLE_MS = 220;

    private final ExecutorService soundPool = Executors.newFixedThreadPool(8, r -> {
        Thread t = new Thread(r, "AudioManager-SFX");
        t.setDaemon(true);
        return t;
    });
    private final Map<String, CachedSound> soundCache = new ConcurrentHashMap<>();
    private final AtomicLong lastPeaHitTime = new AtomicLong(0);
    private final AtomicLong lastFreezeHitTime = new AtomicLong(0);
    private final AtomicLong lastThrowTime = new AtomicLong(0);
    private final AtomicLong lastRockHitTime = new AtomicLong(0);
    private final AtomicLong lastChompTime = new AtomicLong(0);
    private final Object outcomeLock = new Object();
    private SourceDataLine activeOutcomeLine = null;
    private int outcomePlaybackToken = 0;
    private int comboCount = 0;
    private long lastCollectTime = 0;
    private static final long COMBO_RESET_DELAY = 1200; // 1.2 giây
    private volatile float masterVolume = 1.0f;
    private volatile float lastNonZeroVolume = 1.0f;
    private volatile boolean sfxEnabled = true;

    private AudioManager() {
        Integer saved = WalletManager.getInstance().getData().sfxVolumePercent;
        if (saved != null) {
            applyVolume(saved / 100.0f, false);
        }
    }

    public boolean isSfxEnabled() {
        return sfxEnabled && masterVolume > 0.001f;
    }

    public void setSfxEnabled(boolean enabled) {
        if (enabled) {
            float restored = lastNonZeroVolume > 0.05f ? lastNonZeroVolume : 1.0f;
            applyVolume(restored, true);
        } else {
            applyVolume(0.0f, true);
        }
    }

    public void toggleSfx() {
        setSfxEnabled(!isSfxEnabled());
    }

    public float getVolume() {
        return masterVolume;
    }

    public int getVolumePercent() {
        return Math.round(masterVolume * 100.0f);
    }

    public void setVolume(float volume) {
        applyVolume(volume, false);
    }

    public void setVolumePercent(int percent, boolean save) {
        applyVolume(percent / 100.0f, save);
    }

    public void saveVolume() {
        WalletManager wm = WalletManager.getInstance();
        wm.getData().sfxVolumePercent = getVolumePercent();
        wm.save();
    }

    private void applyVolume(float volume, boolean save) {
        float clamped = Math.max(0.0f, Math.min(1.0f, volume));
        if (clamped > 0.01f) {
            this.lastNonZeroVolume = clamped;
        }
        this.masterVolume = clamped;
        this.sfxEnabled = clamped > 0.001f;
        if (!this.sfxEnabled) {
            stopOutcomeMusic();
        }
        if (save) {
            saveVolume();
        }
    }

    private float getGain() {
        float v = masterVolume;
        if (v <= 0.001f) return 0.0f;
        return (float) Math.pow(v, 1.4);
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

            playPcm8(sampleRate, buffer);
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

            playPcm8(sampleRate, buffer);
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

            playPcm8(sampleRate, buffer);
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

            playPcm8(sampleRate, buffer);
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

            playPcm8(sampleRate, buffer);
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

    /**
     * Âm thanh buzzer cảnh báo khi thao tác thất bại hoặc kỹ năng đang hồi chiêu.
     */
    public void playBuzzerSound() {
        if (!sfxEnabled) return;
        soundPool.submit(this::generateBuzzerTone);
    }

    private void generateBuzzerTone() {
        try {
            float sampleRate = 44100f;
            int durationMs = 140;
            int totalSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buffer = new byte[totalSamples];
            for (int i = 0; i < totalSamples; i++) {
                double time = i / (double) sampleRate;
                double wave = Math.sin(2.0 * Math.PI * 180.0 * time) > 0 ? 0.7 : -0.7;
                double decay = 1.0 - (i / (double) totalSamples);
                buffer[i] = (byte) (wave * decay * 85.0);
            }
            playPcm8(sampleRate, buffer);
        } catch (Exception ignored) {}
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

            playPcm8(sampleRate, buffer);
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

            playPcm8(sampleRate, buffer);
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

            playPcm8(sampleRate, buffer);
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

            playPcm8(sampleRate, buffer);
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

            playPcm8(sampleRate, buffer);
        } catch (Exception ignored) {}
    }

    /**
     * Phát nhạc thắng màn chơi (Victory).
     */
    public void playWinMusic() {
        playOutcomeMusic(WIN_MUSIC_SOUND, true);
    }

    /**
     * Phát nhạc thua màn chơi (Game Over).
     */
    public void playLoseMusic() {
        playOutcomeMusic(LOSE_MUSIC_SOUND, false);
    }

    /**
     * Dừng ngay nhạc thắng/thua khi người chơi chơi lại hoặc rời màn hình kết quả.
     */
    public void stopOutcomeMusic() {
        synchronized (outcomeLock) {
            outcomePlaybackToken++;
            if (activeOutcomeLine != null) {
                try {
                    activeOutcomeLine.stop();
                    activeOutcomeLine.flush();
                    activeOutcomeLine.close();
                } catch (Exception ignored) {}
                activeOutcomeLine = null;
            }
        }
    }

    private void playOutcomeMusic(String path, boolean isWin) {
        if (!sfxEnabled) return;
        final int token;
        synchronized (outcomeLock) {
            stopOutcomeMusic();
            token = ++outcomePlaybackToken;
        }
        soundPool.submit(() -> {
            if (!playStoppableAudioFile(path, token)) {
                generateOutcomeTone(isWin);
            }
        });
    }

    private boolean playStoppableAudioFile(String path, int token) {
        CachedSound cached = loadCachedSound(path);
        if (cached == null) return false;
        SourceDataLine line = null;
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, cached.format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(cached.format);
            synchronized (outcomeLock) {
                if (token != outcomePlaybackToken || !sfxEnabled) {
                    line.close();
                    return true;
                }
                activeOutcomeLine = line;
            }
            line.start();
            int offset = 0;
            int chunkSize = 4096;
            byte[] scratch = new byte[chunkSize];
            while (offset < cached.data.length) {
                synchronized (outcomeLock) {
                    if (token != outcomePlaybackToken || !sfxEnabled) break;
                }
                int len = Math.min(chunkSize, cached.data.length - offset);
                writeScaledChunk(line, cached.data, offset, len, cached.format, scratch);
                offset += len;
            }
            synchronized (outcomeLock) {
                if (token == outcomePlaybackToken && sfxEnabled) {
                    line.drain();
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            synchronized (outcomeLock) {
                if (activeOutcomeLine == line) {
                    activeOutcomeLine = null;
                }
            }
            if (line != null) {
                try { line.close(); } catch (Exception ignored) {}
            }
        }
    }

    private void generateOutcomeTone(boolean isWin) {
        float[] notes = isWin
                ? new float[]{523.25f, 659.25f, 783.99f, 1046.50f}
                : new float[]{392.00f, 369.99f, 349.23f, 293.66f};
        try {
            float sampleRate = 44100f;
            int durationMs = isWin ? 700 : 850;
            int totalSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buffer = new byte[totalSamples];
            int samplesPerNote = totalSamples / notes.length;

            for (int i = 0; i < totalSamples; i++) {
                int idx = Math.min(i / samplesPerNote, notes.length - 1);
                float freq = notes[idx];
                double time = i / (double) sampleRate;
                double wave = Math.sin(2.0 * Math.PI * freq * time) * 0.65;
                int local = i % samplesPerNote;
                double decay = Math.exp(-3.5 * (local / (double) samplesPerNote));
                buffer[i] = (byte) (wave * decay * 105.0);
            }

            playPcm8(sampleRate, buffer);
        } catch (Exception ignored) {}
    }

    /**
     * Phát âm thanh nổ bom CherryBomb.
     */
    public void playCherryBombSound() {
        if (!sfxEnabled) return;
        soundPool.submit(() -> {
            if (!playAudioFile(CHERRY_BOMB_SOUND)) {
                generateCherryBombTone();
            }
        });
    }

    /**
     * Phát âm thanh khi đạn của Plant (Pea/Sniper/SnowPea) trúng Zombie.
     * Giới hạn tần suất (throttle) để tránh vỡ tiếng khi nhiều đạn trúng cùng lúc.
     */
    public void playPeaHitSound(boolean isFreeze) {
        playThrottledSound(SPLAT_SOUNDS, lastPeaHitTime, PEA_HIT_THROTTLE_MS, this::generatePeaHitTone);
        if (isFreeze) {
            playThrottledSound(SNOW_PEA_SPARKLE_SOUNDS, lastFreezeHitTime, FREEZE_HIT_THROTTLE_MS, null);
        }
    }

    /**
     * Phát âm thanh khi ShooterZombie ném đá về phía cây.
     */
    public void playZombieThrowSound() {
        playThrottledSound(THROW_SOUNDS, lastThrowTime, THROW_THROTTLE_MS, null);
    }

    /**
     * Phát âm thanh khi đá của ShooterZombie va trúng cây.
     */
    public void playRockHitPlantSound() {
        playThrottledSound(ROCK_HIT_SOUNDS, lastRockHitTime, ROCK_HIT_THROTTLE_MS, this::generatePeaHitTone);
    }

    /**
     * Phát âm thanh khi Chomper ngoạm nuốt chửng Zombie.
     */
    public void playChomperBiteSound() {
        if (!sfxEnabled) return;
        soundPool.submit(() -> {
            if (!playAudioFile(CHOMPER_BITE_SOUND)) {
                generateMowerKillTone();
            }
        });
    }

    /**
     * Phát âm thanh khi Zombie đứng gặm Plant.
     */
    public void playZombieChompSound() {
        playThrottledSound(ZOMBIE_CHOMP_SOUNDS, lastChompTime, CHOMP_THROTTLE_MS, null);
    }

    private void playThrottledSound(String[] paths, AtomicLong lastTime, long throttleMs, Runnable fallback) {
        if (!sfxEnabled || paths == null || paths.length == 0) return;
        long now = System.currentTimeMillis();
        long prev = lastTime.get();
        if (now - prev >= throttleMs && lastTime.compareAndSet(prev, now)) {
            String chosenPath = paths[ThreadLocalRandom.current().nextInt(paths.length)];
            soundPool.submit(() -> {
                if (!playAudioFile(chosenPath) && fallback != null) {
                    fallback.run();
                }
            });
        }
    }

    private void generateCherryBombTone() {
        try {
            float sampleRate = 44100f;
            int durationMs = 550;
            int totalSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buffer = new byte[totalSamples];

            for (int i = 0; i < totalSamples; i++) {
                double time = i / (double) sampleRate;
                double progress = i / (double) totalSamples;
                double boom = Math.sin(2.0 * Math.PI * (95.0 - 45.0 * progress) * time) * 0.55;
                double noise = (Math.random() * 2.0 - 1.0) * 0.65;
                double decay = Math.exp(-5.0 * progress);
                buffer[i] = (byte) ((boom + noise) * decay * 120.0);
            }

            playPcm8(sampleRate, buffer);
        } catch (Exception ignored) {}
    }

    private void generatePeaHitTone() {
        try {
            float sampleRate = 44100f;
            int durationMs = 75;
            int totalSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buffer = new byte[totalSamples];

            for (int i = 0; i < totalSamples; i++) {
                double time = i / (double) sampleRate;
                double progress = i / (double) totalSamples;
                double freq = 320.0 - 160.0 * progress;
                double wave = Math.sin(2.0 * Math.PI * freq * time) * 0.6 +
                              (Math.random() * 2.0 - 1.0) * 0.4;
                double decay = Math.exp(-14.0 * progress);
                buffer[i] = (byte) (wave * decay * 105.0);
            }

            playPcm8(sampleRate, buffer);
        } catch (Exception ignored) {}
    }

    private boolean playAudioFile(String path) {
        CachedSound cached = loadCachedSound(path);
        if (cached == null || !isSfxEnabled()) return false;
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, cached.format);
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(cached.format);
                line.start();
                int chunkSize = 4096;
                byte[] scratch = new byte[chunkSize];
                int offset = 0;
                while (offset < cached.data.length && isSfxEnabled()) {
                    int len = Math.min(chunkSize, cached.data.length - offset);
                    writeScaledChunk(line, cached.data, offset, len, cached.format, scratch);
                    offset += len;
                }
                line.drain();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void playPcm8(float sampleRate, byte[] buffer) {
        float gain = getGain();
        if (gain <= 0.0f) return;
        try {
            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();
                int chunkSize = 4096;
                byte[] scratch = new byte[chunkSize];
                int offset = 0;
                while (offset < buffer.length && isSfxEnabled()) {
                    int len = Math.min(chunkSize, buffer.length - offset);
                    writeScaledChunk(line, buffer, offset, len, format, scratch);
                    offset += len;
                }
                line.drain();
            }
        } catch (Exception ignored) {}
    }

    private void writeScaledChunk(
            SourceDataLine line, byte[] src, int offset, int len, AudioFormat format, byte[] scratch) {
        float gain = getGain();
        if (gain <= 0.0f) {
            java.util.Arrays.fill(scratch, 0, len, (byte) 0);
            line.write(scratch, 0, len);
            return;
        }
        if (gain >= 0.999f) {
            line.write(src, offset, len);
            return;
        }
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && format.getSampleSizeInBits() == 16) {
            boolean bigEndian = format.isBigEndian();
            int i = 0;
            for (; i + 1 < len; i += 2) {
                int b0 = src[offset + i];
                int b1 = src[offset + i + 1];
                short sample = bigEndian
                        ? (short) ((b0 << 8) | (b1 & 0xFF))
                        : (short) ((b1 << 8) | (b0 & 0xFF));
                int scaled = Math.max(-32768, Math.min(32767, Math.round(sample * gain)));
                if (bigEndian) {
                    scratch[i] = (byte) (scaled >> 8);
                    scratch[i + 1] = (byte) scaled;
                } else {
                    scratch[i] = (byte) scaled;
                    scratch[i + 1] = (byte) (scaled >> 8);
                }
            }
            if (i < len) scratch[i] = src[offset + i];
            line.write(scratch, 0, len);
            return;
        }
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && format.getSampleSizeInBits() == 8) {
            for (int i = 0; i < len; i++) {
                int scaled = Math.max(-128, Math.min(127, Math.round(src[offset + i] * gain)));
                scratch[i] = (byte) scaled;
            }
            line.write(scratch, 0, len);
            return;
        }
        line.write(src, offset, len);
    }

    private CachedSound loadCachedSound(String path) {
        if (path == null || path.isEmpty()) return null;
        CachedSound existing = soundCache.get(path);
        if (existing != null) return existing;

        try (InputStream rawStream = openSoundStream(path)) {
            if (rawStream == null) return null;
            try (AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(rawStream))) {
                AudioFormat baseFormat = in.getFormat();
                AudioFormat targetFormat = baseFormat;
                AudioInputStream pcmStream = in;
                if (baseFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                    targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                    );
                    pcmStream = AudioSystem.getAudioInputStream(targetFormat, in);
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = pcmStream.read(buf)) != -1) {
                    out.write(buf, 0, n);
                }
                if (pcmStream != in) {
                    pcmStream.close();
                }
                CachedSound loaded = new CachedSound(targetFormat, out.toByteArray());
                soundCache.putIfAbsent(path, loaded);
                return soundCache.get(path);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private InputStream openSoundStream(String path) {
        try {
            File direct = new File(path);
            if (direct.exists() && direct.isFile()) {
                return new FileInputStream(direct);
            }
            String resPath = path.replace('\\', '/');
            if (resPath.startsWith("src/main/resources/")) {
                resPath = resPath.substring("src/main/resources".length());
            } else if (resPath.startsWith("BaoVeKhuVuon/src/main/resources/")) {
                resPath = resPath.substring("BaoVeKhuVuon/src/main/resources".length());
            }
            if (!resPath.startsWith("/")) {
                resPath = "/" + resPath;
            }

            InputStream is = AudioManager.class.getResourceAsStream(resPath);
            if (is != null) return is;
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
                is = cl.getResourceAsStream(resPath.substring(1));
                if (is != null) return is;
            }

            String[] roots = {
                "src/main/resources",
                "BaoVeKhuVuon/src/main/resources",
                "../src/main/resources",
                "target/classes",
                "BaoVeKhuVuon/target/classes",
                "../target/classes"
            };
            for (String root : roots) {
                File f = new File(root + resPath);
                if (f.exists() && f.isFile()) {
                    return new FileInputStream(f);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}

