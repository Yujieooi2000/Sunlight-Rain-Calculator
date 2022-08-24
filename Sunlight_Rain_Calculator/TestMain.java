import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Math.*;
import static java.lang.System.currentTimeMillis;
import java.lang.Object;
import java.lang.StringBuilder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Timer;
import java.util.TimerTask;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.util.TimeZone;
import java.util.Date;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner;
import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import javafx.geometry.Point3D;

class TestMain{
    /**
     *
     */
    private static final double treeBlockPerc = 0.2;
    private static final double busStopBlockPerc = 0.02;
    private static final double canopyBlockPerc = 0.15;
    private static final double treeRainBlockPerc = 0.1;
    private static final double busStopRainBlockPerc = 0;
    private static final double canopyRainBlockPerc = 0;
    private static String [] testArr2 = null;
    private static String [][] testArr3 = null;
    //private QuoteBank mQuoteBank;
    private static String sun;
    private static String rainString;
    private static double timeInSun=0;

    private static int[] myO2 = null;
    private static int[] myF = null;
    private static int[] myV = null;
    private static Triangle[] triangles2 = null;
    final static GregorianCalendar dateTime = new GregorianCalendar();

    private static Double MyLong;
    private static Double MyLati;
    private static String MyObstacle;
    private static String MyIdentify;
    private static String MyRain;

    final static int NUM_RAYS = 10;

