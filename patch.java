import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.monster.Monster;
import com.team.gardendefense.model.entity.plant.Plant;
import com.team.gardendefense.utils.Constants;

public class Util {
    public static Monster getClosestZombieInDirection(Plant p, GameManager gm, float range) {
        Monster closest = null; float minDist = range;
        for (Monster z : gm.zombies) {
            float dx = z.x - p.x;
            float dy = z.y - p.y;
            boolean inLine = false;
            float dist = 0;
            if (p.direction == 0 && dy < 0 && Math.abs(dx) < 25) { inLine = true; dist = -dy; } // Up
            else if (p.direction == 1 && dx > 0 && Math.abs(dy) < 25) { inLine = true; dist = dx; } // Right
            else if (p.direction == 2 && dy > 0 && Math.abs(dx) < 25) { inLine = true; dist = dy; } // Down
            else if (p.direction == 3 && dx < 0 && Math.abs(dy) < 25) { inLine = true; dist = -dx; } // Left
            
            if (inLine && dist <= minDist) {
                minDist = dist; closest = z;
            }
        }
        return closest;
    }
}
