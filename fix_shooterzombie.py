import re

with open('src/main/java/com/team/gardendefense/model/entity/monster/ShooterZombie.java', 'r') as f:
    content = f.read()

new_logic = """
        if (attackTarget != null) {
            if (attackTarget.isDead()) { attackTarget = null; }
            else {
                // Đứng lại cắn nếu đụng trúng cây
                shootTimer++;
                if (shootTimer >= Constants.RATE_ZOMBIE_SHOOTER) {
                    attackTarget.takeDamage(Constants.DMG_ZOMBIE_SHOOTER);
                    shootTimer = 0;
                }
                return; // Dừng lại không đi tiếp, không bắn xa
            }
        }

        Plant closestPlant = null; float minDist = Constants.RANGE_ZOMBIE_SHOOTER;
"""

content = re.sub(r'Plant closestPlant = null; float minDist = Constants\.RANGE_ZOMBIE_SHOOTER;', new_logic.strip(), content)

with open('src/main/java/com/team/gardendefense/model/entity/monster/ShooterZombie.java', 'w') as f:
    f.write(content)
