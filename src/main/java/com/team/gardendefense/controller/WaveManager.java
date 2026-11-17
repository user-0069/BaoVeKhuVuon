package com.team.gardendefense.controller;
import com.team.gardendefense.model.level.WaveData;
import com.team.gardendefense.model.level.SpawnerData;
import com.team.gardendefense.model.level.Waypoint;
import com.team.gardendefense.model.entity.monster.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class WaveManager {
    private static final Logger LOGGER = Logger.getLogger(WaveManager.class.getName());

    private WaveData[] waves;
    private GameManager gm;
    
    private int currentWaveIdx = 0;
    private int waveDelayTimer = 0;
    private boolean isWaitingForWave = true;
    
    // Class nội bộ để theo dõi tiến trình của từng Spawner đang chạy
    private class ActiveSpawner {
        SpawnerData data;
        int zIndex = 0;
        int timer = 0;
    }
    private List<ActiveSpawner> activeSpawners = new ArrayList<>();

    public WaveManager(GameManager gm, WaveData[] waves) {
        this.gm = gm; 
        this.waves = waves == null ? new WaveData[0] : waves;
        if (this.waves.length > 0 && this.waves[0] != null) {
            this.waveDelayTimer = Math.max(0, this.waves[0].delayBefore);
        }
    }

    public void update() {
        if (currentWaveIdx >= waves.length && activeSpawners.isEmpty()) return; // Đã hết mọi Wave
        
        // Giai đoạn chờ trước khi bắt đầu Wave mới
        if (isWaitingForWave) {
            if (waveDelayTimer > 0) {
                waveDelayTimer--;
                if (waveDelayTimer > 0) return;
            }

            WaveData wave = waves[currentWaveIdx];
            isWaitingForWave = false;
            if (wave != null && wave.spawners != null) {
                for (int i = 0; i < wave.spawners.length; i++) {
                    SpawnerData sd = wave.spawners[i];
                    if (!isValidSpawner(sd)) {
                        LOGGER.warning("Bỏ qua cửa sinh quái không hợp lệ ở đợt " + (currentWaveIdx + 1)
                                + ", cửa " + (i + 1));
                        continue;
                    }
                    ActiveSpawner as = new ActiveSpawner();
                    as.data = sd;
                    as.timer = sd.spawnInterval; // Bắt đầu đếm ngược sinh con quái đầu tiên
                    activeSpawners.add(as);
                }
            }
            if (activeSpawners.isEmpty()) advanceWave();
        } else {
            // Giai đoạn xả quái: Xử lý đếm ngược song song cho tất cả các Spawner đang Active
            Iterator<ActiveSpawner> it = activeSpawners.iterator();
            while (it.hasNext()) {
                ActiveSpawner as = it.next();
                as.timer--;
                
                if (as.timer <= 0) {
                    // Đẻ 1 con quái
                    int zId = as.data.zombies[as.zIndex];
                    if (zId == 1) gm.zombies.add(new Zombie(as.data.waypoints));
                    else if (zId == 2) gm.zombies.add(new ShooterZombie(as.data.waypoints));
                    
                    as.zIndex++; // Tiến sang con quái tiếp theo của Spawner này
                    
                    if (as.zIndex >= as.data.zombies.length) {
                        it.remove(); // Spawner này đã xả hết quái, xóa khỏi danh sách Active
                    } else {
                        as.timer = as.data.spawnInterval; // Reset đồng hồ cho con quái kế tiếp
                    }
                }
            }
            
            // Nếu tất cả các cửa (Spawners) của Wave hiện tại đều đã xả hết quái => Chuyển sang Wave kế tiếp
            if (activeSpawners.isEmpty()) advanceWave();
        }
    }

    private boolean isValidSpawner(SpawnerData spawner) {
        if (spawner == null || spawner.spawnInterval <= 0
                || spawner.zombies == null || spawner.zombies.length == 0
                || spawner.waypoints == null || spawner.waypoints.length < 2) {
            return false;
        }
        for (int zombieId : spawner.zombies) {
            if (zombieId != 1 && zombieId != 2) return false;
        }
        for (Waypoint waypoint : spawner.waypoints) {
            if (waypoint == null) return false;
        }
        return true;
    }

    private void advanceWave() {
        currentWaveIdx++;
        if (currentWaveIdx < waves.length) {
            isWaitingForWave = true;
            WaveData nextWave = waves[currentWaveIdx];
            waveDelayTimer = nextWave == null ? 0 : Math.max(0, nextWave.delayBefore);
        }
    }
    
    public boolean isAllSpawned() {
        return waves.length > 0 && currentWaveIdx >= waves.length && activeSpawners.isEmpty();
    }
}
