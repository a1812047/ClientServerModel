import java.net.ServerSocket;
import java.net.Socket;

public class AggregationServer {
   private ServerSocket serverSocket;
   private int port = 4567;

   String green = "\u001B[32m";
   String reset = "\u001B[0m";
   String red = "\u001B[31m";
   String yellow = "\u001B[33m";
   
   //allocates the port to the server  socket.
   public void startServer(int port){
      try {
         this.port = port;
         serverSocket = new ServerSocket(port);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /*
    * Opens up a new thread for each  client and content server get and put requests.
    * this  is done to  make sure PUT and GET threads can run concurrently. 
    * All the PUT requests send a SYNC msg to get the correct LAMPORT TIME from the Aggregation server.
    * Then they send the actual PUT content request. The server creates a file specific with the name being 
    * request's {LamportClockTime}.txt. These numbers are pushed into the queue.
    * Similarly, The GET request asks  for the  LamportTime First. This helps the client  to send the time of 
    * order ,  or a partial ordering. Now the server maintains a most updated file to which the server writes all the files that
    * have the name of the file less than the GETClient's LamportClock. Once the  compilation is done the file is  sent over the socket to the   client.  
    * The client can now display this json file as text.. 
    */

   void connect() throws InterruptedException{
      try {
         while(true){
            Socket clientSocket = serverSocket.accept();
            ClientHandler client = new ClientHandler(clientSocket);
            Thread  t = new Thread(client);
            t.start();
         }
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println("The server went down!!");
         System.out.println("you may retry in few seconds!!");
         System.out.print("Server restarting");
         Thread.sleep(1000);
         System.out.print(".");
         Thread.sleep(1000);
         System.out.print(".");
         Thread.sleep(1000);
         System.out.print(".");
         Thread.sleep(1000);
         System.out.println(".");
         connect();
      }
   }

   

   public static void main(String[]args) throws InterruptedException{
      AggregationServer A  = new AggregationServer();
      Integer port  = null;
      if(args.length >0){
         //check if args[0]
         
         try{
             port = Integer.parseInt(args[0]);
         }catch(Exception e){
            System.out.println("input is invalid  !!");
            e.printStackTrace();
         }finally{
            if((port != null) ){
               A.startServer(port);
               System.out.println(A.green+"AGGREGATION SERVER IS Listening ON  PORT "+port+A.reset);
               A.connect();
               
            }else{
               System.out.println(A.yellow+"try:\njava  AggregationServer {number}\nFor e.g. java AggregationServer 4567\n" + A.reset);
            }
         }
      }else{
         A.startServer(port=4567);
         System.out.println(A.green+"AGGREGATION SERVER IS Listening ON  PORT "+port+A.reset);
         A.connect();
         
      }
      
   }
   
   
}
