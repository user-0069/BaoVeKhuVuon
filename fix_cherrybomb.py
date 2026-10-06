import re

with open('src/main/java/com/team/gardendefense/model/entity/plant/CherryBomb.java', 'r') as f:
    content = f.read()

new_logic = """
public class CherryBomb extends Plant {
    private int explosionTimer = 30; // ~0.5 second explosion delay
    private int state = 0; // 0 = idle, 1 = exploded
    private int boomTimer = 30; // 0.5s for explosion effect

    public CherryBomb(int col, int row) {
        super(col, row);
        this.health = 300;
        this.maxHealth = 300;
        this.image = ImageLoader.getImage("src/main/resources/assets/images/CherryBomb.png");
    }

    @Override
    public void update(GameManager gm) {
        if (state == 1) {
            boomTimer--;
            if (boomTimer <= 0) this.health = 0;
            return;
        }

        if (explosionTimer > 0) {
            explosionTimer--;
            if (explosionTimer <= 0) {
                explode(gm);
            }
        }
    }

    private void explode(GameManager gm) {
        // Kill all zombies in 3x3 area
        for (Monster zombie : gm.zombies) {
            if (Math.abs(zombie.x - this.x) <= Constants.TILE_SIZE * 1.5 && Math.abs(zombie.y - this.y) <= Constants.TILE_SIZE * 1.5) {
                zombie.takeDamage(1800);
            }
        }
        this.image = ImageLoader.getImage("src/main/resources/assets/images/Boom.png");
        this.state = 1;
    }

    @Override
    public void draw(Graphics2D g) {
        if (state == 1 && image != null) {
            g.drawImage(image, x - Constants.TILE_SIZE, y - Constants.TILE_SIZE, Constants.TILE_SIZE * 3, Constants.TILE_SIZE * 3, null);
        } else {
            super.draw(g);
        }
    }
}
"""

content = re.sub(r'public class CherryBomb extends Plant \{.*\}', new_logic.strip(), content, flags=re.DOTALL)

with open('src/main/java/com/team/gardendefense/model/entity/plant/CherryBomb.java', 'w') as f:
    f.write(content)
