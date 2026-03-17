package dev.akgamerz_790.athenaclient.hud;

public class HudConfig {
    public static boolean showFps       = true;
    public static boolean showCoords    = true;
    public static boolean showDirection = true;
    public static boolean showBiome     = true;
    public static boolean showDimension = true;
    public static boolean showArmor     = true;
    public static boolean showLight     = true;
    // Added Compass Show
    public static boolean showCompass = true;

    // Added Compass Circle Config
    public static boolean showCompassRose = true;

    // COmpass Ring Config
    public enum CompassShape { CIRCLE, SQUARE }
    public static CompassShape compassShape = CompassShape.CIRCLE;
}