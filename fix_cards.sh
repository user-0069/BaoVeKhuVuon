#!/bin/bash
# Remove btnDir usage
sed -i '/public Rectangle btnDir/d' src/main/java/com/team/gardendefense/controller/GameManager.java
sed -i '/btnDir = new Rectangle/d' src/main/java/com/team/gardendefense/controller/GameManager.java
sed -i '/drawButton(g, btnDir/d' src/main/java/com/team/gardendefense/controller/GameManager.java
sed -i '/if (btnDir.contains(mX, mY))/,+3d' src/main/java/com/team/gardendefense/controller/GameManager.java
sed -i '/public int plantDirection/d' src/main/java/com/team/gardendefense/controller/GameManager.java

# We need to change how p.direction is set. Instead of p.direction = plantDirection, it will be based on ID.
sed -i 's/p.direction = plantDirection;//g' src/main/java/com/team/gardendefense/controller/GameManager.java

