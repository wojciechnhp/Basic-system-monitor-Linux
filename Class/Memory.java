package systemMonitor.Class;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Memory {
    private long ussingMemory,tmp,totalMemory,totalSwap,freeSwap;
    private boolean swap;
    private float percent,percentSwap;

    public Memory() {
        Path plik = Paths.get("/proc/meminfo");
        try(BufferedReader read = Files.newBufferedReader(plik)){
            String line;
            String splitLine[];
            while((line = read.readLine())!= null){
                splitLine = line.split(":");
                if(splitLine[0].trim().equalsIgnoreCase("memtotal"))
                    totalMemory = Integer.parseInt(splitLine[1].trim().split(" ")[0]);
                if(splitLine[0].trim().equalsIgnoreCase("swaptotal"))
                    totalSwap = Integer.parseInt(splitLine[1].trim().split(" ")[0]);
            }
        }catch (IOException ex) {
        }
        swap = false;
        if(totalSwap > 0) swap = true;
    }
    public void updateUssingMem() {
        Path plik = Paths.get("/proc/meminfo");
        try(BufferedReader read = Files.newBufferedReader(plik)){
            String line;
            String splitLine[];
            while((line = read.readLine())!= null){
                splitLine = line.split(":");
                if(splitLine[0].trim().equalsIgnoreCase("active"))
                    ussingMemory = Integer.parseInt(splitLine[1].trim().split(" ")[0]);
                if(splitLine[0].trim().equalsIgnoreCase("swapfree") && swap)
                    freeSwap = Integer.parseInt(splitLine[1].trim().split(" ")[0]);
            }
        }catch (IOException ex) {}
        try {
            tmp = 10000 * ussingMemory / totalMemory;
            percent = tmp / 100;
            if (swap) {
                tmp = 10000 * freeSwap / totalSwap;
                percentSwap = 100 - tmp / 100;
            }
        } catch (ArithmeticException aex){

        }
    }

    public long getTotalMem(){
        return totalMemory/1024;
    }

    public long getTotalSwap(){
        return totalSwap/1024;
    }

    public boolean isSwap(){
        return swap;
    }

    public long getUssingMem(){
        return ussingMemory/1024;
    }

    public long getFreeSwap(){
        return freeSwap/1024;
    }
    public float getUsInPerc(){
        return percent;
    }
    public float getSwapPerc(){
        return percentSwap;
    }
}
