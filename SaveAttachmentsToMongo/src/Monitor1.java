import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

//Running at 91 server used for only generating list of documents with missing attachments 

public class Monitor1 {

        public static void main(String[] args) {
                try {
                BufferedReader reader = null;
                String line = "";
                String status = "";
                Process p = null;
                ProcessBuilder pb = null;
                while(true){
                p = Runtime.getRuntime().exec("ps -ef");
                p.waitFor();
                reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                line="";
                status="stopped";
                while ((line = reader.readLine())!= null) {
                        if(line.contains("UpdateMissingList_25_06.jar")){
                           status="running";
                           break;
                        }
                }
                if(status.equalsIgnoreCase("stopped")){
                	 pb= new ProcessBuilder().command("/bin/bash" , "-c", "/opt/ais/app/applications/soar_attachment_import/run.sh");
                     pb.start();
                }
                Thread.sleep(120000);
                }
                } catch (IOException e) {
                e.printStackTrace();
                } catch(InterruptedException e) {
                e.printStackTrace();
                }
}}

