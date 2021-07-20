package top.nowandfuture.gamebrowser.utils;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class Tools {


    /**
     * s_{1}=\frac{-\mathbf{n} \cdot \mathbf{w}}{\mathbf{n} \cdot \mathbf{u}}=\frac{\mathbf{n} \cdot\left(\mathrm{V}_{0}-\mathrm{P}_{0}\right)}{\mathbf{n} \cdot\left(\mathrm{P}_{1}-\mathrm{P}_{0}\right)}=\frac{-\left(a x_{0}+b y_{0}+c z_{0}+d\right)}{\mathbf{n} \cdot \mathbf{u}}
     * @param n The normal vector of a plane.
     * @param v0 A point at a plane.
     * @param p0 A point out of a plane.
     * @param u The vector from p0 to a plane.
     * @return The absolute location in the world coordination.
     */
    public static Vector3d getAbsLocation(Vector3d n, Vector3d v0, Vector3d p0, Vector3d u){
        double s1 = n.dotProduct(v0.subtract(p0)) / n.dotProduct(u);
        return u.scale(s1).add(p0);
    }
}
