package com.team.gardendefense.model.entity.monster;

import com.team.gardendefense.model.level.Waypoint;
import com.team.gardendefense.utils.Constants;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Hiệu ứng cảnh báo khi Zombie xuất hiện:
 * - Hình tam giác chấm than vàng/cam viền đỏ nổi bật tại điểm xuất phát (Hazard Warning Triangle).
 * - Chuỗi mũi tên chỉ hướng chạy kéo dài trên toàn bộ tuyến đường đi từ đầu tới cuối của Zombie trong 2 giây.
 * - Tồn tại chính xác trong 2 giây (120 frames ở 60 FPS), có hiệu ứng chuyển động dòng chảy và fade-out êm ái.
 */
public class ZombieSpawnAlert {
    public float spawnX;
    public float spawnY;
    public Waypoint[] waypoints;
    public float dirX;
    public float dirY;
    public int timer;
    public static final int MAX_TIMER = 120; // 2 giây (60 FPS * 2)

    public ZombieSpawnAlert(float spawnX, float spawnY, Waypoint[] waypoints) {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.waypoints = waypoints;
        float dx = -1.0f;
        float dy = 0.0f;
        if (waypoints != null && waypoints.length >= 2) {
            float x0 = waypoints[0].col * Constants.TILE_SIZE;
            float y0 = waypoints[0].row * Constants.TILE_SIZE;
            float x1 = waypoints[1].col * Constants.TILE_SIZE;
            float y1 = waypoints[1].row * Constants.TILE_SIZE;
            dx = x1 - x0;
            dy = y1 - y0;
        }
        float len = (float) Math.hypot(dx, dy);
        if (len > 0.001f) {
            this.dirX = dx / len;
            this.dirY = dy / len;
        } else {
            this.dirX = -1.0f; // Mặc định zombie đi từ phải sang trái
            this.dirY = 0.0f;
        }
        this.timer = MAX_TIMER;
    }

    public ZombieSpawnAlert(float spawnX, float spawnY, float dx, float dy) {
        this(spawnX, spawnY, null);
        float len = (float) Math.hypot(dx, dy);
        if (len > 0.001f) {
            this.dirX = dx / len;
            this.dirY = dy / len;
        } else {
            this.dirX = -1.0f;
            this.dirY = 0.0f;
        }
    }

    public void update() {
        if (timer > 0) {
            timer--;
        }
    }

    public boolean isDead() {
        return timer <= 0;
    }

    public void draw(Graphics2D g, int mapWidth, int mapHeight) {
        if (isDead()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Alpha mờ dần ở 0.35 giây cuối (20 frames)
        int alpha = (timer < 20) ? Math.max(0, (int) (255 * (timer / 20.0f))) : 255;

        // Xây dựng danh sách các điểm nút của toàn bộ tuyến đường (từ đầu tới cuối)
        List<Point2D.Float> pathPoints = buildPathPoints();

        // 1. Vẽ chuỗi đường mũi tên kéo dài trên cả tuyến đường từ đầu tới cuối của zombie
        drawFullRouteArrows(g2, pathPoints, alpha);

        // 2. Vẽ hình tam giác chấm than cảnh báo tại ô xuất phát của Zombie
        drawHazardTriangle(g2, mapWidth, mapHeight, alpha);

        g2.dispose();
    }

    private List<Point2D.Float> buildPathPoints() {
        List<Point2D.Float> points = new ArrayList<>();
        if (waypoints != null && waypoints.length >= 2) {
            for (Waypoint wp : waypoints) {
                float px = wp.col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0f;
                float py = wp.row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0f;
                points.add(new Point2D.Float(px, py));
            }
        } else {
            // Mặc định zombie đi thẳng từ vị trí sinh sang mép trái bản đồ
            float startX = spawnX + Constants.TILE_SIZE / 2.0f;
            float startY = spawnY + Constants.TILE_SIZE / 2.0f;
            points.add(new Point2D.Float(startX, startY));
            points.add(new Point2D.Float(0, startY));
        }
        return points;
    }

    /**
     * Vẽ vệt dẫn đường và chuỗi mũi tên động trải dài từ đầu đến cuối đường đi của zombie.
     */
    private void drawFullRouteArrows(Graphics2D g2, List<Point2D.Float> points, int alpha) {
        if (points.size() < 2) return;

        // Tạo path toàn tuyến
        Path2D.Float fullRoute = new Path2D.Float();
        fullRoute.moveTo(points.get(0).x, points.get(0).y);
        for (int i = 1; i < points.size(); i++) {
            fullRoute.lineTo(points.get(i).x, points.get(i).y);
        }

        // 1.1 Vệt hào quang phát sáng mờ dưới làn đường di chuyển
        g2.setColor(new Color(255, 60, 40, (int) (55 * (alpha / 255.0f))));
        g2.setStroke(new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(fullRoute);

        // 1.2 Đường nét đứt phát quang chuyển động (Animated Dashed Guideline)
        float dashPhase = ((MAX_TIMER - timer) * 2.2f) % 20.0f;
        g2.setColor(new Color(255, 140, 50, (int) (140 * (alpha / 255.0f))));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float[]{10, 8}, dashPhase));
        g2.draw(fullRoute);

        // 1.3 Chuỗi mũi tên chuyển động (Marching Directional Arrows) trải đều trên từng đoạn đường
        float arrowSpacing = 44.0f; // Khoảng cách giữa các mũi tên
        float flowOffset = ((MAX_TIMER - timer) * 2.5f) % arrowSpacing;

        for (int i = 0; i < points.size() - 1; i++) {
            Point2D.Float p1 = points.get(i);
            Point2D.Float p2 = points.get(i + 1);

            float dx = p2.x - p1.x;
            float dy = p2.y - p1.y;
            float segLen = (float) Math.hypot(dx, dy);
            if (segLen < 1.0f) continue;

            float uX = dx / segLen;
            float uY = dy / segLen;

            for (float d = flowOffset; d < segLen; d += arrowSpacing) {
                float ax = p1.x + uX * d;
                float ay = p1.y + uY * d;
                drawPathArrow(g2, ax, ay, uX, uY, alpha, 0.95f);
            }
        }

        // 1.4 Biểu tượng cảnh báo điểm kết thúc tuyến đường (nơi zombie tiến vào nhà)
        Point2D.Float endPt = points.get(points.size() - 1);
        int endPulseR = (int) (10.0f + 3.0f * Math.sin((MAX_TIMER - timer) * 0.3f));
        g2.setColor(new Color(255, 40, 40, (int) (160 * (alpha / 255.0f))));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawOval((int) endPt.x - endPulseR, (int) endPt.y - endPulseR, endPulseR * 2, endPulseR * 2);
    }

