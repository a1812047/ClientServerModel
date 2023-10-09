import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
public class TesttextToJson {
    

    void txtTojson(String filename){

        //A map to store the data (id, list of this ids data as pair)
        Map<String,List<Pair>> weatherData =new HashMap<>();
        try {

            File file = new File(filename);
            if(file.length() == 0){
                throw new Error("the file is empty");
            }
            Scanner scanner = new Scanner(file);
            String key = "",value =  "", id = "";

            while(scanner.hasNextLine()){

                //get the next line
                String  S =  scanner.nextLine();

                
                if(S.startsWith("id")){
                // at first go to the line with first id. 
                    key = "id";
                    int index_of_id_value = S.indexOf(':');
                    if(index_of_id_value == -1){
                        key = "";
                        continue;
                    }
                    value = S.substring(index_of_id_value+1, S.length());
                    value = value.replaceAll(" ", "");
                    id = value;
                    break;
                }
            }
            if(key == "" || id == ""){
                scanner.close();
                throw new Error("there is no valid data in the file to be sent!");
            }

            List<Pair> currentIDweatherdata =  new ArrayList<Pair>();
            currentIDweatherdata.add(new Pair(key,value));
            
            while(scanner.hasNextLine()){
              
                String X = scanner.nextLine();
                if(X == ""){
                    continue;
                }
                if(X.startsWith("id")){
                    int index_of_id_value = X.indexOf(':');
                    if(index_of_id_value <= -1){
                       
                        continue;
                    }
                    if(!id.isEmpty()){
                        weatherData.put(id,currentIDweatherdata);
                    }
                    currentIDweatherdata  = new ArrayList<Pair>();
                    key = "id";
                    
                    value = X.substring(index_of_id_value+1, X.length());
                    value = value.replaceAll(" ", "");
                    
                    id = value;
                    currentIDweatherdata.add(new Pair(key,value));
                }else{
                    X = X.replaceAll(" ", "");
                    int index_of_colon = X.indexOf(':');
                    if(index_of_colon == -1){
                        continue;
                    }
                    key = X.substring(0, index_of_colon);
                    value = X.substring(index_of_colon+1,X.length());
                    currentIDweatherdata.add(new Pair(key,value));
                }
            }
            weatherData.put(id,currentIDweatherdata);
            
            
            //get the Map  and start storing in the json file
            int endIndex = filename.indexOf(".");
            File jsonFile = new File(filename.substring(0,endIndex)+".json");
            if(jsonFile.exists() == false){
                jsonFile.createNewFile();
            }
            FileWriter writer =  new FileWriter(jsonFile);
            writer.write("{\n");
            
            Iterator<Entry<String, List<Pair>>> iterator = weatherData.entrySet().iterator();
            Entry<String, List<Pair>> entry = null;
            while(iterator.hasNext()){
                entry  = iterator.next();
                key =  entry.getKey();
                List<Pair> thisId_sValue = entry.getValue();
                writer.append("    "+'"'+key+'"'+":{\n");
                int i = 0;
                for(i = 0; i< thisId_sValue.size()-1; i++){
                    key = thisId_sValue.get(i).getKey();
                    value = thisId_sValue.get(i).getVal();
                    writer.append("    "+"    "+'"'+key+'"'+":"+'"'+value+'"'+",\n");
                }
                key = thisId_sValue.get(i).getKey();
                value = thisId_sValue.get(i).getVal();
                writer.append("    "+"    "+'"'+key+'"'+":"+'"'+value+'"'+"\n");
                if(iterator.hasNext()){
                    writer.append("    "+"},\n");
                }else{
                    writer.append("    "+"}\n");
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
    
     public static  void main(String [] args){
        TesttextToJson unitTest = new TesttextToJson();
        unitTest.txtTojson("invalidFile1.txt");
        unitTest.txtTojson("invalidFile2.txt");
     }
}
