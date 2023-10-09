import java.io.File;
import java.util.Date;

public class OldDataHandler implements Runnable{
    boolean filenameStartsWith_A_number(String filename){
        for (Integer i = 1;i < 10; i++){
            if(filename.startsWith(i.toString())){
                return true;
            }
        }
        return false;
    }
    @Override
    public void run() {
        try{
         File file = new File(System.getProperty("user.dir"));
         String[] filenames = file.list(); 
         for (int i = 0; i< filenames.length; i++){
             
             if(filenames[i].endsWith(".json") && filenameStartsWith_A_number(filenames[i])){
                File thisFile = new File(filenames[i]);
                long lastModifiedTime = thisFile.lastModified();
                //System.out.println("lastModified at: "+lastModifiedTime);
                Date currentDate = new Date();
                long currentTime = currentDate.getTime();
                //System.out.println("currentTime is: "+currentTime);
                long difference = currentTime - lastModifiedTime;
                //System.out.println("difference is: "+difference);
                if( difference >= 30*1000){
                    System.out.println("Removing file: "+filenames[i]);
                    thisFile.delete();
                }
             }
         }
         Thread.sleep(30*1000);
         run(); 
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