    public static void main(String[] args) {

        List<String> mLines = new ArrayList<>();
        try {
            File myMap = new File("themap.txt");
            Scanner myReader = new Scanner(myMap);
            while (myReader.hasNextLine()){
                mLines.add(myReader.nextLine());
                //System.out.println(mLines.size());
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

        System.out.println("Enter longitude");
        Scanner myLongAns = new Scanner(System.in);
        MyLong = myLongAns.nextDouble(); // this is Y in my map
        System.out.println("Enter latitude");
        Scanner myLatiAns = new Scanner(System.in);
        MyLati = myLatiAns.nextDouble();  // this is X in my map
        System.out.println("Are you standing under something? ( Yes / No )");
        Scanner obstacleYesOrNo = new Scanner(System.in);
        MyObstacle = obstacleYesOrNo.nextLine();
        if (MyObstacle.contentEquals("Yes")){
            System.out.println("What is the obstacle? ( Tree / Canopy / Bus Stop )");
            Scanner obstacleIdentify = new Scanner(System.in);
            MyIdentify = obstacleIdentify.nextLine();
        }else {
            MyIdentify = "Nothing";
        }
        System.out.println("Is it raining now? ( Yes / No )");
        Scanner rainYesOrNo = new Scanner(System.in);
        MyRain = rainYesOrNo.nextLine();
        int MyElev =108; //(int)MyDElev; //108 testing how this affects the azi and elev
        DecimalFormat numberFormat = new DecimalFormat("#.000000");
        MyLong = Double.parseDouble(numberFormat.format(MyLong));
        MyLati = Double.parseDouble(numberFormat.format(MyLati));

        AzimuthZenithAngle position = SPA.calculateSolarPosition(
                dateTime,
                MyLati, // latitude (degrees)
                MyLong, // longitude (degrees)
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

        //Now from here on we need to transform Long and Lati into the map position
        // long is Y , lat is X
        // MyLong and MyLati
        double mapX=0;
        double mapY=0;
        double mapZ=0.05; // fix this !!!
        mapX = (MyLati+1.48954)*(1556.66-(2866.9*(MyLati+1.48311)))-5.0;
        mapX = Double.parseDouble(numberFormat.format(mapX));
        mapY = (((46542.4*(MyLong-53.38))+2558.64)*(MyLong-53.3819))+2.4;
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

        //Vector3d rayDirection = new Vector3d(mapX,-mapY,mapZ);
        Vector3d rayDirection = new Vector3d(xRayD,yRayD,zRayD);
        Vector3d rayDirection2 = new Vector3d(xRay,yRay,zRay);

        Ray mainRay = new Ray(mapPosition,rayDirection);
        Ray secondRay = new Ray(mapPosition,rayDirection2);

        int numHit = 0;
        int numMiss = 0;

        for (int i = 0; i < NUM_RAYS; ++i) {
            double latitude = mapX-(NUM_RAYS/10000000)+(i/10000000);
            for (int k = 0; k < NUM_RAYS; ++k) {
                double longitude = mapY-(NUM_RAYS/10000000)+(k/10000000);
                Point3d randomOrigin = new Point3d(latitude, longitude, mapZ);
                Ray randomRay = new Ray(randomOrigin, rayDirection2);
                for (int j = 0; j <triangles2.length; ++j) {
                    Point3d t = Triangle.intersectRayTriangle(randomRay,triangles2[j]);
                    if (t!=null) {
                        ++numHit;
                    } else {
                        ++numMiss;
                    }
                }  
            }
            
        }

        int numTests = NUM_RAYS * 50;
        double hitPerc  = ((float) numHit  / numTests) * 100.0;
        double finalExposureLevel = 100-hitPerc;
        String ansTree = "Tree";
        String ansCanopy = "Canopy";
        String ansBusStop = "Bus Stop";
        if (MyIdentify.contentEquals(ansTree)){
            finalExposureLevel = finalExposureLevel*treeBlockPerc;
        }else if (MyIdentify.contentEquals(ansCanopy)){
            finalExposureLevel = finalExposureLevel*canopyBlockPerc;
        }else if (MyIdentify.contentEquals(ansBusStop)){
            finalExposureLevel = finalExposureLevel*busStopBlockPerc;
        }else {
            finalExposureLevel = finalExposureLevel*1;
        }

        // calculation for the smaller amount of triangles, all the basic ones
        int smallArray1 = 0;
        int smallArray2 = 0;
        //triangles2
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

        sun = "";
        if(smallArray1==0 && smallArray2>=2) {sun="not ";System.out.println("HITTTTTTT");}
        else if(smallArray1==0 && smallArray2<2) {sun ="";}
        else if(smallArray1>0 && smallArray2>1) {sun = "not ";System.out.println("HITTTTTTT2");}
        else if(smallArray1==1 && smallArray2==0 ){sun="";}

        Vector3d rainDirection = new Vector3d(0,0,-mapZ);

        Ray rain = new Ray(mapPosition,rainDirection);

        // calculation for the smaller amount of triangles, all the basic ones
        int smallRainArray1 = 0;
        //triangles2
        for(int a=0;a<triangles2.length;a++){
            Point3d type1 = Triangle.intersectRayTriangle(rain,triangles2[a]);
            if(type1!=null){
                smallRainArray1=smallRainArray1+1;
            }
        }

        double finalRainExposureLevel = 100;
        rainString = "";
        if(smallArray1==0) {
            if (MyIdentify.contentEquals(ansTree)){
                rainString="slightly ";
                finalRainExposureLevel = finalRainExposureLevel*treeRainBlockPerc;
            }else if (MyIdentify.contentEquals(ansCanopy)){
                rainString="not ";
                finalRainExposureLevel = finalRainExposureLevel*canopyRainBlockPerc;
            }else if (MyIdentify.contentEquals(ansBusStop)){
                rainString="not ";
                finalRainExposureLevel = finalRainExposureLevel*busStopRainBlockPerc;
            }else {
                rainString="";
                finalRainExposureLevel = finalRainExposureLevel*1;
            }
        }
        else if(smallArray1>0) {
            rainString = "not ";
            finalRainExposureLevel = 0;
        }

        DecimalFormat timerFormat = new DecimalFormat("#.00");
        double myMinutes = Double.parseDouble(timerFormat.format(timeInSun/60));
        String msgSun ="Current position:" + System.getProperty("line.separator") +
                    "Longitude: " + MyLong + System.getProperty("line.separator") +
                    "Latitude: " + MyLati + System.getProperty("line.separator") +
                    "Map Position: "+ " x="+mapX +" y="+mapY +System.getProperty("line.separator")+
                    "Sun zenith: "+currentZenith+System.getProperty("line.separator")+
                    "Sun azimuth: "+ currentAzi +System.getProperty("line.separator")+
                    "Sun elevation: "+ solarElev +System.getProperty("line.separator")+
                    "Total intersection tests:  "+ numTests+System.getProperty("line.separator")+
                    "Hits:  "+ numHit+", "+System.getProperty("line.separator")+
                    "Sunlight Exposure Level:  "+ finalExposureLevel+", "+System.getProperty("line.separator")+
                    "You are "+sun+"exposed to the sun" + System.getProperty("line.separator")
                ;
        String msgRain ="Current position:" + System.getProperty("line.separator") +
                    "Longitude: " + MyLong + System.getProperty("line.separator") +
                    "Latitude: " + MyLati + System.getProperty("line.separator") +
                    "Map Position: "+ " x="+mapX +" y="+mapY +System.getProperty("line.separator")+
                    "Rain Exposure Level:  "+ finalRainExposureLevel+", "+System.getProperty("line.separator")+
                    "You are "+rainString+"exposed to the rain" + System.getProperty("line.separator")
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