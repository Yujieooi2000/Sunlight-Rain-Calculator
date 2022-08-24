import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {
   static final String DB_URL = "jdbc:mysql://localhost:3306/dissertation";
   static final String USER = "root";
   static final String PASS = "password";
   static int[][] blocksArray = new int[5000][2];
   static int arrayInt = 0;
   static int[][] pathArray = new int[5000][2];
   static int pathArrayInt = 0;

   // & 'C:\Program Files\Java\jdk1.8.0_311\bin\java.exe' '-cp' 'C:\Users\Ngahtee\AppData\Local\Temp\cp_2o6p82dj6b3549e5fzlgnl53d.jar' 'Database' 
   public static void main(String[] args) throws ClassNotFoundException, IOException {
      // Open a connection
      try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
         Statement stmt = conn.createStatement();
      ) {	
         //for (int i = 0; i< 24;i++){
            //Double value1 = 53.38176 - (0.00001*i);
            //Double value2 = -1.48348 - (0.00001*i);
            //Double value1 = 53.38102 + (0.00001*i) ;
            //Double value2 = -1.48690;
            //String sql = "insert into map_position (obstacle_name, longitude, latitude)" + " values ('building', " + value1 +"," + value2 +");";
            //stmt.executeUpdate(sql);
         //}
         //conn.close();
            
         ResultSet rs = stmt.executeQuery("select * from dissertation.map_position;"); 
         
         while(rs.next()){
            //System.out.print(rs.getString(2) + " ");
            Double x_coor = (Double.parseDouble(rs.getString(3)) - 53.38006)* 100000;
            Double y_coor = (Double.parseDouble(rs.getString(4))*(-1) - 1.48284)* 100000;
            int x_coor_round = (int) Math.round(x_coor);
            int y_coor_round = (int) Math.round(y_coor);
            blocksArray[arrayInt][0] = x_coor_round;
            blocksArray[arrayInt][1] = y_coor_round;
            arrayInt = arrayInt + 1;
            //System.out.print(rs.getString(1));
            //System.out.print(rs.getString(3) + " ");
            //System.out.println(rs.getString(4));
         }
         conn.close();
         /**   
         for (int i = 0; i< 186;i++){
            //Double value1 = 53.38176 - (0.00001*i);
            //Double value2 = -1.48348 - (0.00001*i);
            Double value1 = 53.38007;
            Double value2 = -1.48433 - (0.00001*i);
            String sql = "insert into map_position (obstacle_name, longitude, latitude)" + " values ('building', " + value1 +"," + value2 +");";
            stmt.executeUpdate(sql);
         }
         */
         //String sql = "CREATE table map_position(" +
          //         " id INTEGER NOT NULL AUTO_INCREMENT," +
           //        " obstacle_name VARCHAR(200) NOT NULL, " + 
           //        " longitude DOUBLE NOT NULL, " + 
           //        " latitude DOUBLE NOT NULL, " + 
           //        " PRIMARY KEY ( id ))"; 
         //stmt.executeUpdate(sql);

         //System.out.println("Database connected successfully...");   	  
      } catch (SQLException e) {
         e.printStackTrace();
      } 
      Double x_coor_start = (53.38120 - 53.38006)* 100000;
      Double y_coor_start = (-1.48285*(-1) - 1.48284)* 100000;
      Double x_coor_end = (53.38070 - 53.38006)* 100000;
      Double y_coor_end = (-1.48537*(-1) - 1.48284)* 100000;
      int x_coor_start_round = (int) Math.round(x_coor_start);
      int y_coor_start_round = (int) Math.round(y_coor_start);
      int x_coor_end_round = (int) Math.round(x_coor_end);
      int y_coor_end_round = (int) Math.round(y_coor_end);
      Node initialNode = new Node(x_coor_start_round, y_coor_start_round);
      Node finalNode = new Node(x_coor_end_round, y_coor_end_round);
      int rows = 216;
      int cols = 425;
      String exposureMode = "Max";
      AStar aStar = new AStar(rows, cols, initialNode, finalNode, exposureMode);
      //int[][] blocksArray = new int[][]{{1,2},{2, 2},{1, 3},{3, 3},{1, 4},{3,4}};
      //int[][] blocksArray = new int[][]{{1,2},{2, 2},{3, 2},{1, 3},{3, 3},{1, 4},{3,4}};
      //int[][] blocksArray = new int[][]{{0,3},{1, 3}, {2, 3}, {3, 3},{4,3}};
      //int[][] blocksArray = new int[][]{{0,3},{1, 3}, {2, 3}, {3, 3}};
      //int[][] blocksArray = new int[][]{{1, 3}, {2, 3}, {3, 3}};
      aStar.setBlocks(blocksArray);
      List<Node> path = aStar.findPath();
      for (Node node : path) {
         //System.out.println(node);
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
         //System.out.println("");
         toReturn = toReturn + "\n";
      }
      fw.write(toReturn);
      fw.flush();
      fw.close();
   }
}
