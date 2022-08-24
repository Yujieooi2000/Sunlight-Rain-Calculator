import java.io.File; 
import java.io.FileNotFoundException; 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

class Main{
    static final String DB_URL = "jdbc:mysql://localhost:3306/dissertation";
    static final String USER = "root";
    static final String PASS = "password";
    static int[][] blocksArray = new int[5000][2];
    static int arrayInt = 0;
    static String[][] obstacleArray = new String[5000][3];
    static int arrayObstacleInt = 0;
    static int[][] pathArray = new int[5000][2];
    static int pathArrayInt = 0;

    private static final double treeBlockPerc = 0.2;
    private static final double busStopBlockPerc = 0.02;
    private static final double canopyBlockPerc = 0.15;
    private static final double treeRainBlockPerc = 0.1;
    private static final double busStopRainBlockPerc = 0;
    private static final double canopyRainBlockPerc = 0;
    private static Double [][] locationPoint = {{53.38120,-1.48285},{53.38070,-1.48537},{53.38110,-1.48572}};
    private static String [] testArr2 = null;
    private static String [][] testArr3 = null;

    private static int[] myO2 = null;
    private static int[] myF = null;
    private static int[] myV = null;
    private static Triangle[] triangles2 = null;
    final static GregorianCalendar dateTime = new GregorianCalendar();

    private static Double MyLong;
    private static Double MyLati;
    private static Double MyLongDes;
    private static Double MyLatiDes;
    private static String MyObstacle;
    private static Double MyObstacleLongitude;
    private static Double MyObstacleLatitude;
    private static String MyIdentify;
    private static String MyRain;

