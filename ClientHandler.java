import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Stack;

public class ClientHandler implements Runnable{
    public static PriorityQueue<Integer> PQ = new PriorityQueue<>();
    public static String status = "201"; // starts with 201 and sends 200 later.
    public static  String illegalRequest_status = "400";
    public static String noContent_Status = "204";
    public static int counter = 0;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int ClientID;
    private color Color= new color();

    ClientHandler(Socket  socket) throws IOException{
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new  PrintWriter(socket.getOutputStream(), true);
        ClientID =   counter;
        counter += 1;
        
    }
    void closeEverything(){
        try {
            if(socket.isConnected()){
                socket.close();
            }
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    String dismantle(String inputString){
        if(inputString.isEmpty()){
            return "emptyRequest";
        }
        String [] input = inputString.split(" ");
       
        if(input[0].equals("GET") && input[1].equals("LamportClock")){
            return "SYNC";
        }else if(input[0].equals("GET") && input[1].startsWith("/weatherData")){
            int index = input[1].indexOf("=");
            String time;
            if(index == -1){
                return "INVALID FORMAT !!";
            }else{
                time = input[1].substring(index+1);
            }
            return "GET"+","+time;
        }else if(input[0].equals("PUT") && input[1].startsWith("/weatherData")){
            int index = input[1].indexOf("=");
            String time;
            if(index == -1){
                return "INVALID FORMAT !!";
            }else{
                time = input[1].substring(index+1);
            }
            return "PUT"+","+time;
        }

        return "INVALID REQUEST";
    }

    @Override
    public void run() {
        try{
            String Input;
            while(!in.ready()){

            }
            Input = in.readLine();
            System.out.println(Input);

            String meaning  = dismantle(Input);
            //System.out.println("Meaning: "+meaning);
            if(meaning.equals("SYNC")){
                //start sync  message and die
                updateLamportClock(Integer.parseInt(getCurrentLamportClock()));
                sendGLOBAL_CLOCK_TIME(in, out);
            }else if(meaning.startsWith("GET")){
                //GETCLient request
                if(status.equals("201")){
                    out.println("status:"+noContent_Status+" I  have no COntent"+Color.reset);
                    out.flush();
                    throw new IOException("there is  no data in my  DATABASE");
                }
                int order = Integer.parseInt(meaning.split(",")[1]);
                serveGET(order);
                out.println("status:"+status);
                out.flush();
            }else if(meaning.startsWith("PUT")){
                //its a put request
                Integer order = Integer.parseInt(meaning.split(",")[1]);
                String currentStatus = servePUT(order);

                // send status as ACK
                //System.out.println("i am ready to send the status");
                out.println("status:"+currentStatus);
                out.flush();
                if(currentStatus.equals("201")){
                    status = "200";
                }
            }else{
                out.println("status:"+illegalRequest_status);
                out.flush();
                throw new IOException(meaning);
            }

        }catch(Exception e){
            e.printStackTrace();
            closeEverything();
        }finally{
            closeEverything();
        }
        
    }
    void getDataIntoMymap(Map<String, String> myMap, String filename){
        try {
            
            File file = new File(filename);
            if(filename == "weatherData.json" && file.exists() == false){
                return;
            }
            Scanner  reader = new Scanner(file);
            Stack <String> stringStack = new Stack<>();
            String key;
            String value = "";
            while(reader.hasNextLine()){
                String text = reader.nextLine();
                
                if(text.startsWith("}")){
                    if(stringStack.size() <3){
                        break;
                    }
                    String Val = stringStack.pop();
                    
                    while(!Val.endsWith("{")){
                       
                        
                        value = Val+"\n"+value;
                        Val = stringStack.pop();
                    }
                    key = Val.split(":")[0];
                    key = key.replaceAll("\"", "");
                    
                    myMap.put(key, value);
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
    synchronized void serveGET(Integer order){
        try {
            //create a new PQ
            PriorityQueue<Integer> myPQ = new PriorityQueue<>();
            Map<String, String> myMap = new HashMap<>();
            synchronized(PQ){
                while(!PQ.isEmpty()  &&  PQ.peek() <= order){
                    myPQ.add(PQ.poll());
                }
            }
            if(!myPQ.isEmpty()){
                getDataIntoMymap(myMap, "weatherData.json");
            }
            while(!myPQ.isEmpty()){
                String filename = myPQ.poll().toString()+".json";
                getDataIntoMymap(myMap, filename);
            }
            //now we can just Update the weatherData.json file.
            updateDATA(myMap,"weatherData.json");
            File file= new File("weatherData.json") ;
            Scanner reader = new Scanner(file);
            
            while(reader.hasNextLine()){
                out.println(reader.nextLine());
                out.flush();
            }
            reader.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void updateDATA(Map<String, String> myMap,String filename){
        if(myMap.isEmpty()){
            return;
        }
        //updates the data at copyData.json
        try {
             //get the Map  and start storing in the json file
             File jsonFile = new File(filename);
             FileWriter writer =  new FileWriter(jsonFile);
             writer.write("{\n");
             
             Iterator<Entry<String, String>> iterator = myMap.entrySet().iterator();
             Entry<String, String> entry = null;
             while(iterator.hasNext()){
                 entry  = iterator.next();
                 String key = entry.getKey();
                 String thisId_sValue = entry.getValue();
                 writer.append("\""+key+"\":{\n");
                 writer.append(thisId_sValue);
                 
                 if(iterator.hasNext()){
                     writer.append("},\n");
                 }else{
                     writer.append("}\n");
                 }
                 
             }
             writer.append("}\n");
             writer.flush();
 
             writer.close(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    String servePUT(Integer order){
        
        try {
            
            String msg = in.readLine();

            
            if(msg.isEmpty() || msg.equals(null)){
                System.out.println(msg);
                return noContent_Status;
            }
            
            if(!msg.startsWith("{")){
                System.out.println(msg);
                return "500-Internal Server Error";
            }
            
            //create a file called "order.json"
            File file= new File(order.toString()+".json");
            FileWriter writer = new FileWriter(file);
            writer.append(msg+'\n');
            while((msg = in.readLine()) != null ){
                if(msg.equals("null")){
                    break;
                }
                writer.append(msg+'\n');
                writer.flush();
                
            }
            writer.close();
            
            synchronized(PQ){
                
                PQ.add(order);
                
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }       
        return  status;
    }
    
    void  sendGLOBAL_CLOCK_TIME(BufferedReader in,PrintWriter out) throws IOException{
        String time = getCurrentLamportClock();
        //System.out.println("Secondly, send this time to the recently connected client: "+time);
        try{
            out.println(time);
            out.flush();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    synchronized String getCurrentLamportClock(){
        String data = null;
        try {
            File myObj = new File("lamportTime.txt");
            if(!myObj.exists()){
                myObj.createNewFile();
                FileWriter writer = new FileWriter(myObj);
                writer.write("0");
                writer.close();
            }
            Scanner myReader = new Scanner(myObj);
            data = myReader.nextLine();
            Integer toSend = (Integer.parseInt(data)+1);
            //System.out.println(toSend);
            FileWriter writer = new FileWriter(myObj);
            
            writer.write(toSend.toString());
            writer.close();
            myReader.close();
            return toSend.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
    
    void updateLamportClock(int timeToCompareWith){
        File file = new File("lamportTime.txt");
        synchronized(file){
            Scanner scanner;
            try {
                scanner = new Scanner(file);
                Integer LamportTime = Integer.parseInt(scanner.nextLine());
                LamportTime = Math.max(LamportTime, timeToCompareWith)+1;
                FileWriter writer  = new FileWriter(file);
                writer.write(LamportTime.toString());
                scanner.close();
                writer.close();
            } catch (FileNotFoundException e) {
                
                e.printStackTrace();
            } catch (IOException e) {
                
                e.printStackTrace();
            }
            
        }
    }

}