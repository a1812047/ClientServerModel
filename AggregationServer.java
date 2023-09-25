import java.net.ServerSocket;
import java.net.Socket;

public class AggregationServer {
   private ServerSocket serverSocket;
   private int port = 4567;
   public void startServer(int port){
      try {
         this.port = port;
         serverSocket = new ServerSocket(port);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   void connect(){
      try {
         while(true){
            Socket clientSocket = serverSocket.accept();
            ClientHandler client = new ClientHandler(clientSocket);
            Thread  t = new Thread(client);
            t.start();
         }
      } catch (Exception e) {
         e.printStackTrace();  
      }
   }


   public static void main(String[]args){
      AggregationServer A  = new AggregationServer();
      A.startServer(4567);
      A.connect();
   }
   
   
}
