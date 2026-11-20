package com.team.gardendefense.view.renderer;

import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.utils.ImageLoader;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/** Renders the part-by-part animation data already shipped with the game. */
public final class Reanim {
    private static final Logger LOGGER = Logger.getLogger(Reanim.class.getName());
    private static final String ROOT = "src/main/resources/assets/reanim/";
    private static final Map<String, Reanim> CACHE = new HashMap<>();
    private static final Map<String, Image> PART_IMAGES = new HashMap<>();
    private static Map<String, String> imageNames;

    private final List<Track> tracks = new ArrayList<>();
    private final Map<String, Clip> clips = new HashMap<>();
    private final int fps;
    private final int frameCount;

    private Reanim(String fileName) throws Exception {
        String xml;
        File source = new File(ROOT + fileName);
        if (source.isFile()) {
            xml = Files.readString(source.toPath(), StandardCharsets.UTF_8);
        } else {
            try (InputStream stream = Reanim.class.getResourceAsStream("/assets/reanim/" + fileName)) {
                if (stream == null) throw new IllegalArgumentException("Missing animation: " + fileName);
                xml = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        // PopCap's .reanim files are XML fragments without a document root.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Element root = factory.newDocumentBuilder().parse(
                new InputSource(new StringReader("<reanim>" + xml + "</reanim>"))).getDocumentElement();
        fps = Math.max(1, Math.round(Float.parseFloat(root.getElementsByTagName("fps").item(0).getTextContent())));
        NodeList trackNodes = root.getElementsByTagName("track");
        int count = 0;
        for (int i = 0; i < trackNodes.getLength(); i++) {
            Element trackElement = (Element) trackNodes.item(i);
            NodeList frameNodes = trackElement.getElementsByTagName("t");
            count = Math.max(count, frameNodes.getLength());
        }
        frameCount = count;
        for (int i = 0; i < trackNodes.getLength(); i++) {
            Element element = (Element) trackNodes.item(i);
            String name = element.getElementsByTagName("name").item(0).getTextContent();
            NodeList nodes = element.getElementsByTagName("t");
            Pose[] poses = new Pose[frameCount];
            Pose current = new Pose();
            for (int frame = 0; frame < frameCount; frame++) {
                if (frame < nodes.getLength()) current = current.with((Element) nodes.item(frame));
                poses[frame] = current;
            }
            tracks.add(new Track(name, poses));
        }
        findClip("idle", "anim_idle");
        findClip("walk", "anim_walk");
        findClip("eat", "anim_eat");
        findClip("shoot", "anim_shooting");
        findClip("bite", "anim_bite");
        findClip("chew", "anim_chew");
        findClip("swallow", "anim_swallow");
        findClip("explode", "anim_explode");
        // PopCap's PeaShooter and SnowPea reanim files store the full idle (stem + head)
        // under anim_full_idle, whereas anim_idle only contains the stem track.
        if (fileName.equals("PeaShooter.reanim") || fileName.equals("SnowPea.reanim")
                || fileName.equals("PeaShooterSingle.reanim")) {
            findClip("idle", "anim_full_idle");
        } else if (fileName.equalsIgnoreCase("Wallnut.reanim")) {
            clips.put("idle", new Clip(0, frameCount)); // Full loop with eye-rolls, blinks, and twitches
        } else if (fileName.equalsIgnoreCase("CherryBomb.reanim")) {
            findClip("explode", "anim_explode");
            findClip("idle", "anim_idle");
        }
    }

    public static synchronized Reanim load(String fileName) {
        if (CACHE.containsKey(fileName)) return CACHE.get(fileName);
        try {
            Reanim animation = new Reanim(fileName);
            CACHE.put(fileName, animation);
            return animation;
        } catch (Exception e) {
            LOGGER.warning("Could not load " + fileName + ": " + e.getMessage());
            CACHE.put(fileName, null);
            return null;
        }
    }

    private void findClip(String alias, String markerName) {
        for (Track track : tracks) {
            if (!track.name.equals(markerName)) continue;
            int start = 0;
            boolean foundStart = false;
            for (int frame = 0; frame < frameCount; frame++) {
                if (track.poses[frame].visible && !foundStart) {
                    start = frame;
                    foundStart = true;
                } else if (foundStart && !track.poses[frame].visible) {
                    clips.put(alias, new Clip(start, frame));
                    return;
                }
            }
            if (foundStart) clips.put(alias, new Clip(start, frameCount));
            return;
        }
    }

    public int durationTicks(String clipName) {
        Clip clip = clips.get(clipName);
        if (clip == null) return 0;
        if ("shoot".equals(clipName)) {
            // Snappy shoot animation duration in game ticks (at 60 FPS, ~0.40s)
            // Leaves plenty of time (~36 ticks) for idle sway before the next shot.
            return 24;
        }
        if ("bite".equals(clipName)) {
            return 25;
        }
        if ("swallow".equals(clipName)) {
            return 20;
        }
        if ("explode".equals(clipName)) {
            return 23;
        }
        return Math.max(1, (int) Math.ceil((clip.end - clip.start) * Constants.FPS / (double) fps));
    }

    private Track findTrack(String name) {
        for (Track track : tracks) {
            if (track.name.equals(name)) return track;
        }
        return null;
    }

    /** Draws an 80x80 plant rig in a tile, or a 100x120 zombie rig at a waypoint. */
    public void draw(Graphics2D graphics, float x, float y, float scale, String clipName,
                     double elapsedTicks, boolean zombie) {
        draw(graphics, x, y, scale, clipName, elapsedTicks, elapsedTicks, zombie);
    }

    /** Draws with separate action clock and background idle clock (keeps stem swaying during shoot). */
    public void draw(Graphics2D graphics, float x, float y, float scale, String clipName,
                     double actionTicks, double idleTicks, boolean zombie) {
        Clip clip = clips.get(clipName);
        if (clip == null) clip = clips.get("idle");
        if (clip == null) return;
        int length = clip.end - clip.start;
        if (length <= 0) return;

        double phase;
        if ("shoot".equals(clipName) || "bite".equals(clipName) || "swallow".equals(clipName) || "explode".equals(clipName)) {
            int duration = durationTicks(clipName);
            phase = duration > 0 ? (actionTicks / (double) duration) * length : 0;
            if (phase >= length) phase = length - 0.001;
            if (phase < 0) phase = 0;
        } else {
            phase = (actionTicks * fps / Constants.FPS) % length;
            if (phase < 0) phase += length;
        }

        int first = clip.start + (int) phase;
        int second = clip.start + (((int) phase + 1) % length);
        double blend = phase - Math.floor(phase);

        Clip idleClip = clips.get("idle");
        int idleLen = idleClip != null ? idleClip.end - idleClip.start : 0;
        double idlePhase = idleLen > 0 ? (idleTicks * fps / Constants.FPS) % idleLen : 0;
        if (idlePhase < 0) idlePhase += idleLen;
        int idleFirst = idleClip != null ? idleClip.start + (int) idlePhase : 0;
        int idleSecond = idleClip != null ? idleClip.start + (((int) idlePhase + 1) % idleLen) : 0;
        double idleBlend = idlePhase - Math.floor(idlePhase);

        // Compute stem sway offset if shoot animation is active on a plant with a swaying stem (e.g. Peashooter, SnowPea).
        // Find the actual neck track (stalk_top) so the head and neck stay attached with zero drift.
        double swayX = 0;
        double swayY = 0;
        Track stemTrack = findTrack("stalk_top");
        if (stemTrack == null) stemTrack = findTrack("anim_stem");
        if ("shoot".equals(clipName) && idleClip != null && stemTrack != null && idleLen > 0) {
            Pose sa = stemTrack.poses[idleFirst];
            Pose sb = stemTrack.poses[idleSecond];
            double curStemX = lerp(sa.x, sb.x, idleBlend);
            double curStemY = lerp(sa.y, sb.y, idleBlend);
            Pose baseStem = stemTrack.poses[idleClip.start];
            swayX = curStemX - baseStem.x;
            swayY = curStemY - baseStem.y;
        }

        // Smooth cross-fade transition factor at the start and end of shooting to eliminate any sudden snap
        double blendWeight = 1.0;
        if ("shoot".equals(clipName)) {
            int duration = durationTicks("shoot");
            double inF = Math.min(1.0, actionTicks / 3.0);
            double outF = Math.min(1.0, (duration - actionTicks) / 3.0);
            blendWeight = Math.max(0.0, Math.min(inF, outF));
        }

        Graphics2D root = (Graphics2D) graphics.create();
        root.translate(x, y);
        root.scale(scale, scale);
        try {
            for (Track track : tracks) {
                if (zombie && !isPlainZombiePart(track.name)) continue;
                Pose a = track.poses[first];
                Pose b = track.poses[second];
                double trackBlend = blend;

                // When playing an action clip (like shoot) that only animates head parts,
                // fallback to the idle clip for body parts (like stem/leaves) so they remain visible and sway.
                boolean isActionPart = (a.visible && a.image != null);
                if (!isActionPart && idleClip != null && clip != idleClip) {
                    if (idleLen > 0) {
                        Pose ia = track.poses[idleFirst];
                        if (ia.visible && ia.image != null) {
                            a = ia;
                            b = track.poses[idleSecond];
                            trackBlend = idleBlend;
                        }
                    }
                }

                if (!a.visible || a.image == null) continue;
                Image image = partImage(a.image);
                if (image == null) continue;
                if (!b.visible || !a.image.equals(b.image)) b = a;
                Graphics2D part = (Graphics2D) root.create();
                try {
                    double px = lerp(a.x, b.x, trackBlend);
                    double py = lerp(a.y, b.y, trackBlend);
                    double sx = lerp(a.sx, b.sx, trackBlend);
                    double sy = lerp(a.sy, b.sy, trackBlend);
                    double kx = lerpAngle(a.kx, b.kx, trackBlend);
                    double ky = lerpAngle(a.ky, b.ky, trackBlend);

                    // If this is a head part during shoot, attach it to the swaying neck and cross-fade smoothly with idle
                    if (isActionPart && "shoot".equals(clipName) && idleClip != null && idleLen > 0) {
                        Pose ia = track.poses[idleFirst];
                        Pose ib = track.poses[idleSecond];
                        if (ia.visible && ia.image != null) {
                            Pose aStart = (clip != null && clip.start < track.poses.length) ? track.poses[clip.start] : a;
                            double partRecoilX = px - aStart.x;
                            double partRecoilY = py - aStart.y;

                            double idlePx = lerp(ia.x, ib.x, idleBlend);
                            double idlePy = lerp(ia.y, ib.y, idleBlend);
                            double idleSx = lerp(ia.sx, ib.sx, idleBlend);
                            double idleSy = lerp(ia.sy, ib.sy, idleBlend);
                            double idleKx = lerpAngle(ia.kx, ib.kx, idleBlend);
                            double idleKy = lerpAngle(ia.ky, ib.ky, idleBlend);

                            // Anchor head parts directly to the idle base pose plus recoil delta
                            px = idlePx + partRecoilX * blendWeight;
                            py = idlePy + partRecoilY * blendWeight;
                            sx = lerp(idleSx, sx, blendWeight);
                            sy = lerp(idleSy, sy, blendWeight);
                            kx = lerpAngle(idleKx, kx, blendWeight);
                            ky = lerpAngle(idleKy, ky, blendWeight);
                        } else {
                            px = px + swayX;
                            py = py + swayY;
                        }
                    } else if (!isActionPart && "shoot".equals(clipName) && ("stalk_top".equals(track.name) || "anim_stem".equals(track.name))) {
                        // When head recoils during shoot, let the neck (stalk_top) flex naturally with the head
                        Track faceTrack = findTrack("anim_face");
                        if (faceTrack != null && clip != null && clip.start < faceTrack.poses.length) {
                            Pose headStart = faceTrack.poses[clip.start];
                            Pose headCurA = faceTrack.poses[first];
                            Pose headCurB = faceTrack.poses[second];
                            double headCurX = lerp(headCurA.x, headCurB.x, trackBlend);
                            double headCurY = lerp(headCurA.y, headCurB.y, trackBlend);
                            double headRecoilX = headCurX - headStart.x;
                            double headRecoilY = headCurY - headStart.y;
                            px += headRecoilX * blendWeight;
                            py += headRecoilY * blendWeight;
                        }
                    }

                    double radX = Math.toRadians(kx);
                    double radY = Math.toRadians(ky);
                    double m00 = sx * Math.cos(radX);
                    double m10 = sx * Math.sin(radX);
                    double m01 = -sy * Math.sin(radY);
                    double m11 = sy * Math.cos(radY);
                    part.transform(new AffineTransform(m00, m10, m01, m11, px, py));
                    float alpha = (float) Math.max(0, Math.min(1, lerp(a.alpha, b.alpha, trackBlend)));
                    part.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    part.drawImage(image, 0, 0, null);
                } finally {
                    part.dispose();
                }
            }
        } finally {
            root.dispose();
        }
    }

    private static boolean isPlainZombiePart(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return !lower.contains("flag") && !lower.contains("screendoor")
                && !lower.contains("duckytube") && !lower.contains("whitewater")
                && !lower.contains("mustache") && !lower.contains("tongue")
                && !lower.contains("hair") && !lower.contains("cone")
                && !lower.contains("bucket");
    }

    private static double lerp(double a, double b, double fraction) {
        return a + (b - a) * fraction;
    }

    private static double lerpAngle(double a, double b, double fraction) {
        double diff = (b - a) % 360.0;
        if (diff > 180.0) diff -= 360.0;
        else if (diff < -180.0) diff += 360.0;
        return a + diff * fraction;
    }

    private static synchronized Image partImage(String key) {
        if (PART_IMAGES.containsKey(key)) return PART_IMAGES.get(key);
        if (imageNames == null) {
            imageNames = new HashMap<>();
            File[] files = new File(ROOT).listFiles((folder, name) -> name.toLowerCase(Locale.ROOT).endsWith(".png"));
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    imageNames.put(normalize(name.substring(0, name.length() - 4)), name);
                }
            }
        }
        String name = imageNames.get(normalize(key.replace("IMAGE_REANIM_", "")));
        Image image = name == null ? null : ImageLoader.getImage(ROOT + name);
        PART_IMAGES.put(key, image);
        return image;
    }

    private static String normalize(String value) {
        return value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    private static final class Track {
        final String name;
        final Pose[] poses;
        Track(String name, Pose[] poses) { this.name = name; this.poses = poses; }
    }

    private static final class Clip {
        final int start;
        final int end;
        Clip(int start, int end) { this.start = start; this.end = end; }
    }

    private static final class Pose {
        double x, y, sx = 1, sy = 1, kx, ky, alpha = 1;
        boolean visible = true;
        String image;

        Pose with(Element element) {
            Pose next = new Pose();
            next.x = x; next.y = y; next.sx = sx; next.sy = sy;
            next.kx = kx; next.ky = ky; next.alpha = alpha;
            next.visible = visible; next.image = image;
            for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getNodeType() != Node.ELEMENT_NODE) continue;
                String value = child.getTextContent();
                switch (child.getNodeName()) {
                    case "x": next.x = Double.parseDouble(value); break;
                    case "y": next.y = Double.parseDouble(value); break;
                    case "sx": next.sx = Double.parseDouble(value); break;
                    case "sy": next.sy = Double.parseDouble(value); break;
                    case "kx": next.kx = Double.parseDouble(value); break;
                    case "ky": next.ky = Double.parseDouble(value); break;
                    case "a": next.alpha = Double.parseDouble(value); break;
                    case "f": next.visible = !"-1".equals(value); break;
                    case "i": next.image = value; break;
                    default: break;
                }
            }
            return next;
        }
    }
}
