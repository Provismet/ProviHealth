package com.provismet.provihealth.util;

import net.minecraft.util.ARGB;

public class ColourHelper {
    public static int lerpBarColour (float percentage, int start, int end, boolean shouldGradient) {
        if (shouldGradient) return ARGB.srgbLerp(percentage, start, end);
        return start;
    }
}
