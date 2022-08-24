import javax.vecmath.Point3d;
public class MapTransform2 {



    public static String[][] myString2(String[] X) {
        int stringLen=X.length;
        String[][] mResult = new String[stringLen][4];

        for (int i=0;i<stringLen;i++){
            String[] temp = X[i].split(" ");
            mResult[i]= temp.clone();
        }

        return mResult;
    }
    public static int[] numberf (String[][] darray ){
        int x = darray.length;
        int counter = 0; 

        for (int i=0;i<x;i++){
            if (darray[i][0].equals("f")){
                counter=counter+1; //we get the number of "o"
            }
        }
        int counter2=0;
        int[] oRay = new int[counter];
        for (int j=0;j<x;j++){
            if (darray[j][0].equals("f")){
                oRay[counter2]=j;
                counter2=counter2+1;
            }
        }

        return oRay;
    }

    public static int[] numberV (String[][] darray ){

        int x = darray.length;
        int counter = 0; // how many "v"
        for (int i=0;i<x;i++){
            if (darray[i][0].equals("v")){
                counter=counter+1; //we get the number of "o"
            }
        }
        int counter2=0;
        int[] oRay = new int[counter];
        for (int j=0;j<x;j++){
            if (darray[j][0].equals("v")){
                //System.out.println("True");
                oRay[counter2]=j;
                counter2=counter2+1;
            }
        }
        return oRay;
    }

    public static Point3d toPoint2(String[] Z){
        Point3d myP=new Point3d(Double.parseDouble(Z[1]),(Double.parseDouble(Z[3])),Double.parseDouble(Z[2]));
        return myP;
    }

    public static Triangle[] myTriangle2(String[][] A,int[] F, int[] V){
        Triangle[] theTriangles = new Triangle[F.length];
        int counter = 0; //this is a counter for the position of the Triangle

        for(int i=0;i<F.length;i++){
            int fPos = F[i];
            String[] strTriangle = A[fPos]; // [f] [6//4] [1//4] [5//4]

            String point1 = strTriangle[1];
            int int1 = point1.indexOf("/");
            int pos1 = Integer.parseInt(point1.substring(0,int1));
            pos1=pos1-1; // for the position in F
            int thePosition1 = V[pos1];

            String point2 = strTriangle[2];
            int int2 = point2.indexOf("/");
            int pos2 = Integer.parseInt(point2.substring(0,int2));
            pos2 = pos2-1;
            int thePosition2 = V[pos2];

            String point3 = strTriangle[3];
            int int3 = point3.indexOf("/");
            int pos3 = Integer.parseInt(point3.substring(0,int3));
            pos3= pos3-1;
            int thePosition3 = V[pos3];

            Point3d V1 = toPoint2(A[thePosition1]);
            Point3d V2 = toPoint2(A[thePosition2]);
            Point3d V3 = toPoint2(A[thePosition3]);

            Triangle theTr = new Triangle(V1,V2,V3);

            theTriangles[counter]=theTr;
            counter=counter+1;
        }
        return theTriangles;
    }
}
