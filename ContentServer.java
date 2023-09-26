import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import java.util.Scanner;

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
public class ContentServer {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Integer LamportTime;
    private String hostname;
    private int port;

    ContentServer(String hostname, int port){
        this.hostname=hostname;
        this.port =port;
        
        File file = new File("ConnectionDataCS.txt");
        try  {
            
            FileWriter writer = new FileWriter(file);
            writer.append(hostname+":"+port);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            
            e.printStackTrace();
        }finally{
            System.out.println("New file Created:ConnectionDataCS.txt");
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
            String response =  in.readLine();
            LamportTime = Integer.parseInt(response)+1;
            
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            closeEverything();
        }
    }

    void connectAndSendData(String filename, String hostname, int port){
        
        
        //before this we assume the lamport clock sync message was  sent.
        
        
        try {
            socket = new Socket(hostname, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(),true);
            


            //FIRST, convert the most recent data  in the file "weatherData.txt"  to a json 
            //format and send this data from "currentData.json" file. 

            txtTojson(filename);// produces the required file.

            /*  NOW, WE  START THE PUT REQUEST */
            
            //SEND THE time first to the agg server
            String time = LamportTime.toString();
            
            
            //Send the  put request  message.
            out.println("PUT /weatherData.json/t="+time+" HTTP/1.1");
            out.flush();
            
            

            File file = new File("currentData.json");
            Scanner reader = new Scanner(file);
            
            while(reader.hasNextLine()){
                String a = reader.nextLine();
                out.println(a);
                //System.out.println("it is here now!!0");
                out.flush();
            }
            //sending an end of the put request. when a null is received by the Aggregation server.
            out.println("null");
            out.flush();
            
            //out.close();
            reader.close();
            while(!in.ready()){

            }
            color Color = new color();
            String response= in.readLine();
            System.out.println(Color.yellow+response+Color.reset);
            

            
            /*
             * WRITE YOUR RECEIVE OF RESPONSE CODE  HERE
             */
            
        } catch (FileNotFoundException e) {
            
            e.printStackTrace();
        } catch (Exception e) {
            
            e.printStackTrace();
        }finally{
            LamportTime += 1;
            closeEverything();
        }
    }

    /*
     * Takes in a filename and produces currentData.json
     */
    void txtTojson(String filename){

        //A map to store the data (id, list of this ids data as pair)
        Map<String,List<Pair>> weatherData =new HashMap<>();
        try {

            File file = new File(filename);
            Scanner scanner = new Scanner(file);
            String key = "",value =  "", id = "";

            while(scanner.hasNextLine()){

                //get the next line
                String  S =  scanner.nextLine();

                
                if(S.contains("id")){
                // at first go to the line with first id. 
                    key = "id";
                    int index_of_id_value = S.indexOf(':');
                    value = S.substring(index_of_id_value+1, S.length());
                    value = value.replaceAll(" ", "");
                    id = value;
                    break;
                }
            }

            List<Pair> currentIDweatherdata =  new ArrayList<Pair>();
            currentIDweatherdata.add(new Pair(key,value));
            
            while(scanner.hasNextLine()){
              
                String X = scanner.nextLine();
                if(X == ""){
                    continue;
                }
                if(X.startsWith("id")){
                    weatherData.put(id,currentIDweatherdata);
                    currentIDweatherdata  = new ArrayList<Pair>();
                    key = "id";
                    int index_of_id_value = X.indexOf(':');
                    value = X.substring(index_of_id_value+1, X.length());
                    value = value.replaceAll(" ", "");
                    id = value;
                    currentIDweatherdata.add(new Pair(key,value));
                }else{
                    X = X.replaceAll(" ", "");
                    int index_of_colon = X.indexOf(':');
                    
                    key = X.substring(0, index_of_colon);
                    value = X.substring(index_of_colon+1,X.length());
                    currentIDweatherdata.add(new Pair(key,value));
                }
            }
            weatherData.put(id,currentIDweatherdata);
            
            
            //get the Map  and start storing in the json file
            File jsonFile = new File("currentData.json");
            FileWriter writer =  new FileWriter(jsonFile);
            writer.write("{\n");
            
            Iterator<Entry<String, List<Pair>>> iterator = weatherData.entrySet().iterator();
            Entry<String, List<Pair>> entry = null;
            while(iterator.hasNext()){
                entry  = iterator.next();
                key =  entry.getKey();
                List<Pair> thisId_sValue = entry.getValue();
                writer.append('"'+key+'"'+":{\n");
                int i = 0;
                for(i = 0; i< thisId_sValue.size()-1; i++){
                    key = thisId_sValue.get(i).getKey();
                    value = thisId_sValue.get(i).getVal();
                    writer.append('"'+key+'"'+":"+'"'+value+'"'+",\n");
                }
                key = thisId_sValue.get(i).getKey();
                value = thisId_sValue.get(i).getVal();
                writer.append('"'+key+'"'+":"+'"'+value+'"'+"\n");
                if(iterator.hasNext()){
                    writer.append("},\n");
                }else{
                    writer.append("}\n");
                }
                
            }
            writer.append("}\n");

            writer.close();    
            scanner.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
        
     }

     void closeEverything(){
        try {
            if(socket.isConnected()){
                // System.out.println("Everything is closed !!!");
                socket.close();
            }
            
            in.close();
            out.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[]args) throws InterruptedException, IOException{
        color Color= new color();
        if(args.length <1  ){
            throw new IOException(Color.red+"\nProvide both the hostname and  the  port number\nThe format of the input should be serversIPaddressorname:portnumber\nFor e.g. if connecting to the localhost with port number 4567 type:"+Color.green+"java GETClient localhost:4567"+Color.reset);
        }
        //
        String [] input = args[0].split(":");
        try {
            String hostname = input[0];
            int port = Integer.parseInt(input[1]);
            ContentServer cs  = new ContentServer(hostname, port);
            
            //String filename = "weatherData1.txt";
            //cs.connectAndSendData(filename, "localhost", 4567);
            for(Integer i  =  1; i <=3 ; i++){
                String filename = "weatherData"+i.toString()+".txt";
                cs.getSYNCed();
                cs.connectAndSendData(filename, "localhost", 4567);
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            System.out.println(Color.red+"Check the input. For more info refere to the README guide:)"+Color.reset);
            e.printStackTrace();
        }
        
    }
}
