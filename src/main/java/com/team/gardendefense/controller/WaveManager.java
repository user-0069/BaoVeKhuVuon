package com.team.gardendefense.controller;
import com.team.gardendefense.model.level.WaveData;
import com.team.gardendefense.model.level.SpawnerData;
import com.team.gardendefense.model.entity.monster.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WaveManager {
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
        this.waves = waves; 
        if (waves != null && waves.length > 0) {
            this.waveDelayTimer = waves[0].delayBefore;
        }
    }

    public void update() {
        if (waves == null) return;
        if (currentWaveIdx >= waves.length && activeSpawners.isEmpty()) return; // Đã hết mọi Wave
        
        // Giai đoạn chờ trước khi bắt đầu Wave mới
        if (isWaitingForWave) {
            waveDelayTimer--;
            if (waveDelayTimer <= 0) {
                isWaitingForWave = false; // Bắt đầu Wave!
                // Kích hoạt tất cả các cửa đẻ quái (Spawners) của Wave này cùng lúc
                for (SpawnerData sd : waves[currentWaveIdx].spawners) {
                    ActiveSpawner as = new ActiveSpawner();
                    as.data = sd;
                    as.timer = sd.spawnInterval; // Bắt đầu đếm ngược sinh con quái đầu tiên
                    activeSpawners.add(as);
                }
            }
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
            if (activeSpawners.isEmpty()) {
                currentWaveIdx++;
                if (currentWaveIdx < waves.length) {
                    isWaitingForWave = true;
                    waveDelayTimer = waves[currentWaveIdx].delayBefore;
                }
            }
        }
    }
    
    public boolean isAllSpawned() {
        return waves == null || (currentWaveIdx >= waves.length && activeSpawners.isEmpty());
    }
}