    public static void main(String[] args) throws IOException {
        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
        ) {	
            System.out.println("Database connected successfully..."); 
            ResultSet rs = stmt.executeQuery("select * from dissertation.map_position;"); 
            
            while(rs.next()){
                if (rs.getString(2).equals("building")){
                    Double x_coor = (Double.parseDouble(rs.getString(3)) - 53.38006)* 100000;
                    Double y_coor = (Double.parseDouble(rs.getString(4))*(-1) - 1.48284)* 100000;
                    int x_coor_round = (int) Math.round(x_coor);
                    int y_coor_round = (int) Math.round(y_coor);
                    blocksArray[arrayInt][0] = x_coor_round;
                    blocksArray[arrayInt][1] = y_coor_round;
                    arrayInt = arrayInt + 1;
                }else {
                    String obstacleName = rs.getString(2);
                    Double x_coor = (Double.parseDouble(rs.getString(3)) - 53.38006)* 100000;
                    Double y_coor = (Double.parseDouble(rs.getString(4))*(-1) - 1.48284)* 100000;
                    int x_coor_round = (int) Math.round(x_coor);
                    int y_coor_round = (int) Math.round(y_coor);
                    obstacleArray[arrayObstacleInt][0] = obstacleName;
                    obstacleArray[arrayObstacleInt][1] = Integer.toString(x_coor_round);
                    obstacleArray[arrayObstacleInt][2] = Integer.toString(y_coor_round);
                    arrayObstacleInt = arrayObstacleInt + 1;
                }
            }
            conn.close();  	  
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        
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
        
        testArr3 = MapTransform2.myString2(testArr2); 
        for(int a=0;a<testArr3.length;a++){
            if(testArr3[a][0].equals("v")){
                double Switcher = Double.parseDouble(testArr3[a][2]);
                Switcher = Switcher * 0.02;
                String Swither2 = ""+Switcher;
                testArr3[a][2]=Swither2;
            }
        }

        myO2 = MapTransform.numberO(testArr3);
        myF = MapTransform2.numberf(testArr3); 
        myV = MapTransform2.numberV(testArr3); 
        triangles2 = MapTransform2.myTriangle2(testArr3,myF,myV);
        
        System.out.println("Enter origin");
        Scanner myLocationAns = new Scanner(System.in);
        String myLocation = myLocationAns.nextLine();
        if (myLocation.contentEquals("A")){
            MyLong = locationPoint[0][0];
            MyLati = locationPoint[0][1];
        } else if (myLocation.contentEquals("B")){
            MyLong = locationPoint[1][0];
            MyLati = locationPoint[1][1];
        } else if (myLocation.contentEquals("C")){
            MyLong = locationPoint[2][0];
            MyLati = locationPoint[2][1];
        }
        System.out.println("Enter destination");
        Scanner myDestinationAns = new Scanner(System.in);
        String myDestination = myDestinationAns.nextLine();
        if (myDestination.contentEquals("A")){
            MyLongDes = locationPoint[0][0];
            MyLatiDes = locationPoint[0][1];
        } else if (myDestination.contentEquals("B")){
            MyLongDes = locationPoint[1][0];
            MyLatiDes = locationPoint[1][1];
        } else if (myDestination.contentEquals("C")){
            MyLongDes = locationPoint[2][0];
            MyLatiDes = locationPoint[2][1];
        }
        System.out.println("Enter the exposure mode");
        Scanner myModeAns = new Scanner(System.in);
        String myMode = myModeAns.nextLine();

        Double x_coor_start = (MyLong - 53.38006)* 100000;
        Double y_coor_start = (MyLati*(-1) - 1.48284)* 100000;
        Double x_coor_end = (MyLongDes - 53.38006)* 100000;
        Double y_coor_end = (MyLatiDes*(-1) - 1.48284)* 100000;
        int x_coor_start_round = (int) Math.round(x_coor_start);
        int y_coor_start_round = (int) Math.round(y_coor_start);
        int x_coor_end_round = (int) Math.round(x_coor_end);
        int y_coor_end_round = (int) Math.round(y_coor_end);
        Node initialNode = new Node(x_coor_start_round, y_coor_start_round);
        Node finalNode = new Node(x_coor_end_round, y_coor_end_round);
        int rows = 216;
        int cols = 425;
        String exposureMode = myMode;
        AStar aStar = new AStar(rows, cols, initialNode, finalNode, exposureMode);
        aStar.setBlocks(blocksArray);
        List<Node> path = aStar.findPath();
        for (Node node : path) {
            pathArray[pathArrayInt][0] = node.getRow();
            pathArray[pathArrayInt][1] = node.getCol();
            pathArrayInt = pathArrayInt + 1;
        }
        String toReturn = "";
        FileWriter fw = new FileWriter("file.txt");
        for (int i = 1; i < rows; i++){
            for (int j = 1; j < cols; j++){
                Boolean trueFalse = false;
                for (int row = 0; row < blocksArray.length;row++) {
                    if(blocksArray[row][0] == i && blocksArray[row][1] == j){
                        toReturn = toReturn + "口";
                        trueFalse = true;
                    }
                }
                for (int row = 0; row < pathArray.length;row++) {
                    if(pathArray[row][0] == i && pathArray[row][1] == j){
                        toReturn = toReturn + "@";
                        trueFalse = true;
                    }
                }
                if(trueFalse == false){
                    toReturn = toReturn + ">>";
                }
            }
            toReturn = toReturn + "\n";
        }
        fw.write(toReturn);
        fw.flush();
        fw.close(); 

        System.out.println("Do you have any obstacle to add? ( Yes / No )");
        Scanner obstacleYesOrNo = new Scanner(System.in);
        MyObstacle = obstacleYesOrNo.nextLine();
        if (MyObstacle.contentEquals("Yes")){
            System.out.println("What is the obstacle? ( Tree / Canopy / Bus Stop )");
            Scanner obstacleIdentify = new Scanner(System.in);
            MyIdentify = obstacleIdentify.nextLine();
            System.out.println("What is the longitude of the obstacle?");
            Scanner obstacleLongitude = new Scanner(System.in);
            MyObstacleLongitude = obstacleLongitude.nextDouble();
            System.out.println("What is the latitude of the obstacle?");
            Scanner obstacleLatitude = new Scanner(System.in);
            MyObstacleLatitude = obstacleLatitude.nextDouble();
            try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            ) {	
                String mysqldata = " insert into dissertation.map_position (obstacle_name, longitude, latitude)" + " values (?, ?, ?)";
                PreparedStatement preparedStmt = conn.prepareStatement(mysqldata);
                preparedStmt.setString (1, MyIdentify);
                preparedStmt.setDouble(2, MyObstacleLongitude);
                preparedStmt.setDouble(3, MyObstacleLatitude);
                preparedStmt.execute();
                conn.close();  	  
                System.out.println("Data is successfully added to database...");
            } catch (SQLException e) {
                e.printStackTrace();
            } 
        }else {
            MyIdentify = "Nothing";
        }
        System.out.println("Is it raining now? ( Yes / No )");
        Scanner rainYesOrNo = new Scanner(System.in);
        MyRain = rainYesOrNo.nextLine();

        //Now from here on we need to transform Long and Lati into the map position
        // long is Y , lat is X
        // MyLong and MyLati
        double numHit = 0;
        int numTest = 0;
        double numHitRain = 0;
        int numRainTest = 0;
        String ansTree = "Tree";
        String ansCanopy = "Canopy";
        String ansBusStop = "Bus Stop";

