import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Created by rafalo on 2015-06-28.
 */
public class SendMyFiles {

    static Properties props;

    public static void main(String[] args) {
        System.out.println(args[0]+" "+args[1]);
        boolean state=false;
        if (args[0].equalsIgnoreCase("stany")) {
            state=true;
        }
        String propertiesFile = "";//c:\\szkolenie\\SubiektSynchronization\\src\\main\\resources\\ftp.properites";
        if (!args[1].isEmpty()) {
            propertiesFile=args[1];
        }
        GetDataFromSubiekt connServer = new GetDataFromSubiekt(propertiesFile);
        connServer.dbConnect(state);

        SendMyFiles sendMyFiles = new SendMyFiles();
        sendMyFiles.startFTP(propertiesFile, "*", state);

        System.out.println("END SENDING.");

    }

    public boolean startFTP(String propertiesFilename, String fileToFTP, boolean onlyStan) {

        props = new Properties();

        try {

            props.load(new FileInputStream(propertiesFilename));

            String serverAddress = props.getProperty("serverAddress").trim();
            String userId = props.getProperty("userId").trim();
            String password = props.getProperty("password").trim();
            String remoteDirectory = props.getProperty("remoteDirectory").trim();
            String localDirectory = props.getProperty("localDirectory").trim();

            List<String> results = new ArrayList<String>();
            HashMap<String,String> mapResults = new HashMap<String, String>();
            File[] files = new File(localDirectory).listFiles();

            for (File file : files) {
                if (file.isFile()) {
                    results.add(file.getName());
                    mapResults.put(file.getName(),"");
                    System.out.println(mapResults.get(file.getName()));
                }
                //if (onlyStan == false) {
                    if (file.isDirectory()) {
                        File[] filesInDirectory = new File(file.getAbsolutePath()).listFiles();
                        for (File fileIn : filesInDirectory) {
                            if (fileIn.isFile()) {
                                results.add(fileIn.getName());
                             //   System.out.println(file.getName() + File.separator + fileIn.getName());
                                mapResults.put(fileIn.getName(), file.getName());
                             //   System.out.println(mapResults.get(fileIn.getName()));
                            }
                        }
                    }
                //}
            }


            //new ftp client
            FTPClient ftp = new FTPClient();
            //try to connect
            ftp.connect(serverAddress);
            //login to server
            if (!ftp.login(userId, password)) {
                ftp.logout();
                return false;
            }
            int reply = ftp.getReplyCode();
            //FTPReply stores a set of constants for FTP reply codes.
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return false;
            }

            //enter passive mode
            ftp.enterLocalPassiveMode();
         //   ftp.setFileType(ftp.BINARY_FILE_TYPE, ftp.BINARY_FILE_TYPE);
          //  ftp.setfilet
            ftp.setFileTransferMode(ftp.BINARY_FILE_TYPE);
            ftp.setFileType(ftp.BINARY_FILE_TYPE);
            //get system name
            System.out.println("Remote system is " + ftp.getSystemType());
            //change current directory
            ftp.changeWorkingDirectory(remoteDirectory);
            System.out.println("Current directory is " + ftp.printWorkingDirectory());
            int i=0;
            int j=0;
            for (String file : results) {
                i++;
                j++;
                //get input stream
                String path=mapResults.get(file);
                InputStream input;
                File ppp = new File(localDirectory+File.separator+path+File.separator+file);
                ftp.changeWorkingDirectory(remoteDirectory+"/"+path);

                if ( (onlyStan == false) || (ftp.mlistFile(file)==null && !file.endsWith("old")) || file.endsWith("xml") || file.contains("htm")) {
                    if (i>100) {
                        System.out.println(remoteDirectory + path + ":" + localDirectory + path + "\\" + file + " i=" + i + " Rekord nr=" + j);
                        i = 0;
                    }
                    input = new FileInputStream(localDirectory+File.separator+path+File.separator+file);
                    ftp.storeFile(file, input);
                    input.close();

                }
            }

            ftp.logout();
            ftp.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }


}
