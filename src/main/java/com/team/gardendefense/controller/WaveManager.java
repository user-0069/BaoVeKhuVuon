package com.team.gardendefense.controller;
import com.team.gardendefense.model.level.WaveData;
import com.team.gardendefense.model.level.Waypoint;
import com.team.gardendefense.model.entity.monster.*;

public class WaveManager {
    private WaveData[] waves;
    private Waypoint[] waypoints;
    private int startY;
    private int currentWaveIdx = 0, zombieIdx = 0, timer = 0;
    private boolean isWaitingForWave = true;
    private GameManager gm;

    public WaveManager(GameManager gm, WaveData[] waves, Waypoint[] wps, int startY) {
        this.gm = gm; this.waves = waves; this.waypoints = wps; this.startY = startY;
        if (waves != null && waves.length > 0) this.timer = waves[0].delayBefore;
    }

    public void update() {
        if (waves == null || currentWaveIdx >= waves.length) return;
        
        timer--;
        if (timer <= 0) {
            if (isWaitingForWave) {
                isWaitingForWave = false;
                timer = waves[currentWaveIdx].spawnInterval;
            } else {
                int zId = waves[currentWaveIdx].zombies[zombieIdx];
                if (zId == 1) gm.zombies.add(new Zombie(startY, waypoints));
                else if (zId == 2) gm.zombies.add(new ShooterZombie(startY, waypoints));

                zombieIdx++;
                if (zombieIdx >= waves[currentWaveIdx].zombies.length) {
                    currentWaveIdx++; zombieIdx = 0;
                    if (currentWaveIdx < waves.length) {
                        isWaitingForWave = true; timer = waves[currentWaveIdx].delayBefore;
                    }
                } else {
                    timer = waves[currentWaveIdx].spawnInterval;
                }
            }
        }
    }
    
    public boolean isAllSpawned() {
        return waves == null || currentWaveIdx >= waves.length;
    }
}