    /**
     * Vẽ một mũi tên đồ họa sắc nét chỉ hướng đi tại tọa độ (x, y) xoay theo vector (dx, dy).
     */
    private void drawPathArrow(Graphics2D g2, float x, float y, float dx, float dy, int alpha, float scale) {
        double angle = Math.atan2(dy, dx);
        Graphics2D ga = (Graphics2D) g2.create();
        ga.translate(x, y);
        ga.rotate(angle);

        float arrowLen = 16.0f * scale;
        float headLen = 8.0f * scale;
        float headW = 7.0f * scale;
        float shaftW = 3.2f * scale;

        Path2D.Float arrowPath = new Path2D.Float();
        arrowPath.moveTo(arrowLen / 2.0f, 0); // Đỉnh nhọn mũi tên
        arrowPath.lineTo(arrowLen / 2.0f - headLen, -headW); // Cánh trên đầu
        arrowPath.lineTo(arrowLen / 2.0f - headLen, -shaftW / 2.0f); // Thân trên
        arrowPath.lineTo(-arrowLen / 2.0f, -shaftW / 2.0f); // Đuôi trên
        arrowPath.lineTo(-arrowLen / 2.0f, shaftW / 2.0f); // Đuôi dưới
        arrowPath.lineTo(arrowLen / 2.0f - headLen, shaftW / 2.0f); // Thân dưới
        arrowPath.lineTo(arrowLen / 2.0f - headLen, headW); // Cánh dưới đầu
        arrowPath.closePath();

        // Nền mũi tên màu đỏ tươi nổi bật
        ga.setColor(new Color(255, 35, 35, alpha));
        ga.fill(arrowPath);

        // Viền mũi tên màu vàng sáng sắc nét
        ga.setColor(new Color(255, 235, 90, alpha));
        ga.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        ga.draw(arrowPath);

        ga.dispose();
    }

    /**
     * Vẽ hình tam giác chấm than cảnh báo tại ô xuất phát của Zombie (Hazard Warning Triangle).
     */
    private void drawHazardTriangle(Graphics2D g2, int mapWidth, int mapHeight, int alpha) {
        int cx = (int) spawnX + Constants.TILE_SIZE / 2;
        int cy = (int) spawnY + Constants.TILE_SIZE / 2;

        // Giữ vị trí hiển thị luôn nằm trong màn hình nhìn thấy (không bị Header hay mép bản đồ che)
        int drawX = Math.max(40, Math.min(cx, mapWidth - 45));
        int drawY = Math.max(105, Math.min(cy, mapHeight - 35));

        // Hiệu ứng nhịp đập cảnh báo (pulse)
        float pulse = (float) Math.sin((MAX_TIMER - timer) * 0.35f);
        float scale = 1.0f + 0.12f * pulse;

        // Quầng sáng cảnh báo dưới nền
        int glowR = (int) (28 * scale);
        g2.setColor(new Color(255, 60, 0, (int) (70 * (alpha / 255.0f))));
        g2.fillOval(drawX - glowR, drawY - glowR, glowR * 2, glowR * 2);

        // Vẽ hình tam giác cảnh báo
        float triW = 18.0f * scale;
        float triH = 28.0f * scale;
        Path2D.Float triangle = new Path2D.Float();
        triangle.moveTo(drawX, drawY - triH * 0.65f); // Đỉnh tam giác
        triangle.lineTo(drawX - triW, drawY + triH * 0.35f); // Đáy trái
        triangle.lineTo(drawX + triW, drawY + triH * 0.35f); // Đáy phải
        triangle.closePath();

        // Nền tam giác màu vàng rực rỡ
        g2.setColor(new Color(255, 215, 0, alpha));
        g2.fill(triangle);

        // Viền tam giác màu đỏ cam cảnh báo đậm
        g2.setColor(new Color(220, 20, 0, alpha));
        g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(triangle);

        // Dấu chấm than "!" ở giữa tam giác
        g2.setColor(new Color(30, 20, 10, alpha));
        g2.setStroke(new BasicStroke(3.6f * scale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int lineTop = (int) (drawY - triH * 0.28f);
        int lineBot = (int) (drawY + triH * 0.05f);
        g2.drawLine(drawX, lineTop, drawX, lineBot);
        int dotR = Math.max(2, (int) (2.4f * scale));
        int dotY = (int) (drawY + triH * 0.20f);
        g2.fillOval(drawX - dotR, dotY - dotR, dotR * 2, dotR * 2);
    }
}
