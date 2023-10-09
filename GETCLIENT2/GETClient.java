package GETCLIENT2;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;



class Pair{
    String key;
    String value;
    Pair(String A, String B){
        key = A;
        value = B;
    }
    void setKey(String key){
        this.key = key;
    }
    String getKey(){
        return key;
    }
    void setVal(String val){
        this.value = val;
    }
    String getVal(){
        return value;
    }
}

public class GETClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Integer LamportTime;
    private Map<String, String> DataMap = new HashMap<String, String>();
    private String hostname;
    private int port;
    private color Color;
    
    
    GETClient(String hostname, int  port){
        this.hostname=hostname;
        this.port =port;
        Color =  new color();
        File file = new File("ConnectionData.txt");
        try  {
            
            FileWriter writer = new FileWriter(file);
            writer.append(hostname+":"+port);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            
            e.printStackTrace();
        }finally{
            System.out.println("New file Created:ConnectionData.txt");
        }
    }

    void getSYNCed(){
        try {
            socket = new Socket(hostname, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(),true);

            out.println("GET LamportClock HTTP/1.1");
            out.flush();

            while(!in.ready()){
                
            }

            String  response = in.readLine();
            LamportTime = Integer.parseInt(response)+1;
            closeEverything();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void GETDATA(){
        String  message  =  null;
        try {
            socket = new Socket(hostname, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(),true);

            out.println("GET /weatherData.json/t="+LamportTime.toString()+" HTTP/1.1"+"\n");
            out.flush();
            // out.checkError();
            // System.out.println("Get request is sent!");
            
            File file = new File("copyData.json");
            FileWriter writer  = new FileWriter(file);
            while((message = in.readLine())!= null){
                if(message.startsWith("status")){
                    System.out.println(Color.green + message.split(":")[1]+Color.reset);
                }else{
                    writer.append(message+"\n");
                    writer.flush();
                }
                
            }
            writer.close();
            JSONparser();
            printDataforAll();
        } catch (Exception e) {
            e.printStackTrace();
            closeEverything();
        }
    }
    void JSONparser() {
        try {
            File file = new File("copyData.json");
            Scanner  reader = new Scanner(file);
            Stack <String> stringStack = new Stack<>();
            String key;
            String value = "\n";
            while(reader.hasNextLine()){
                String text = reader.nextLine();
                text = text.replaceAll(" ", "");
                if(text.startsWith("}")){
                    if(stringStack.size() <3){
                        break;
                    }
                    String Val = stringStack.pop();
                    
                    while(!Val.endsWith("{")){
                        Val = Val.replaceAll(",", "");
                        Val = Val.replaceAll("\"", "");
                        
                        value = Val+"\n"+value;
                        Val = stringStack.pop();
                    }
                    key = Val.split(":")[0];
                    key = key.replaceAll("\"", "");
                    
                    DataMap.put(key, value);
                    value = "";
                    
                    
                }else{
                    stringStack.push(text);
                }
            }   
            reader.close(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    void printDataforSome(String ID){
        if(DataMap.containsKey(ID)){
            System.out.println(DataMap.get(ID));
        }else{
            System.out.println("Weather Station with this ID is  not in the DATABASE");
        }
    }
    void printDataforAll(){
        for(Map.Entry<String,String> entry: DataMap.entrySet()){
            System.out.println(entry.getValue());
        }
    }
    void closeEverything(){
        try {
            if(socket.isConnected()){
                System.out.println("Everything is closed !!!");
                socket.close();
            }
            if(in.equals(null)){
                System.out.println("Everything is closed here  already!!!");
            }
            in.close();
            out.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String [] args) throws IOException {
        color Color= new color();
        if(args.length <1  ){
            
            throw new IOException(Color.red+"\nProvide both the hostname and  the  port number\nThe format of the input should be serversIPaddressorname:portnumber\nFor e.g. if connecting to the localhost with port number 4567 type:"+Color.green+"java GETClient localhost:4567"+Color.reset);
        }
        //
        String [] input = args[0].split(":");
        try {
            String hostname = input[0];
            int port = Integer.parseInt(input[1]);
            GETClient client = new GETClient(hostname, port);
            client.getSYNCed();
            client.GETDATA();
        } catch (Exception e) {
            System.out.println(Color.red+"Check the input. For more info refere to the README guide:)"+Color.reset);
            e.printStackTrace();
        }
        
        //client.printDataforAll();
    }

}