        for (int i = 0;i<pathArray.length;i++){
            if(pathArray[i][0] != 0 && pathArray[i][1] != 0){
                
                int MyElev =108; 
                DecimalFormat numberFormat = new DecimalFormat("#.000000");
                Double MyLongPos = Double.parseDouble(numberFormat.format((double) Math.round(((double)pathArray[i][0]/100000 + 53.38006)*100000)/100000));
                Double MyLatiPos = Double.parseDouble(numberFormat.format((double) Math.round((((double)pathArray[i][1]/100000 + 1.48284)*(-1))*100000)/100000));

                AzimuthZenithAngle position = SPA.calculateSolarPosition(
                dateTime,
                MyLatiPos, // latitude (degrees)
                MyLongPos, // longitude (degrees)
                MyElev, // elevation (m)
                85, // delta T (s)
                1010, // avg. air pressure (hPa)
                13); // avg. air temperature (°C)

                double currentZenith = position.getZenithAngle();
                double currentAzi = position.getAzimuth();
                DecimalFormat numberFormat2 = new DecimalFormat("#.0000");
                currentZenith = Double.parseDouble(numberFormat2.format(currentZenith));
                currentAzi = Double.parseDouble(numberFormat2.format(currentAzi));
                double solarElev = 90-currentZenith;
                solarElev= Math.abs(Double.parseDouble(numberFormat2.format(solarElev)));
                double mapX=0;
                double mapY=0;
                double mapZ=0.05;
                Double path_y_coor = (double) Math.round(((double)pathArray[i][0]/100000 + 53.38006)*100000)/100000;
                Double path_x_coor = (double) Math.round((((double)pathArray[i][1]/100000 + 1.48284)*(-1))*100000)/100000;
                mapX = (path_x_coor+1.48954)*(1556.66-(2866.9*(path_x_coor+1.48311)))-5.0;
                mapX = Double.parseDouble(numberFormat.format(mapX));
                mapY = (((46542.4*(path_y_coor-53.38))+2558.64)*(path_y_coor-53.3819))+2.4;
                mapY = Double.parseDouble(numberFormat.format(mapY));
                Point3d mapPosition = new Point3d(mapX,mapY,mapZ);
                
                // without radians
                double xRay=Math.sin(currentAzi);
                double yRay=Math.cos(currentAzi);
                double zRay=Math.abs(Math.tan(solarElev)); //Math.abs

                //with radians
                double xRayD =Math.sin( Math.toRadians(currentAzi));
                double yRayD =Math.cos( Math.toRadians(currentAzi));
                double zRayD =Math.abs(Math.tan(Math.toRadians(solarElev))); //Math.abs

                Vector3d rayDirection = new Vector3d(xRayD,yRayD,zRayD);
                Vector3d rayDirection2 = new Vector3d(xRay,yRay,zRay);

                Ray mainRay = new Ray(mapPosition,rayDirection);
                Ray secondRay = new Ray(mapPosition,rayDirection2);
                
                int smallArray1 = 0;
                int smallArray2 = 0;

                for(int a=0;a<triangles2.length;a++){
                    Point3d type1 = Triangle.intersectRayTriangle(mainRay,triangles2[a]);
                    Point3d type2 = Triangle.intersectRayTriangle(secondRay,triangles2[a]);
                    if(type1!=null){
                        smallArray1=smallArray1+1;
                    }
                    if(type2!=null){
                        smallArray2=smallArray2+1;
                    }
                }
        
                if(smallArray1==0 && smallArray2>=2) {
                    if (MyIdentify.contentEquals(ansTree)){
                        numHit = numHit+treeBlockPerc;
                        numTest++;
                    }else if (MyIdentify.contentEquals(ansCanopy)){
                        numHit = numHit+canopyBlockPerc;
                        numTest++;
                    }else if (MyIdentify.contentEquals(ansBusStop)){
                        numHit = numHit+busStopBlockPerc;
                        numTest++;
                    }else {
                        numHit = numHit+1;
                        numTest++;
                    }
                }
                else if(smallArray1==0 && smallArray2<2) {
                    numTest++;
                }
                else if(smallArray1>0 && smallArray2>1) {
                    if (MyIdentify.contentEquals(ansTree)){
                        numHit = numHit+treeBlockPerc;
                        numTest++;
                    }else if (MyIdentify.contentEquals(ansCanopy)){
                        numHit = numHit+canopyBlockPerc;
                        numTest++;
                    }else if (MyIdentify.contentEquals(ansBusStop)){
                        numHit = numHit+busStopBlockPerc;
                        numTest++;
                    }else {
                        numHit = numHit+1;
                        numTest++;
                    }
                }
                else if(smallArray1==1 && smallArray2==0 ){
                    numTest++;
                }
            }
        }

