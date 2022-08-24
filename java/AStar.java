import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.*;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class AStar {
    private static int DEFAULT_HV_COST = 10; // Horizontal - Vertical Cost
    private static int DEFAULT_DIAGONAL_COST = 14;
    private int hvCost;
    private int diagonalCost;
    private Node[][] searchArea;
    private PriorityQueue<Node> openList;
    private Set<Node> closedSet;
    private Node initialNode;
    private Node finalNode;
    private String exposureMode;
    private static int[] myO2 = null;
    private static int[] myF = null;
    private static int[] myV = null;
    private static String [] testArr2 = null;
    private static String [][] testArr3 = null;
    private static Triangle[] triangles2 = null;
    final static GregorianCalendar dateTime = new GregorianCalendar();

    public AStar(int rows, int cols, Node initialNode, Node finalNode, int hvCost, int diagonalCost, String exposureMode) {
        this.hvCost = hvCost;
        this.diagonalCost = diagonalCost;
        setInitialNode(initialNode);
        setFinalNode(finalNode);
        this.exposureMode = exposureMode;
        this.searchArea = new Node[rows][cols];
        this.openList = new PriorityQueue<Node>(new Comparator<Node>() {
            @Override
            public int compare(Node node0, Node node1) {
                return Integer.compare(node0.getF(), node1.getF());
            }
        });
        setNodes();
        this.closedSet = new HashSet<>();
    }

    public AStar(int rows, int cols, Node initialNode, Node finalNode, String exposureMode) {
        this(rows, cols, initialNode, finalNode, DEFAULT_HV_COST, DEFAULT_DIAGONAL_COST, exposureMode);
    }

    private void setNodes() {
        for (int i = 0; i < searchArea.length; i++) {
            for (int j = 0; j < searchArea[0].length; j++) {
                Node node = new Node(i, j);
                node.calculateHeuristic(getFinalNode());
                this.searchArea[i][j] = node;
            }
        }
    }

    public void setBlocks(int[][] blocksArray) {
        for (int i = 0; i < blocksArray.length; i++) {
            int row = blocksArray[i][0];
            int col = blocksArray[i][1];
            setBlock(row, col);
        }
    }

    public List<Node> findPath() {
        int count = 0;
        openList.add(initialNode);
        while (!isEmpty(openList)) {
            Node currentNode = openList.poll();
            closedSet.add(currentNode);
            if (isFinalNode(currentNode)) {
                return getPath(currentNode);
            } else {
                if (count < 40 && exposureMode.equals("Max")){
                    addMaxExposureAdjacentNodes(currentNode);
                    count++;
                }else if (count < 40 && exposureMode.equals("Min")){
                    addMinExposureAdjacentNodes(currentNode);
                    count++;
                }else {
                    addAdjacentNodes(currentNode);
                }
            }
        }
        return new ArrayList<Node>();
    }

    private List<Node> getPath(Node currentNode) {
        List<Node> path = new ArrayList<Node>();
        path.add(currentNode);
        Node parent;
        while ((parent = currentNode.getParent()) != null) {
            path.add(0, parent);
            currentNode = parent;
        }
        return path;
    }

    private void addAdjacentNodes(Node currentNode) {
        addAdjacentUpperRow(currentNode);
        addAdjacentMiddleRow(currentNode);
        addAdjacentLowerRow(currentNode);
    }

    private void addMinExposureAdjacentNodes(Node currentNode) {
        int row = currentNode.getRow();
        int col = currentNode.getCol();
        List<String> mLines = new ArrayList<>();
        try {
            File myMap = new File("./Sunlight_Rain_Calculator/themap.txt");
            Scanner myReader = new Scanner(myMap);
            while (myReader.hasNextLine()){
                mLines.add(myReader.nextLine());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error has occured");
            e.printStackTrace();
        }
        String[] mLines2 = new String[mLines.size()];
        mLines2 = mLines.toArray(mLines2);
        testArr2 = mLines2;
        
        testArr3 = MapTransform2.myString2(testArr2); // actual double array
        // quick building height control
        for(int a=0;a<testArr3.length;a++){
            if(testArr3[a][0].equals("v")){
                double Switcher = Double.parseDouble(testArr3[a][2]);
                Switcher = Switcher * 0.02;
                String Swither2 = ""+Switcher;
                testArr3[a][2]=Swither2;
            }
        }

        myO2 = MapTransform.numberO(testArr3);
        myF = MapTransform2.numberf(testArr3); // this is the number of triangles
        myV = MapTransform2.numberV(testArr3); // this is the position of the points for the triangles in myF
        triangles2 = MapTransform2.myTriangle2(testArr3,myF,myV);
        int MyElev =108; //(int)MyDElev; //108 testing how this affects the azi and elev
        DecimalFormat numberFormat = new DecimalFormat("#.000000");
        Double MyLongPosUp = Double.parseDouble(numberFormat.format((double) Math.round(((double)(row - 1)/100000 + 53.38006)*100000)/100000));
        Double MyLatiPosUp = Double.parseDouble(numberFormat.format((double) Math.round((((double)col/100000 + 1.48284)*(-1))*100000)/100000));
        Double MyLongPosMid = Double.parseDouble(numberFormat.format((double) Math.round(((double)(row)/100000 + 53.38006)*100000)/100000));
        Double MyLatiPosMid = Double.parseDouble(numberFormat.format((double) Math.round((((double)col/100000 + 1.48284)*(-1))*100000)/100000));
        Double MyLongPosDown = Double.parseDouble(numberFormat.format((double) Math.round(((double)(row + 1)/100000 + 53.38006)*100000)/100000));
        Double MyLatiPosDown = Double.parseDouble(numberFormat.format((double) Math.round((((double)col/100000 + 1.48284)*(-1))*100000)/100000));

        AzimuthZenithAngle positionUp = SPA.calculateSolarPosition(
        dateTime,
        MyLatiPosUp, // latitude (degrees)
        MyLongPosUp, // longitude (degrees)
        MyElev, // elevation (m)
        85, // delta T (s)
        1010, // avg. air pressure (hPa)
        13); // avg. air temperature (°C)
        AzimuthZenithAngle positionMid = SPA.calculateSolarPosition(
        dateTime,
        MyLatiPosMid, // latitude (degrees)
        MyLongPosMid, // longitude (degrees)
        MyElev, // elevation (m)
        85, // delta T (s)
        1010, // avg. air pressure (hPa)
        13); // avg. air temperature (°C)
        AzimuthZenithAngle positionDown = SPA.calculateSolarPosition(
        dateTime,
        MyLatiPosDown, // latitude (degrees)
        MyLongPosDown, // longitude (degrees)
        MyElev, // elevation (m)
        85, // delta T (s)
        1010, // avg. air pressure (hPa)
        13); // avg. air temperature (°C)
    

        double currentZenithUp = positionUp.getZenithAngle();
        double currentAziUp = positionUp.getAzimuth();
        double currentZenithMid = positionMid.getZenithAngle();
        double currentAziMid = positionMid.getAzimuth();
        double currentZenithDown = positionDown.getZenithAngle();
        double currentAziDown = positionDown.getAzimuth();
        DecimalFormat numberFormat2 = new DecimalFormat("#.0000");
        currentZenithUp = Double.parseDouble(numberFormat2.format(currentZenithUp));
        currentAziUp = Double.parseDouble(numberFormat2.format(currentAziUp));
        currentZenithMid = Double.parseDouble(numberFormat2.format(currentZenithMid));
        currentAziMid = Double.parseDouble(numberFormat2.format(currentAziMid));
        currentZenithDown = Double.parseDouble(numberFormat2.format(currentZenithDown));
        currentAziDown = Double.parseDouble(numberFormat2.format(currentAziDown));
        double solarElevUp = 90-currentZenithUp;
        double solarElevMid = 90-currentZenithMid;
        double solarElevDown = 90-currentZenithDown;
        solarElevUp= Math.abs(Double.parseDouble(numberFormat2.format(solarElevUp)));
        solarElevMid= Math.abs(Double.parseDouble(numberFormat2.format(solarElevMid)));
        solarElevDown= Math.abs(Double.parseDouble(numberFormat2.format(solarElevDown)));
        double mapXUp=0;
        double mapYUp=0;
        double mapXMid=0;
        double mapYMid=0;
        double mapXDown=0;
        double mapYDown=0;
        double mapZ=0.05; // fix this !!!
        Double path_y_coorUp = (double) Math.round(((double)(row - 1)/100000 + 53.38006)*100000)/100000;
        Double path_x_coorUp = (double) Math.round((((double)col/100000 + 1.48284)*(-1))*100000)/100000;
        Double path_y_coorMid = (double) Math.round(((double)row/100000 + 53.38006)*100000)/100000;
        Double path_x_coorMid = (double) Math.round((((double)col/100000 + 1.48284)*(-1))*100000)/100000;
        Double path_y_coorDown = (double) Math.round(((double)(row + 1)/100000 + 53.38006)*100000)/100000;
        Double path_x_coorDown = (double) Math.round((((double)col/100000 + 1.48284)*(-1))*100000)/100000;
        mapXUp = (path_x_coorUp+1.48954)*(1556.66-(2866.9*(path_x_coorUp+1.48311)))-5.0;
        mapXUp = Double.parseDouble(numberFormat.format(mapXUp));
        mapYUp = (((46542.4*(path_y_coorUp-53.38))+2558.64)*(path_y_coorUp-53.3819))+2.4;
        mapYUp = Double.parseDouble(numberFormat.format(mapYUp));
        mapXMid = (path_x_coorMid+1.48954)*(1556.66-(2866.9*(path_x_coorMid+1.48311)))-5.0;
        mapXMid = Double.parseDouble(numberFormat.format(mapXMid));
        mapYMid = (((46542.4*(path_y_coorMid-53.38))+2558.64)*(path_y_coorMid-53.3819))+2.4;
        mapYMid = Double.parseDouble(numberFormat.format(mapYMid));
        mapXDown = (path_x_coorDown+1.48954)*(1556.66-(2866.9*(path_x_coorDown+1.48311)))-5.0;
        mapXDown = Double.parseDouble(numberFormat.format(mapXDown));
        mapYDown = (((46542.4*(path_y_coorDown-53.38))+2558.64)*(path_y_coorDown-53.3819))+2.4;
        mapYDown = Double.parseDouble(numberFormat.format(mapYDown));
        Point3d mapPositionUp = new Point3d(mapXUp,mapYUp,mapZ);
        Point3d mapPositionMid = new Point3d(mapXMid,mapYMid,mapZ);
        Point3d mapPositionDown = new Point3d(mapXDown,mapYDown,mapZ);
        
        // without radians
        double xRayUp=Math.sin(currentAziUp);
        double yRayUp=Math.cos(currentAziUp);
        double zRayUp=Math.abs(Math.tan(solarElevUp)); //Math.abs
        double xRayMid=Math.sin(currentAziMid);
        double yRayMid=Math.cos(currentAziMid);
        double zRayMid=Math.abs(Math.tan(solarElevMid)); //Math.abs
        double xRayDown=Math.sin(currentAziDown);
        double yRayDown=Math.cos(currentAziDown);
        double zRayDown=Math.abs(Math.tan(solarElevDown)); //Math.abs

        //with radians
        double xRayDUp =Math.sin( Math.toRadians(currentAziUp));
        double yRayDUp =Math.cos( Math.toRadians(currentAziUp));
        double zRayDUp =Math.abs(Math.tan(Math.toRadians(solarElevUp))); //Math.abs
        double xRayDMid =Math.sin( Math.toRadians(currentAziMid));
        double yRayDMid =Math.cos( Math.toRadians(currentAziMid));
        double zRayDMid =Math.abs(Math.tan(Math.toRadians(solarElevMid))); //Math.abs
        double xRayDDown =Math.sin( Math.toRadians(currentAziDown));
        double yRayDDown =Math.cos( Math.toRadians(currentAziDown));
        double zRayDDown =Math.abs(Math.tan(Math.toRadians(solarElevDown))); //Math.abs

        //Vector3d rayDirection = new Vector3d(mapX,-mapY,mapZ);
        Vector3d rayDirectionUp = new Vector3d(xRayDUp,yRayDUp,zRayDUp);
        Vector3d rayDirection2Up = new Vector3d(xRayUp,yRayUp,zRayUp);
        Vector3d rayDirectionMid = new Vector3d(xRayDMid,yRayDMid,zRayDMid);
        Vector3d rayDirection2Mid = new Vector3d(xRayMid,yRayMid,zRayMid);
        Vector3d rayDirectionDown = new Vector3d(xRayDDown,yRayDDown,zRayDDown);
        Vector3d rayDirection2Down = new Vector3d(xRayDown,yRayDown,zRayDown);

        Ray mainRayUp = new Ray(mapPositionUp,rayDirectionUp);
        Ray secondRayUp = new Ray(mapPositionUp,rayDirection2Up);
        Ray mainRayMid = new Ray(mapPositionMid,rayDirectionMid);
        Ray secondRayMid = new Ray(mapPositionMid,rayDirection2Mid);
        Ray mainRayDown = new Ray(mapPositionDown,rayDirectionDown);
        Ray secondRayDown = new Ray(mapPositionDown,rayDirection2Down);

        int smallArray1Up = 0;
        int smallArray2Up = 0;
        int smallArray1Mid = 0;
        int smallArray2Mid = 0;
        int smallArray1Down = 0;
        int smallArray2Down = 0;
        //triangles2
        for(int a=0;a<triangles2.length;a++){
            Point3d type1 = Triangle.intersectRayTriangle(mainRayUp,triangles2[a]);
            Point3d type2 = Triangle.intersectRayTriangle(secondRayUp,triangles2[a]);
            if(type1!=null){
                smallArray1Up=smallArray1Up+1;
            }
            if(type2!=null){
                smallArray2Up=smallArray2Up+1;
            }
        }
        for(int a=0;a<triangles2.length;a++){
            Point3d type1 = Triangle.intersectRayTriangle(mainRayMid,triangles2[a]);
            Point3d type2 = Triangle.intersectRayTriangle(secondRayMid,triangles2[a]);
            if(type1!=null){
                smallArray1Mid=smallArray1Mid+1;
            }
            if(type2!=null){
                smallArray2Mid=smallArray2Mid+1;
            }
        }
        for(int a=0;a<triangles2.length;a++){
            Point3d type1 = Triangle.intersectRayTriangle(mainRayDown,triangles2[a]);
            Point3d type2 = Triangle.intersectRayTriangle(secondRayDown,triangles2[a]);
            if(type1!=null){
                smallArray1Down=smallArray1Down+1;
            }
            if(type2!=null){
                smallArray2Down=smallArray2Down+1;
            }
        }
        // Finding minimum exposure
        if(smallArray1Up==0 && smallArray2Up>=2 && !getSearchArea()[row - 1][col].isBlock() && (row - 1 < getSearchArea().length)) {
            addAdjacentUpperRow(currentNode);
            addAdjacentMiddleRow(currentNode);
        }
        else if(smallArray1Down==0 && smallArray2Down>=2 && !getSearchArea()[row + 1][col].isBlock() && (row + 1 < getSearchArea().length)) {
            addAdjacentLowerRow(currentNode);
            addAdjacentUpperRow(currentNode);
        }
        else if(smallArray1Mid==0 && smallArray2Mid>=2) {
            addAdjacentMiddleRow(currentNode);
            addAdjacentLowerRow(currentNode);
        }
        else if(smallArray1Up>0 && smallArray2Up>1 && !getSearchArea()[row - 1][col].isBlock() && (row - 1 < getSearchArea().length)) {
            addAdjacentUpperRow(currentNode);
            addAdjacentMiddleRow(currentNode);
        }
        else if(smallArray1Down>0 && smallArray2Mid>1 && !getSearchArea()[row + 1][col].isBlock() && (row + 1 < getSearchArea().length)) {
            addAdjacentLowerRow(currentNode);
            addAdjacentUpperRow(currentNode);
        }
        else if(smallArray1Mid>0 && smallArray2Mid>1) {
            addAdjacentMiddleRow(currentNode);
            addAdjacentLowerRow(currentNode);
        }
        else{
            addAdjacentUpperRow(currentNode);
            addAdjacentMiddleRow(currentNode);
            addAdjacentLowerRow(currentNode);
        }
    }

    private void addMaxExposureAdjacentNodes(Node currentNode) {
        int row = currentNode.getRow();
        int col = currentNode.getCol();
        List<String> mLines = new ArrayList<>();
        try {
            File myMap = new File("./Sunlight_Rain_Calculator/themap.txt");
            Scanner myReader = new Scanner(myMap);
            while (myReader.hasNextLine()){
                mLines.add(myReader.nextLine());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error has occured");
            e.printStackTrace();
        }
        String[] mLines2 = new String[mLines.size()];
        mLines2 = mLines.toArray(mLines2);
        testArr2 = mLines2;
        
        testArr3 = MapTransform2.myString2(testArr2); // actual double array
        // quick building height control
        for(int a=0;a<testArr3.length;a++){
            if(testArr3[a][0].equals("v")){
                double Switcher = Double.parseDouble(testArr3[a][2]);
                Switcher = Switcher * 0.02;
                String Swither2 = ""+Switcher;
                testArr3[a][2]=Swither2;
            }
        }

        myO2 = MapTransform.numberO(testArr3);
        myF = MapTransform2.numberf(testArr3); // this is the number of triangles
        myV = MapTransform2.numberV(testArr3); // this is the position of the points for the triangles in myF
        triangles2 = MapTransform2.myTriangle2(testArr3,myF,myV);
        int MyElev =108; //(int)MyDElev; //108 testing how this affects the azi and elev
        DecimalFormat numberFormat = new DecimalFormat("#.000000");
        Double MyLongPosUp = Double.parseDouble(numberFormat.format((double) Math.round(((double)(row - 1)/100000 + 53.38006)*100000)/100000));
        Double MyLatiPosUp = Double.parseDouble(numberFormat.format((double) Math.round((((double)col/100000 + 1.48284)*(-1))*100000)/100000));
        Double MyLongPosMid = Double.parseDouble(numberFormat.format((double) Math.round(((double)(row)/100000 + 53.38006)*100000)/100000));
        Double MyLatiPosMid = Double.parseDouble(numberFormat.format((double) Math.round((((double)col/100000 + 1.48284)*(-1))*100000)/100000));
        Double MyLongPosDown = Double.parseDouble(numberFormat.format((double) Math.round(((double)(row + 1)/100000 + 53.38006)*100000)/100000));
        Double MyLatiPosDown = Double.parseDouble(numberFormat.format((double) Math.round((((double)col/100000 + 1.48284)*(-1))*100000)/100000));

        AzimuthZenithAngle positionUp = SPA.calculateSolarPosition(
        dateTime,
        MyLatiPosUp, // latitude (degrees)
        MyLongPosUp, // longitude (degrees)
        MyElev, // elevation (m)
        85, // delta T (s)
        1010, // avg. air pressure (hPa)
        13); // avg. air temperature (°C)
        AzimuthZenithAngle positionMid = SPA.calculateSolarPosition(
        dateTime,
        MyLatiPosMid, // latitude (degrees)
        MyLongPosMid, // longitude (degrees)
        MyElev, // elevation (m)
        85, // delta T (s)
        1010, // avg. air pressure (hPa)
        13); // avg. air temperature (°C)
        AzimuthZenithAngle positionDown = SPA.calculateSolarPosition(
        dateTime,
        MyLatiPosDown, // latitude (degrees)
        MyLongPosDown, // longitude (degrees)
        MyElev, // elevation (m)
        85, // delta T (s)
        1010, // avg. air pressure (hPa)
        13); // avg. air temperature (°C)
    

        double currentZenithUp = positionUp.getZenithAngle();
        double currentAziUp = positionUp.getAzimuth();
        double currentZenithMid = positionMid.getZenithAngle();
        double currentAziMid = positionMid.getAzimuth();
        double currentZenithDown = positionDown.getZenithAngle();
        double currentAziDown = positionDown.getAzimuth();
        DecimalFormat numberFormat2 = new DecimalFormat("#.0000");
        currentZenithUp = Double.parseDouble(numberFormat2.format(currentZenithUp));
        currentAziUp = Double.parseDouble(numberFormat2.format(currentAziUp));
        currentZenithMid = Double.parseDouble(numberFormat2.format(currentZenithMid));
        currentAziMid = Double.parseDouble(numberFormat2.format(currentAziMid));
        currentZenithDown = Double.parseDouble(numberFormat2.format(currentZenithDown));
        currentAziDown = Double.parseDouble(numberFormat2.format(currentAziDown));
        double solarElevUp = 90-currentZenithUp;
        double solarElevMid = 90-currentZenithMid;
        double solarElevDown = 90-currentZenithDown;
        solarElevUp= Math.abs(Double.parseDouble(numberFormat2.format(solarElevUp)));
        solarElevMid= Math.abs(Double.parseDouble(numberFormat2.format(solarElevMid)));
        solarElevDown= Math.abs(Double.parseDouble(numberFormat2.format(solarElevDown)));
        double mapXUp=0;
        double mapYUp=0;
        double mapXMid=0;
        double mapYMid=0;
        double mapXDown=0;
        double mapYDown=0;
        double mapZ=0.05; // fix this !!!
        Double path_y_coorUp = (double) Math.round(((double)(row - 1)/100000 + 53.38006)*100000)/100000;
        Double path_x_coorUp = (double) Math.round((((double)col/100000 + 1.48284)*(-1))*100000)/100000;
        Double path_y_coorMid = (double) Math.round(((double)row/100000 + 53.38006)*100000)/100000;
        Double path_x_coorMid = (double) Math.round((((double)col/100000 + 1.48284)*(-1))*100000)/100000;
        Double path_y_coorDown = (double) Math.round(((double)(row + 1)/100000 + 53.38006)*100000)/100000;
        Double path_x_coorDown = (double) Math.round((((double)col/100000 + 1.48284)*(-1))*100000)/100000;
        mapXUp = (path_x_coorUp+1.48954)*(1556.66-(2866.9*(path_x_coorUp+1.48311)))-5.0;
        mapXUp = Double.parseDouble(numberFormat.format(mapXUp));
        mapYUp = (((46542.4*(path_y_coorUp-53.38))+2558.64)*(path_y_coorUp-53.3819))+2.4;
        mapYUp = Double.parseDouble(numberFormat.format(mapYUp));
        mapXMid = (path_x_coorMid+1.48954)*(1556.66-(2866.9*(path_x_coorMid+1.48311)))-5.0;
        mapXMid = Double.parseDouble(numberFormat.format(mapXMid));
        mapYMid = (((46542.4*(path_y_coorMid-53.38))+2558.64)*(path_y_coorMid-53.3819))+2.4;
        mapYMid = Double.parseDouble(numberFormat.format(mapYMid));
        mapXDown = (path_x_coorDown+1.48954)*(1556.66-(2866.9*(path_x_coorDown+1.48311)))-5.0;
        mapXDown = Double.parseDouble(numberFormat.format(mapXDown));
        mapYDown = (((46542.4*(path_y_coorDown-53.38))+2558.64)*(path_y_coorDown-53.3819))+2.4;
        mapYDown = Double.parseDouble(numberFormat.format(mapYDown));
        Point3d mapPositionUp = new Point3d(mapXUp,mapYUp,mapZ);
        Point3d mapPositionMid = new Point3d(mapXMid,mapYMid,mapZ);
        Point3d mapPositionDown = new Point3d(mapXDown,mapYDown,mapZ);
        
        // without radians
        double xRayUp=Math.sin(currentAziUp);
        double yRayUp=Math.cos(currentAziUp);
        double zRayUp=Math.abs(Math.tan(solarElevUp)); //Math.abs
        double xRayMid=Math.sin(currentAziMid);
        double yRayMid=Math.cos(currentAziMid);
        double zRayMid=Math.abs(Math.tan(solarElevMid)); //Math.abs
        double xRayDown=Math.sin(currentAziDown);
        double yRayDown=Math.cos(currentAziDown);
        double zRayDown=Math.abs(Math.tan(solarElevDown)); //Math.abs

        //with radians
        double xRayDUp =Math.sin( Math.toRadians(currentAziUp));
        double yRayDUp =Math.cos( Math.toRadians(currentAziUp));
        double zRayDUp =Math.abs(Math.tan(Math.toRadians(solarElevUp))); //Math.abs
        double xRayDMid =Math.sin( Math.toRadians(currentAziMid));
        double yRayDMid =Math.cos( Math.toRadians(currentAziMid));
        double zRayDMid =Math.abs(Math.tan(Math.toRadians(solarElevMid))); //Math.abs
        double xRayDDown =Math.sin( Math.toRadians(currentAziDown));
        double yRayDDown =Math.cos( Math.toRadians(currentAziDown));
        double zRayDDown =Math.abs(Math.tan(Math.toRadians(solarElevDown))); //Math.abs

        //Vector3d rayDirection = new Vector3d(mapX,-mapY,mapZ);
        Vector3d rayDirectionUp = new Vector3d(xRayDUp,yRayDUp,zRayDUp);
        Vector3d rayDirection2Up = new Vector3d(xRayUp,yRayUp,zRayUp);
        Vector3d rayDirectionMid = new Vector3d(xRayDMid,yRayDMid,zRayDMid);
        Vector3d rayDirection2Mid = new Vector3d(xRayMid,yRayMid,zRayMid);
        Vector3d rayDirectionDown = new Vector3d(xRayDDown,yRayDDown,zRayDDown);
        Vector3d rayDirection2Down = new Vector3d(xRayDown,yRayDown,zRayDown);

        Ray mainRayUp = new Ray(mapPositionUp,rayDirectionUp);
        Ray secondRayUp = new Ray(mapPositionUp,rayDirection2Up);
        Ray mainRayMid = new Ray(mapPositionMid,rayDirectionMid);
        Ray secondRayMid = new Ray(mapPositionMid,rayDirection2Mid);
        Ray mainRayDown = new Ray(mapPositionDown,rayDirectionDown);
        Ray secondRayDown = new Ray(mapPositionDown,rayDirection2Down);

        int smallArray1Up = 0;
        int smallArray2Up = 0;
        int smallArray1Mid = 0;
        int smallArray2Mid = 0;
        int smallArray1Down = 0;
        int smallArray2Down = 0;
        //triangles2
        for(int a=0;a<triangles2.length;a++){
            Point3d type1 = Triangle.intersectRayTriangle(mainRayUp,triangles2[a]);
            Point3d type2 = Triangle.intersectRayTriangle(secondRayUp,triangles2[a]);
            if(type1!=null){
                smallArray1Up=smallArray1Up+1;
            }
            if(type2!=null){
                smallArray2Up=smallArray2Up+1;
            }
        }
        for(int a=0;a<triangles2.length;a++){
            Point3d type1 = Triangle.intersectRayTriangle(mainRayMid,triangles2[a]);
            Point3d type2 = Triangle.intersectRayTriangle(secondRayMid,triangles2[a]);
            if(type1!=null){
                smallArray1Mid=smallArray1Mid+1;
            }
            if(type2!=null){
                smallArray2Mid=smallArray2Mid+1;
            }
        }
        for(int a=0;a<triangles2.length;a++){
            Point3d type1 = Triangle.intersectRayTriangle(mainRayDown,triangles2[a]);
            Point3d type2 = Triangle.intersectRayTriangle(secondRayDown,triangles2[a]);
            if(type1!=null){
                smallArray1Down=smallArray1Down+1;
            }
            if(type2!=null){
                smallArray2Down=smallArray2Down+1;
            }
        }
        // Finding minimum exposure
        if(smallArray1Up==0 && smallArray2Up<2 && !getSearchArea()[row - 1][col].isBlock() && (row - 1 < getSearchArea().length)) {
            addAdjacentUpperRow(currentNode);
            addAdjacentMiddleRow(currentNode);
        }
        else if(smallArray1Down==0 && smallArray2Down<2 && !getSearchArea()[row + 1][col].isBlock() && (row + 1 < getSearchArea().length)) {
            addAdjacentLowerRow(currentNode);
            addAdjacentUpperRow(currentNode);
        }
        else if(smallArray1Mid==0 && smallArray2Mid<2) {
            addAdjacentMiddleRow(currentNode);
            addAdjacentLowerRow(currentNode);
        }
        else if(smallArray1Up==1 && smallArray2Up==0 && !getSearchArea()[row - 1][col].isBlock() && (row - 1 < getSearchArea().length)) {
            addAdjacentUpperRow(currentNode);
            addAdjacentMiddleRow(currentNode);
        }
        else if(smallArray1Down==1 && smallArray2Down==0 && !getSearchArea()[row + 1][col].isBlock() && (row + 1 < getSearchArea().length)) {
            addAdjacentLowerRow(currentNode);
            addAdjacentUpperRow(currentNode);
        }
        else if(smallArray1Mid==1 && smallArray2Mid==0) {
            addAdjacentMiddleRow(currentNode);
            addAdjacentLowerRow(currentNode);
        }
        else{
            addAdjacentUpperRow(currentNode);
            addAdjacentMiddleRow(currentNode);
            addAdjacentLowerRow(currentNode);
        }
    }

    private void addAdjacentLowerRow(Node currentNode) {
        int row = currentNode.getRow();
        int col = currentNode.getCol();
        int lowerRow = row + 1;
        if (lowerRow < getSearchArea().length) {
            checkNode(currentNode, col, lowerRow, getHvCost());
        }
    }

    private void addAdjacentMiddleRow(Node currentNode) {
        int row = currentNode.getRow();
        int col = currentNode.getCol();
        int middleRow = row;
        if (col - 1 >= 0) {
            checkNode(currentNode, col - 1, middleRow, getHvCost());
        }
        if (col + 1 < getSearchArea()[0].length) {
            checkNode(currentNode, col + 1, middleRow, getHvCost());
        }
    }

    private void addAdjacentUpperRow(Node currentNode) {
        int row = currentNode.getRow();
        int col = currentNode.getCol();
        int upperRow = row - 1;
        if (upperRow >= 0) {
            checkNode(currentNode, col, upperRow, getHvCost());
        }
    }

    private void checkNode(Node currentNode, int col, int row, int cost) {
        Node adjacentNode = getSearchArea()[row][col];
        if (!adjacentNode.isBlock() && !getClosedSet().contains(adjacentNode)) {
            if (!getOpenList().contains(adjacentNode)) {
                adjacentNode.setNodeData(currentNode, cost);
                getOpenList().add(adjacentNode);
            } else {
                boolean changed = adjacentNode.checkBetterPath(currentNode, cost);
                if (changed) {
                    // Remove and Add the changed node, so that the PriorityQueue can sort again its
                    // contents with the modified "finalCost" value of the modified node
                    getOpenList().remove(adjacentNode);
                    getOpenList().add(adjacentNode);
                }
            }
        }
    }

    private boolean isFinalNode(Node currentNode) {
        return currentNode.equals(finalNode);
    }

    private boolean isEmpty(PriorityQueue<Node> openList) {
        return openList.size() == 0;
    }

    private void setBlock(int row, int col) {
        this.searchArea[row][col].setBlock(true);
    }

    public Node getInitialNode() {
        return initialNode;
    }

    public void setInitialNode(Node initialNode) {
        this.initialNode = initialNode;
    }

    public Node getFinalNode() {
        return finalNode;
    }

    public void setFinalNode(Node finalNode) {
        this.finalNode = finalNode;
    }

    public Node[][] getSearchArea() {
        return searchArea;
    }

    public void setSearchArea(Node[][] searchArea) {
        this.searchArea = searchArea;
    }

    public PriorityQueue<Node> getOpenList() {
        return openList;
    }

    public void setOpenList(PriorityQueue<Node> openList) {
        this.openList = openList;
    }

    public Set<Node> getClosedSet() {
        return closedSet;
    }

    public void setClosedSet(Set<Node> closedSet) {
        this.closedSet = closedSet;
    }

    public int getHvCost() {
        return hvCost;
    }

    public void setHvCost(int hvCost) {
        this.hvCost = hvCost;
    }
}
