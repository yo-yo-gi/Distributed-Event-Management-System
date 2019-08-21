package src.Client;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private FileWriter File;
    private BufferedWriter Write;
    public String UserID;
    private static final String ClientLogfile = "ClientLog.log";
    //private static final String ClientLogfile = "ClientLog_" + new SimpleDateFormat("yymmddmmss").format(new Date()) + ".log";
    private static final String Path =  "C:\\Users\\y_nimbho\\Desktop\\Final\\";
    private String logfile;
    public Logger() throws IOException{
        this(ClientLogfile);
        //this.WriteLog("Client Log started - " + new Date());
    }
    public Logger(String file) throws IOException{
        this.logfile = Path+file;
        this.OpenLog();
    }
    public synchronized void WriteLog(String message) throws IOException{
        try{
            OpenLog();
            if(this.UserID == null)
                Write.write(new Date() + " - " + message);
            else
                Write.write(new Date() + " - " + this.UserID + ":" + " " + message);
            Write.newLine();
            Write.close();
        }
        catch (IOException errmsg){
            System.out.println("Error in writing log file - " + logfile + "\n" +
                    " - Error details: " + errmsg);
            errmsg.printStackTrace(System.out);
        }

    }
    public synchronized void OpenLog() throws IOException{
        try{
            File = new FileWriter(logfile,true);
            Write = new BufferedWriter(File);
            /*this.WriteLog(" ");
            this.WriteLog("Client log opened");
            this.WriteLog(" ");*/
        }
        catch(IOException errmsg){
            System.out.println("Error in creating log file - " + logfile + "\n" +
                               " - Error details: " + errmsg);
        }
    }
    public synchronized void CloseLog() throws IOException{
        try{
            this.WriteLog(" ");
            this.WriteLog("Client log closed");
            this.Write.close();
        }
        catch(IOException errmsg){
            System.out.println("Error in closing log file - " + logfile + "\n" +
                               " - Error details: " + errmsg);
        }
    }

    public synchronized static void main(String[] args) throws IOException{
        Logger lg = new Logger();
        lg.CloseLog();
    }

}

