#!/bin/bash
sed -i 's/Iterator<Monster> zIt = zombies.iterator();/for (Monster z : zombies) {/g' src/main/java/com/team/gardendefense/controller/GameManager.java
sed -i 's/while(zIt.hasNext()) {//g' src/main/java/com/team/gardendefense/controller/GameManager.java
sed -i 's/Monster z = zIt.next();//g' src/main/java/com/team/gardendefense/controller/GameManager.java
sed -i 's/zIt.remove();/zombies.remove(z);/g' src/main/java/com/team/gardendefense/controller/GameManager.java

sed -i 's/Iterator<Projectile> pIt = projectiles.iterator();/for (Projectile p : projectiles) {/g' src/main/java/com/team/gardendefense/controller/GameManager.java
sed -i 's/while(pIt.hasNext()) {//g' src/main/java/com/team/gardendefense/controller/GameManager.java
sed -i 's/Projectile p = pIt.next();//g' src/main/java/com/team/gardendefense/controller/GameManager.java
sed -i 's/pIt.remove();/projectiles.remove(p);/g' src/main/java/com/team/gardendefense/controller/GameManager.java
