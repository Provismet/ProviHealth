package com.provismet.provihealth.util;

import net.minecraft.util.math.ColorHelper;

public class ColourHelper {
    public static int lerpBarColour (float percentage, int start, int end, boolean shouldGradient) {
        if (shouldGradient) return ColorHelper.lerp(percentage, start, end);
        return start;
    }
}
