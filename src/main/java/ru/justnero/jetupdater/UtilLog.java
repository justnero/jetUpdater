package ru.justnero.jetupdater;


import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;

public class UtilLog {
    
    public static boolean infoEnabled = true;
    public static boolean warningEnabled = true;
    public static boolean errorEnabled = true;
    public static boolean debugEnabled = false;
    public static PrintStream out = System.out;
    
    public static void info(String... list) {
        if(infoEnabled) {
            out.print("[INFO] ");
            println(list);
        }
    }
    
    public static void warning(String... list) {
        if(warningEnabled) {
            out.print("[WARNING] ");
            println(list);
        }
    }
    
    public static void error(String... list) {
        if(errorEnabled) {
            out.print("[ERROR] ");
            println(list);
        }
    }
    
    public static void error(Throwable ex) {
        if(errorEnabled) {
            StringWriter error = new StringWriter();
            ex.printStackTrace(new PrintWriter(error));
            Scanner scan = new Scanner(error.toString());
            scan.useDelimiter("\n");
            while(scan.hasNext()) {
                println("[ERROR] ",scan.nextLine());
            }
        }
    }
    
    public static void debug(String... list) {
        if(debugEnabled) {
            out.print("[DEBUG] ");
            println(list);
        }
    }
    
    public static void debug(Throwable ex) {
        if(debugEnabled) {
            StringWriter error = new StringWriter();
            ex.printStackTrace(new PrintWriter(error));
            Scanner scan = new Scanner(error.toString());
            scan.useDelimiter("\n");
            while(scan.hasNext()) {
                println("[DEBUG] ",scan.nextLine());
            }
        }
    }
    
    public static void println(String... list) {
        for(String part : list) {
            out.print(part);
        }
        out.println();
    }
    
    
}
