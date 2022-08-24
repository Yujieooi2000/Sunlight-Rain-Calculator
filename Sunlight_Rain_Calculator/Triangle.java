import java.lang.Object;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/*
    Public class Triangle
    Constructs triangles to be used in the RayTriangle Intersection
    It is constructed as a container for Three Poin3d objects for the three points of the triangle
    And get methods for each point
 */
public class Triangle {


    Point3d[] points = new Point3d[3];

    public Triangle(Point3d point1, Point3d point2, Point3d point3) {
        points[0] = point1;
        points[1] = point2;
        points[2] = point3;
    }

    public Point3d getPointOne() {
        return points[0];
    }

    public Point3d getPointTwo() {
        return points[1];
    }

    public Point3d getPointThree() {
        return points[2];
    }

    public Point3d intersects(Point3d from, Vector3d dir) {
        Ray ray = new Ray(from, dir);
        return intersectRayTriangle(ray, this);
    }

    public static final double EPSILON  = 0.01;

    /*
    Ray Triangle Intersection algorithm

    taken from wiki, before the java style one was added
    this is the C++ version adapted for java
    */

    public static Point3d intersectRayTriangle(Ray R, Triangle T) {
        Point3d    I;
        Vector3d    e1, e2, cross1, cross2;
        Vector3d    dir, w0, distance,origin,distance1;
        double     determinant,uParam,invDet, vParam, b;

        dir = new Vector3d(R.getDirection());
        origin = new Vector3d (R.getOrigin());

        //Find vectors for two edges sharing V1
        e1 = new Vector3d(T.getPointTwo());
        e1.sub(T.getPointOne());
        e2 = new Vector3d(T.getPointThree());
        e2.sub(T.getPointOne());

        //Begin calculating determinant - also used to calculate u parameter
        cross1 = new Vector3d(); // cross product
        cross1.cross(dir, e2); //P

        //if determinant is near zero, ray lies in plane of triangle or ray is parallel to plane of triangle
        determinant = cross1.dot(e1);

        if (determinant > (EPSILON *(-1)) && determinant<EPSILON ){
            return null;
        }

        //calculate distance from V1 to ray origin
        distance1=new Vector3d(R.getOrigin()); //O for origin
        distance1.sub(T.getPointOne()); // T

        invDet = 1/determinant;
        //Calculate u parameter and test bound
        uParam = (distance1.dot(cross1))*invDet;
        //The intersection lies outside of the triangle
        if(uParam<0 || uParam>1){
            return null;
        }

        //Prepare to test v parameter
        cross2 = new Vector3d();
        cross2.cross(distance1,e1); //Q

        //Calculate V parameter and test bound
        vParam = cross2.dot(distance1)*invDet;
        //The intersection lies outside of the triangle
        if(vParam<0 || uParam+vParam >1){
            return null;
        }

        b = (e2.dot(cross2))*invDet;
        I = new Point3d(b,b,b);

        if(b > EPSILON ){
            return I;
        }else{
            return null;
        }

    }
}