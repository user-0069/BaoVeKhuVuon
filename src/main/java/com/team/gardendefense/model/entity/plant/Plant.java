package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Image;
import com.team.gardendefense.utils.ImageLoader;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.view.renderer.Reanim;

public abstract class Plant {
    public int x, y;
    public int health;
    public int maxHealth;
    public Image image;
    public int direction = 1; // 0=Up, 1=Right, 2=Down, 3=Left
    protected Reanim animation;
    protected double animationTicks;
    protected int actionTicksRemaining;
    protected int actionDuration;

    public Plant(int col, int row) {
        this.x = col * Constants.TILE_SIZE;
        this.y = row * Constants.TILE_SIZE;
    }
    public abstract void update(GameManager gm);
    public void advanceAnimation() {
        animationTicks++;
        if (actionTicksRemaining > 0) actionTicksRemaining--;
    }

    protected void setAnimation(String fileName) {
        animation = Reanim.load(fileName);
    }

    protected void playAction(String clip) {
        actionDuration = animation == null ? 18 : animation.durationTicks(clip);
        actionTicksRemaining = actionDuration;
    }

    protected String animationClip() {
        return actionTicksRemaining > 0 ? "shoot" : "idle";
    }

    protected double animationTime() {
        return actionTicksRemaining > 0 ? actionDuration - actionTicksRemaining : animationTicks;
    }

    public void draw(Graphics2D g) {
        drawPlantImage(g, Constants.TILE_SIZE);
    }

    protected void drawPlantImage(Graphics2D g, int size) {
        drawPlantImage(g, size, true);
    }

    protected void drawPlantImage(Graphics2D g, int size, boolean useAnimation) {
        if (image != null || (useAnimation && animation != null)) {
            int cx = x + Constants.TILE_SIZE / 2;
            int cy = y + Constants.TILE_SIZE / 2;
            Graphics2D g2 = (Graphics2D) g.create();
            if (useAnimation && animation != null) {
                if (direction == 0) g2.rotate(-Math.PI / 2, cx, cy);
                else if (direction == 2) g2.rotate(Math.PI / 2, cx, cy);
                else if (direction == 3) {
                    g2.translate(2.0 * cx, 0);
                    g2.scale(-1, 1);
                }
                // Calibrated scale and offsets: plant stays strictly within tile bounds (no overflow)
                // and rests on the ground baseline (Y = 57) matching the zombie walking line.
                float plantScale = 0.85f;
                int plantDrawX = x - 2;
                int plantDrawY = y - 8;
                animation.draw(g2, plantDrawX, plantDrawY, plantScale,
                        animationClip(), animationTime(), animationTicks, false);
                g2.dispose();
                return;
            }

            // Plants without a rig still breathe and lean gently from their roots.
            int drawSize = Math.min(size, Constants.TILE_SIZE - 10);
            int drawOffsetX = (Constants.TILE_SIZE - drawSize) / 2;
            int drawOffsetY = Constants.TILE_SIZE - drawSize - 7; // Bottom baseline at Y = 57
            int rootY = y + 57;

            // Natural organic idle breathing
            double idleSway = Math.sin(animationTicks * 0.045 + x * 0.013) * 0.035;
            double breatheY = 1.0 + Math.sin(animationTicks * 0.065 + y * 0.019) * 0.025;
            double breatheX = 1.0 - Math.sin(animationTicks * 0.065 + y * 0.019) * 0.015;

            // Root-anchored shooting animation (rotates and squashes from roots - NO translation/sliding)
            double shootAngle = 0;
            double shootScaleX = 1.0;
            double shootScaleY = 1.0;

            if (actionTicksRemaining > 0) {
                double t = (actionDuration - actionTicksRemaining) / (double) actionDuration;
                if (t < 0.20) {
                    double p = t / 0.20;
                    shootAngle = -0.04 * Math.sin(p * Math.PI * 0.5); // Anticipation: leans back ~2.3 deg
                    shootScaleX = 1.0 - 0.05 * p;
                    shootScaleY = 1.0 + 0.05 * p;
                } else if (t < 0.45) {
                    double p = (t - 0.20) / 0.25;
                    shootAngle = -0.04 + 0.09 * Math.sin(p * Math.PI * 0.5); // Recoil lunge forward ~+3 deg
                    shootScaleX = 0.95 + 0.12 * p;
                    shootScaleY = 1.05 - 0.09 * p;
                } else {
                    double p = (t - 0.45) / 0.55;
                    double damp = Math.exp(-p * 3.5);
                    shootAngle = 0.05 * damp * Math.cos(p * Math.PI * 2.0); // Gentle harmonic rebound
                    shootScaleX = 1.0 + 0.07 * damp * Math.cos(p * Math.PI * 2.0);
                    shootScaleY = 1.0 - 0.04 * damp * Math.cos(p * Math.PI * 2.0);
                }
            }

            double totalAngle = idleSway + (direction == 3 ? -shootAngle : shootAngle);
            double totalScaleX = breatheX * shootScaleX;
            double totalScaleY = breatheY * shootScaleY;

            // Pivot strictly around the root anchor point on the ground (cx, rootY)
            g2.translate(cx, rootY);
            g2.rotate(totalAngle);
            g2.scale(totalScaleX, totalScaleY);
            g2.translate(-cx, -rootY);

            if (direction == 0) { // Up
                g2.rotate(-Math.PI / 2, cx, cy);
                g2.drawImage(image, x + drawOffsetX, y + drawOffsetY, drawSize, drawSize, null);
            } else if (direction == 2) { // Down
                g2.rotate(Math.PI / 2, cx, cy);
                g2.drawImage(image, x + drawOffsetX, y + drawOffsetY, drawSize, drawSize, null);
            } else if (direction == 3) { // Left (flip)
                g2.drawImage(image, x + drawOffsetX + drawSize, y + drawOffsetY, -drawSize, drawSize, null);
            } else { // Right
                g2.drawImage(image, x + drawOffsetX, y + drawOffsetY, drawSize, drawSize, null);
            }
            g2.dispose();
        }
    }
    
    public void takeDamage(int amount) { health -= amount; }
    public boolean isDead() { return health <= 0; }
    public Rectangle getBounds() { return new Rectangle(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE); }

    public int getCost() {
        if (this instanceof Sunflower) return Constants.COST_SUNFLOWER;
        if (this instanceof Peashooter) return Constants.COST_PEASHOOTER;
        if (this instanceof SniperPlant) return Constants.COST_SNIPER;
        if (this instanceof WallNut) return Constants.COST_WALLNUT;
        if (this instanceof SnowPea) return Constants.COST_SNOWPEA;
        if (this instanceof CherryBomb) return Constants.COST_CHERRYBOMB;
        if (this instanceof Chomper) return Constants.COST_CHOMPER;
        if (this instanceof Marigold) return Constants.COST_MARIGOLD;
        return 100;
    }

    public int getRefundAmount(boolean isGoldenShovel) {
        if (!isGoldenShovel) return 0;
        return getCost() / 2;
    }
}
