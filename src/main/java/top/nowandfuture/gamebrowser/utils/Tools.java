package top.nowandfuture.gamebrowser.utils;

import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;

public class Tools {


    /**
     * s_{1}=\frac{-\mathbf{n} \cdot \mathbf{w}}{\mathbf{n} \cdot \mathbf{u}}=\frac{\mathbf{n} \cdot\left(\mathrm{V}_{0}-\mathrm{P}_{0}\right)}{\mathbf{n} \cdot\left(\mathrm{P}_{1}-\mathrm{P}_{0}\right)}=\frac{-\left(a x_{0}+b y_{0}+c z_{0}+d\right)}{\mathbf{n} \cdot \mathbf{u}}
     *
     * @param n  The normal vector of a plane
     * @param v0 A point at a plane
     * @param p0 A point out of a plane
     * @param u  The vector from p0 to a plane
     * @return The absolute location in the world coordination
     */
    public static Vector3d getAbsLocation(Vector3d n, Vector3d v0, Vector3d p0, Vector3d u) {
        double s1 = n.dotProduct(v0.subtract(p0)) / n.dotProduct(u);
        return u.scale(s1).add(p0);
    }

    /**
     * @param start  The quaternion to start rotation
     * @param end    The quaternion to end rotation
     * @param result The quaternion to output
     * @param t      Time from zero to one to slerp the rotation
     */
    public static void slerp(@Nonnull Quaternion start, @Nonnull Quaternion end, @Nonnull Quaternion result, float t) {
        float dot = start.getW() * end.getW() + start.getX() * end.getX() + start.getY() * end.getY() + start.getZ() * end.getZ();

        // If the dot product is negative, the quaternions have opposite handed-ness and slerp won't take
        // the shorter path. Fix by reversing one quaternion.
        if (dot < 0.0f) {
            end.multiply(-1);
            dot = -dot;
        }

        float k0, k1;

        // If the inputs are too close for comfort, linearly interpolate
        if (dot > 0.9995f) {
            k0 = 1.0f - t;
            k1 = t;
        } else {
            float sina = MathHelper.sqrt(1.0f - dot * dot);
            float a = (float) MathHelper.atan2(sina, dot);
            k0 = MathHelper.sin((1.0f - t) * a) / sina;
            k1 = MathHelper.sin(t * a) / sina;
        }

        start = start.copy();
        end = end.copy();
        start.multiply(k0);
        end.multiply(k1);
        result.set(start.getX() + end.getX(), start.getY() + end.getY(), start.getZ() + end.getZ(), start.getW() + end.getW());
    }

}
