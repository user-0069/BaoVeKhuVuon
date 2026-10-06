import re

with open('src/main/java/com/team/gardendefense/controller/GameManager.java', 'r') as f:
    content = f.read()

# Replace ArrayList with CopyOnWriteArrayList
content = content.replace("import java.util.ArrayList;", "import java.util.ArrayList;\nimport java.util.concurrent.CopyOnWriteArrayList;")
content = content.replace("public List<Plant> plants = new ArrayList<>();", "public List<Plant> plants = new CopyOnWriteArrayList<>();")
content = content.replace("public List<Monster> zombies = new ArrayList<>();", "public List<Monster> zombies = new CopyOnWriteArrayList<>();")
content = content.replace("public List<Projectile> projectiles = new ArrayList<>();", "public List<Projectile> projectiles = new CopyOnWriteArrayList<>();")

# Fix removals to not use Iterator.remove() which fails on CopyOnWriteArrayList
# plants
plants_loop = """
        for (Plant p : plants) {
            if (p.isDead()) plants.remove(p);
            else p.update(this);
        }
"""
content = re.sub(r'Iterator<Plant> pltIt = plants.iterator\(\);\s*while\(pltIt.hasNext\(\)\) \{\s*Plant p = pltIt.next\(\);\s*if \(p.isDead\(\)\) pltIt.remove\(\); else p.update\(this\);\s*\}', plants_loop.strip(), content, flags=re.DOTALL)

# projectiles
proj_loop = """
        for (Projectile p : projectiles) {
            if (p.isDead) projectiles.remove(p);
            else p.update(this);
        }
"""
content = re.sub(r'Iterator<Projectile> pIt = projectiles.iterator\(\);\s*while\(pIt.hasNext\(\)\) \{\s*Projectile p = pIt.next\(\);\s*if \(p.isDead\) pIt.remove\(\); else p.update\(this\);\s*\}', proj_loop.strip(), content, flags=re.DOTALL)

# zombies
zombie_loop = """
        for (Monster z : zombies) {
            if (z.x > Constants.SCREEN_WIDTH) {
                sun -= Constants.ZOMBIE_ESCAPE_PENALTY; 
                zombies.remove(z);
                if (sun < 0) { state = GameState.GAMEOVER; } 
                continue;
            }
            if (z.attackTarget == null && !(z instanceof ShooterZombie)) {
                for (Plant p : plants) {
                    if (z.getBounds().intersects(p.getBounds())) { z.attackTarget = p; break; }
                }
            }
            if (z.isDead()) {
                sun += Constants.ZOMBIE_KILL_REWARD; 
                zombies.remove(z);
            } else {
                z.update(this);
            }
        }
"""
content = re.sub(r'Iterator<Monster> zIt = zombies.iterator\(\);\s*while\(zIt.hasNext\(\)\) \{.*?\s*z.update\(this\);\s*\}\s*\}', zombie_loop.strip(), content, flags=re.DOTALL)


with open('src/main/java/com/team/gardendefense/controller/GameManager.java', 'w') as f:
    f.write(content)
