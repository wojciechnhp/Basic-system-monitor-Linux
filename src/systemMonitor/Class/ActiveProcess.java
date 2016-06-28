package systemMonitor.Class;

/**
 * Created by wojciech on 28.04.16.
 */
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ActiveProcess {
    private static int pagesize;
    private SimpleStringProperty name;
    private SimpleIntegerProperty pid;
    private SimpleLongProperty ram;
    private SimpleLongProperty vSize;
    private float procUssing;
    private long procTime;
    private long totalProcTime  = 0;
    private SimpleFloatProperty procPercent;

    public ActiveProcess(int pid, String name, long procTime){

        this.pid = new SimpleIntegerProperty(pid);
        this.name = new SimpleStringProperty(name);
        this.procTime = procTime;
        this.procPercent = new SimpleFloatProperty(0);
        this.vSize = new SimpleLongProperty(0);
        this.ram = new SimpleLongProperty(0);
        if(pagesize==0) {
            try {
                System.out.println("wczytuje pagesize");
                Process p = Runtime.getRuntime().exec("getconf PAGESIZE");
                p.waitFor();
                try (BufferedReader read = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    pagesize = Integer.parseInt(read.readLine()) / 1024;
                }
            } catch (Exception e) {
            }
        }
        System.out.println("tworze process "+name);
        String [] splitLine;
        Path file = Paths.get("/proc/stat");
        try(BufferedReader read = Files.newBufferedReader(file)){
            splitLine = read.readLine().split("\\s+");
            for(int i=1;i<=8;i++) totalProcTime += Long.parseLong(splitLine[i]);
        }catch(IOException ex) {
            System.out.println(ex);
        }
    }

    public boolean processUpdate(){
        //System.out.println("aktualizuje");
        long nowProcTime, nowTotalProcTime = 0;
        Path plik = Paths.get("/proc/"+pid.get()+"/stat");
        String [] splitLine;
        try(BufferedReader read = Files.newBufferedReader(plik)){
            splitLine = read.readLine().split("\\s+");
            nowProcTime = Long.parseLong(splitLine[13]);
            vSize.set(Long.parseLong(splitLine[22])/(1024*1024));
            ram.set((Long.parseLong(splitLine[23]) * pagesize)/1024);
        }catch(IOException ex){
            System.out.println("brak pliku usuwam proces "+name.get());
            return false;
        }
        plik = Paths.get("/proc/stat");
        try(BufferedReader read = Files.newBufferedReader(plik)){
            splitLine = read.readLine().split("\\s+");
            for(int i=1;i<=8;i++){
                nowTotalProcTime += Long.parseLong(splitLine[i]);
            }
        }catch(IOException ex){
            System.out.println("probelm prz odczycie total proc w procesie "+ex);
        }
        try{
            procUssing = 10000*(nowProcTime - procTime)/(nowTotalProcTime - totalProcTime);
        }catch(ArithmeticException aex){ }
        procPercent.set(procUssing/100);
        procTime = nowProcTime;
        totalProcTime = nowTotalProcTime;
        return true;
    }

    public float getProcPercent(){
        return procPercent.get();
    }
    public int getPid(){
        return pid.get();
    }
    public long getVSize(){
        return vSize.get();
    }
    public String getName(){
        return name.get();
    }
    public long getRam() {
        return ram.get();
    }
}