        double missPerc  = ((float) (numTest - numHit)  / numTest) * 100.0;

        for (int i = 0;i<pathArray.length;i++){
            if(pathArray[i][0] != 0 && pathArray[i][1] != 0){
                
                DecimalFormat numberFormat = new DecimalFormat("#.000000");

                double mapX=0;
                double mapY=0;
                double mapZ=0.05; 
                Double path_y_coor = (double) Math.round(((double)pathArray[i][0]/100000 + 53.38006)*100000)/100000;
                Double path_x_coor = (double) Math.round((((double)pathArray[i][1]/100000 + 1.48284)*(-1))*100000)/100000;
                mapX = (path_x_coor+1.48954)*(1556.66-(2866.9*(path_x_coor+1.48311)))-5.0;
                mapX = Double.parseDouble(numberFormat.format(mapX));
                mapY = (((46542.4*(path_y_coor-53.38))+2558.64)*(path_y_coor-53.3819))+2.4;
                mapY = Double.parseDouble(numberFormat.format(mapY));
                Point3d mapPosition = new Point3d(mapX,mapY,mapZ);
                
                Vector3d rainDirection = new Vector3d(0,0,-mapZ);

                Ray mainRain = new Ray(mapPosition,rainDirection);
                
                int smallArrayRain = 0;

                for(int a=0;a<triangles2.length;a++){
                    Point3d type1 = Triangle.intersectRayTriangle(mainRain,triangles2[a]);
                    if(type1!=null){
                        smallArrayRain=smallArrayRain+1;
                    }
                }
        
                if(smallArrayRain==0) {
                    if (MyIdentify.contentEquals(ansTree)){
                        numHitRain = numHitRain+treeRainBlockPerc;
                        numRainTest++;
                    }else if (MyIdentify.contentEquals(ansCanopy)){
                        numHitRain = numHitRain+canopyRainBlockPerc;
                        numRainTest++;
                    }else if (MyIdentify.contentEquals(ansBusStop)){
                        numHitRain = numHitRain+busStopRainBlockPerc;
                        numRainTest++;
                    }else {
                        numHitRain = numHitRain+1;
                        numRainTest++;
                    }
                }
                else if(smallArrayRain>0) {
                    numRainTest++;
                }
            }
        }

        double missRainPerc  = ((float) (numRainTest - numHitRain)  / numRainTest) * 100.0;

        String msgSun ="Origin:" + System.getProperty("line.separator") +
                    "Longitude: " + MyLong + System.getProperty("line.separator") +
                    "Latitude: " + MyLati + System.getProperty("line.separator") +
                    "Origin Map Position: "+ " x="+x_coor_start_round +" y="+y_coor_start_round +System.getProperty("line.separator")+
                    "Destination:" + System.getProperty("line.separator") +
                    "Longitude: " + MyLongDes + System.getProperty("line.separator") +
                    "Latitude: " + MyLatiDes + System.getProperty("line.separator") +
                    "Destination Map Position: "+ " x="+x_coor_end_round +" y="+y_coor_end_round +System.getProperty("line.separator")+
                    "Sunlight Exposure Level:  "+ missPerc+", "+System.getProperty("line.separator")
                ;
        String msgRain ="Origin:" + System.getProperty("line.separator") +
                    "Longitude: " + MyLong + System.getProperty("line.separator") +
                    "Latitude: " + MyLati + System.getProperty("line.separator") +
                    "Origin Map Position: "+ " x="+x_coor_start_round +" y="+y_coor_start_round +System.getProperty("line.separator")+
                    "Destination:" + System.getProperty("line.separator") +
                    "Longitude: " + MyLongDes + System.getProperty("line.separator") +
                    "Latitude: " + MyLatiDes + System.getProperty("line.separator") +
                    "Destination Map Position: "+ " x="+x_coor_end_round +" y="+y_coor_end_round +System.getProperty("line.separator")+
                    "Rain Exposure Level:  "+ missRainPerc+", "+System.getProperty("line.separator")
                ;

        if (MyRain.contentEquals("No")){
            System.out.println(msgSun);
        }else if(MyRain.contentEquals("Yes")){
            System.out.println(msgRain);
        }else {
            System.out.println("Error message obtained from raining question.");
        }

    }

}