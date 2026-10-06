import re

with open('src/main/java/com/team/gardendefense/model/entity/plant/Chomper.java', 'r') as f:
    content = f.read()

new_logic = """
    @Override
    public void update(GameManager gm) {
        if (attackCooldown > 0) {
            attackCooldown--;
            this.image = ImageLoader.getImage("src/main/resources/assets/images/ChomperDigest.png");
            if (attackCooldown == 0) {
                this.image = ImageLoader.getImage("src/main/resources/assets/images/Chomper.png");
            }
            return; // Can't attack while chewing
        }

        // Find a zombie in 3x3 area to eat
"""

content = re.sub(r'@Override\s*public void update\(GameManager gm\) \{\s*if \(attackCooldown > 0\) \{\s*attackCooldown--;\s*return; // Can\'t attack while chewing\s*\}\s*// Find a zombie in 3x3 area to eat', new_logic.strip(), content, flags=re.DOTALL)

with open('src/main/java/com/team/gardendefense/model/entity/plant/Chomper.java', 'w') as f:
    f.write(content)
