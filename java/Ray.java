import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Ray {

    Point3d origin = null;
    Vector3d direction = null;

    public Ray(Point3d origin, Vector3d direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Point3d getOrigin() {
        return origin;
    }

    public Vector3d getDirection() {
        return direction;
    }

}
