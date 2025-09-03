package com.provismet.provihealth.util;

import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class ColourHelper {
    public static Vector3f lerpBarColour (float percentage, Vector3f start, Vector3f end, boolean shouldGradient) {
        if (shouldGradient) {
            Vector3f colour = new Vector3f();
            colour.x = MathHelper.lerp(percentage, end.x, start.x);
            colour.y = MathHelper.lerp(percentage, end.y, start.y);
            colour.z = MathHelper.lerp(percentage, end.z, start.z);
            return colour;
        }
        else return start;
    }

    public static int lerpBarColour (float percentage, int start, int end, boolean shouldGradient) {
        Vector3f lerpedVector = lerpBarColour(percentage, Vec3d.unpackRgb(start).toVector3f(), Vec3d.unpackRgb(end).toVector3f(), shouldGradient);
        int red = (int)(lerpedVector.x * 255) << 16;
        int green = (int)(lerpedVector.y * 255) << 8;
        int blue = (int)(lerpedVector.z * 255);
        return ColorHelper.fullAlpha(red | green | blue);
    }
}
