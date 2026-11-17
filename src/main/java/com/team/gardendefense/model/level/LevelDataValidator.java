package com.team.gardendefense.model.level;

public final class LevelDataValidator {
    private LevelDataValidator() {}

    public static String validate(LevelData data) {
        if (data == null) return "Dữ liệu màn chơi bị thiếu.";
        if (data.id <= 0) return "ID màn chơi phải lớn hơn 0.";
        if (data.initialSun < 0) return "Lượng nắng ban đầu không được âm.";
        if (data.map == null || data.map.length == 0 || data.map[0] == null || data.map[0].length == 0) {
            return "Bản đồ không được để trống.";
        }

        int rows = data.map.length;
        int cols = data.map[0].length;
        for (int row = 0; row < rows; row++) {
            if (data.map[row] == null || data.map[row].length != cols) {
                return "Các hàng của bản đồ phải có cùng số ô.";
            }
            for (int tile : data.map[row]) {
                if (tile < 0 || tile > 2) return "Bản đồ chứa loại ô không hợp lệ.";
            }
        }

        if (data.allowedPlants == null || data.allowedPlants.length == 0) {
            return "Màn chơi phải cho phép ít nhất một loại cây.";
        }
        for (int plantId : data.allowedPlants) {
            if (plantId < 1 || plantId > 11) return "Danh sách cây chứa ID không hợp lệ: " + plantId;
        }

        if (data.waves == null || data.waves.length == 0) {
            return "Màn chơi phải có ít nhất một đợt quái.";
        }
        for (int waveIndex = 0; waveIndex < data.waves.length; waveIndex++) {
            WaveData wave = data.waves[waveIndex];
            if (wave == null) return "Đợt " + (waveIndex + 1) + " không hợp lệ.";
            if (wave.delayBefore < 0) return "Thời gian chờ của đợt " + (waveIndex + 1) + " không được âm.";
            if (wave.spawners == null || wave.spawners.length == 0) {
                return "Đợt " + (waveIndex + 1) + " phải có ít nhất một cửa sinh quái.";
            }

            for (int spawnerIndex = 0; spawnerIndex < wave.spawners.length; spawnerIndex++) {
                SpawnerData spawner = wave.spawners[spawnerIndex];
                String prefix = "Đợt " + (waveIndex + 1) + ", cửa sinh quái " + (spawnerIndex + 1) + ": ";
                if (spawner == null) return prefix + "dữ liệu bị thiếu.";
                if (spawner.spawnInterval <= 0) return prefix + "khoảng cách sinh quái phải lớn hơn 0.";
                if (spawner.zombies == null || spawner.zombies.length == 0) {
                    return prefix + "phải có ít nhất một quái.";
                }
                for (int zombieId : spawner.zombies) {
                    if (zombieId != 1 && zombieId != 2) return prefix + "có ID quái không được hỗ trợ: " + zombieId;
                }
                if (spawner.waypoints == null || spawner.waypoints.length < 2) {
                    return prefix + "đường đi cần ít nhất hai điểm.";
                }
                for (Waypoint waypoint : spawner.waypoints) {
                    if (waypoint == null
                            || waypoint.row < 0 || waypoint.row >= rows
                            || waypoint.col < -1 || waypoint.col > cols) {
                        return prefix + "có tọa độ đường đi nằm ngoài bản đồ.";
                    }
                }
            }
        }
        return null;
    }
}
