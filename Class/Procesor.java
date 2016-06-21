package systemMonitor.Class;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Procesor {
    private String name = null;
    private int cores = 0;
    private int mhz;
    private float cpuUssing;
    private long idle;
    private long total;

    public Procesor(){
        long work;
        Path plik = Paths.get("/proc/cpuinfo");
        try(BufferedReader read = Files.newBufferedReader(plik)){
            String line;
            String splitLine[];
            while((line = read.readLine())!= null){
                splitLine = line.split(":");
                //for(String str : splitLine){
                //    System.out.print(str+";");
                //}
                if(splitLine[0].trim().equalsIgnoreCase("processor")) cores++;
                if(splitLine[0].trim().equalsIgnoreCase("model name")&&name == null){
                    name = splitLine[1].trim();
                }

            }

        } catch (IOException ex) {
            System.err.println(ex);
        }
        plik = Paths.get("/sys/devices/system/cpu/cpu1/cpufreq/scaling_max_freq");
        try(BufferedReader read = Files.newBufferedReader(plik)){
            mhz= Integer.parseInt(read.readLine());
        } catch (IOException ex) {
            System.err.println(ex);
        }
        plik = Paths.get("/proc/stat");
        try(BufferedReader read = Files.newBufferedReader(plik)){
            String line;
            String [] splitLine;
            line=read.readLine();
            splitLine = line.split("\\s+");
            //for(String str : splitLine){
            //    System.out.print(str+";");
            // }
            idle = Long.parseLong(splitLine[4]) + Long.parseLong(splitLine[5]);
            work = Long.parseLong(splitLine[1]) + Long.parseLong(splitLine[2]) +
                    Long.parseLong(splitLine[3]) + Long.parseLong(splitLine[6]) +
                    Long.parseLong(splitLine[7]) + Long.parseLong(splitLine[8]);
            total = idle + work;
        }catch (IOException ex){
            System.err.println(ex);
        }
    }

    public void updateCpuUsing(){
        long idleNow = 0, workNow = 0, totalNow = 0;
        long totalDif,idleDif;
        Path plik = Paths.get("/proc/stat");
        try(BufferedReader read = Files.newBufferedReader(plik)){
            String line;
            String [] splitLine;
            line=read.readLine();
            splitLine = line.split("\\s+");
            //for(String str : splitLine){
            //    System.out.print(str+";");
            // }
            idleNow = Long.parseLong(splitLine[4]) + Long.parseLong(splitLine[5]);
            workNow = Long.parseLong(splitLine[1]) + Long.parseLong(splitLine[2]) +
                    Long.parseLong(splitLine[3]) + Long.parseLong(splitLine[6]) +
                    Long.parseLong(splitLine[7]) + Long.parseLong(splitLine[8]);
            totalNow = idleNow + workNow;
            totalDif = totalNow - total;
            idleDif = idleNow - idle;
            //System.out.println(totalNow +";"+ idleNow +";"+ idle +";"+ total);
            try{
                float tmp = 10000*(totalDif - idleDif)/totalDif;
                cpuUssing = tmp/100;
            } catch(ArithmeticException ex) {
                return;
            }

        }catch (IOException ex){
            System.err.println(ex);
        }
        idle = idleNow;
        total = totalNow;

    }
    public String getName(){
        return name;
    }
    public int getCores(){
        return cores;
    }
    public int getMhz(){
        return mhz;
    }
    public float getCpuUssing(){
        return cpuUssing;
    }
}