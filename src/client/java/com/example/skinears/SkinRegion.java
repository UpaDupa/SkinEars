package com.example.skinears;

/**
 * Regions in the standard 64x64 Minecraft skin layout.
 * Source coordinates are UV starting points for the ears' cube faces.
 */
public enum SkinRegion {
    HEAD_TOP("Head top", 7, 0, 39, 0),
    HEAD_FRONT("Head front", 7, 8, 39, 8),
    HEAD_BACK("Head back", 23, 8, 55, 8),
    HEAD_LEFT("Head left", 15, 8, 47, 8),
    HEAD_RIGHT("Head right", 0, 8, 32, 8),
    TORSO_FRONT("Torso front", 19, 20, -1, -1),
    TORSO_BACK("Torso back", 31, 20, -1, -1),
    RIGHT_ARM("Right arm", 43, 20, -1, -1),
    LEFT_ARM("Left arm", 35, 52, -1, -1),
    RIGHT_LEG("Right leg", 3, 20, -1, -1),
    LEFT_LEG("Left leg", 19, 52, -1, -1);

    public final String label;
    public final int u;
    public final int v;
    public final int overlayU;
    public final int overlayV;

    SkinRegion(String label, int u, int v, int overlayU, int overlayV) {
        this.label = label;
        this.u = u;
        this.v = v;
        this.overlayU = overlayU;
        this.overlayV = overlayV;
    }

    public boolean hasOverlay() {
        return overlayU >= 0;
    }

    public static SkinRegion fromIndex(int index) {
        SkinRegion[] values = values();
        return values[Math.floorMod(index, values.length)];
    }

    public static int next(int index) {
        return Math.floorMod(index + 1, values().length);
    }
}
