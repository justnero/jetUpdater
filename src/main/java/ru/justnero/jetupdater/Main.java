package ru.justnero.jetupdater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ru.justnero.jetupdater.UtilLog.*;

public class Main {
    
    public static final int NO_ERROR = 0;
    public static final int NO_FILE_ERROR = 1;
    public static final int FILE_EXISTS_ERROR = 1;
    public static final int INPUT_ERROR = 3;
    public static final int PERMISSION_ERROR = 4;
    public static final int UNKNOWN_ERROR = 5;
    public static int bufferSize = 4096;
    public static Exception lastException = null;
    
    private static File workDir = null;
    
    public static void main(String[] args) {
        try {
            UtilLog.out = new PrintStream(new File(getWorkDir()+File.separator+"updater.log"));
        } catch(FileNotFoundException ex) {
            error("Couldn`t init file logging");
            ex.printStackTrace(System.err);
        }
        String url = "http://lz-craft.ru/public/system/jetLZ."+(getPlatform() == OS.windows ? "exe" : "jar");
        Path path = Paths.get(getWorkDir()+File.separator+"jetLZ."+(getPlatform() == OS.windows ? "exe" : "jar"));
        info("Updater inited");
        if(fileExists(path)) {
            info("File exists, removing");
            try {
                Files.delete(path);
            } catch(IOException ex) {
                error("Can`t remove file");
                error(ex);
                return;
            }
        }
        info("Download started");
        int result = downloadFile(url,path,1024);
        switch(result) {
            case FILE_EXISTS_ERROR:
                error("File was not removed");
                return;
            case UNKNOWN_ERROR:
                error("Unknown error ocured");
                error(lastException);
                return;
        }
        info("File downloaded");
        info("Starting launcher");
        try {
            Process process = null;
            if(getPlatform() == OS.windows)
                process = Runtime.getRuntime().exec("\""+path.toString()+"\"");
            else
                process = Runtime.getRuntime().exec(new String[] {
                    "java",
                    "-jar",
                    "\""+path.toString()+"\"",
                });
            if(process == null) 
                throw new Exception("Process just didn`t started");
        } catch(Exception ex) {
            error("Failed to start launcher.");
            error(ex);
        }
        info("All great");
    }
    
    public static int downloadFile(String strURL, Path path, int buffSize) {
        if(fileExists(path)) {
            return FILE_EXISTS_ERROR;
        }
        try {
            Files.createDirectories(path.getParent());
            URL connection = new URL(strURL);
            HttpURLConnection urlconn;
            urlconn = (HttpURLConnection) connection.openConnection();
            urlconn.setRequestMethod("GET");
            urlconn.connect();
            try(InputStream in = urlconn.getInputStream(); OutputStream writer = new FileOutputStream(path.toFile())) {
                byte buffer[] = new byte[buffSize == 0 ? bufferSize : buffSize];
                int c = in.read(buffer);
                while(c > 0) {
                    writer.write(buffer, 0, c);
                    c = in.read(buffer);
                }
                writer.flush();
            }
        } catch (IOException ex) {
            lastException = ex;
            debug("Can`t download file from:",strURL," to:",path.toString());
            error(ex);
            return UNKNOWN_ERROR;
        }
        return NO_ERROR;
    }
    
    public static boolean fileExists(String path) {
        return fileExists(Paths.get(path));
    }
    
    public static boolean fileExists(Path path) {
        return Files.exists(path);
    }
    
    private static File getWorkDir() {
        if(workDir != null) {
            return workDir;
        }
        return workDir = getWorkDir("jetLZ");
    }
    
    private static File getWorkDir(String applicationName) {
        String userHome = System.getProperty("user.home",".");
        File workingDirectory;
        switch (getPlatform().ordinal()) {
            case 0:
            case 1:
                workingDirectory = new File(userHome,'.'+applicationName+'/');
                break;
            case 2:
                String applicationData = System.getenv("APPDATA");
                if(applicationData != null) {
                    workingDirectory = new File(applicationData,"."+applicationName+'/');
                } else {
                    workingDirectory = new File(userHome,'.'+applicationName+'/');
                }
                break;
            case 3:
                workingDirectory = new File(userHome,"Library/Application Support/"+applicationName+'/');
                break;
            default:
                workingDirectory = new File(userHome,applicationName+'/');
        }
        if((!workingDirectory.exists()) && (!workingDirectory.mkdirs()))
            throw new RuntimeException("The working directory could not be created: "+workingDirectory);
        return workingDirectory;
    }
    
    public static OS getPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        if(osName.contains("win")) return OS.windows;
        if(osName.contains("mac")) return OS.macos;
        if(osName.contains("solaris")) return OS.solaris;
        if(osName.contains("sunos")) return OS.solaris;
        if(osName.contains("linux")) return OS.linux;
        if(osName.contains("unix")) return OS.linux;
        return OS.unknown;
    }
    
    public static enum OS {
        linux, solaris, windows, macos, unknown;
    }

}
