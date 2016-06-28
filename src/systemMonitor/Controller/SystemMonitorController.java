package systemMonitor.Controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import systemMonitor.Class.Memory;
import systemMonitor.Class.Procesor;
import systemMonitor.Class.ActiveProcess;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class SystemMonitorController implements Initializable {

    @FXML
    public TableView<ActiveProcess> processTableView;
    @FXML
    public TableColumn<ActiveProcess, Integer> pidColumn;
    @FXML
    public TableColumn<ActiveProcess, String> nazwaColumn;
    @FXML
    public TableColumn<ActiveProcess, Float> cpuColumn;
    @FXML
    public TableColumn<ActiveProcess, Long> vSizeColumn;
    @FXML
    public TableColumn<ActiveProcess, Long> ramColumn;
    @FXML
    public Label procNameLabel;
    @FXML
    public Label ramTotalLabel;
    @FXML
    public Label swapTotalLabel;
    @FXML
    public LineChart<String,Number> lineChartCpu;
    @FXML
    public LineChart<String,Number> lineChartMem;
    @FXML
    public Label cpuPercentLabel;


    private long maxPid =0;
    private ObservableList<ActiveProcess> proc = FXCollections.observableList(createList());
    private Procesor procesor=new Procesor();
    private Memory mem = new Memory();
    private SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss");
    private XYChart.Series seriesCpu = new XYChart.Series<String,Number>();
    private XYChart.Series seriesRam = new XYChart.Series<String,Number>();
    private XYChart.Series seriesSwap = new XYChart.Series<String,Number>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //tablica procesow
        pidColumn.setCellValueFactory(new PropertyValueFactory<>("pid"));
        nazwaColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cpuColumn.setCellValueFactory(new PropertyValueFactory<>("procPercent"));
        vSizeColumn.setCellValueFactory(new PropertyValueFactory<>("vSize"));
        ramColumn.setCellValueFactory(new PropertyValueFactory<>("ram"));
        processTableView.setItems(proc);
        procNameLabel.setText(procesor.getName());
        //wykresy
        for(int i=0;i<20;i++) {
            seriesCpu.getData().add(new XYChart.Data("0"+i,i));
            seriesRam.getData().add(new XYChart.Data("0"+i,i));
            if(mem.isSwap()) seriesSwap.getData().add(new XYChart.Data("0"+i,i));
            }

        lineChartCpu.getData().add(seriesCpu);
        if(mem.isSwap()){
            lineChartMem.getData().addAll(seriesRam,seriesSwap);
        } else {
          lineChartMem.getData().add(seriesRam);
        }

        refresh();

    }

    private ArrayList<ActiveProcess> createList() {

        ArrayList<ActiveProcess> procesy = new ArrayList<>();
        int pid, usr;
        long procTime;
        String name;

        File dir;
        dir = new File("/proc");
        for (File kat : dir.listFiles())
            if (kat.isDirectory()) {
                try {
                    pid = Integer.parseInt(kat.getName());
                } catch (Exception ex) {
                    continue;
                }
                if (pid > maxPid) maxPid = pid;
                Path file = Paths.get("/proc/" + pid + "/stat");
                try (BufferedReader read = Files.newBufferedReader(file)) {
                    String[] splitLine = read.readLine().split("\\s+");
                    //for(String str : splitLine) System.out.print(" "+str);
                    procTime = Long.parseLong(splitLine[13]);
                    name = splitLine[1].replace("(","").replace(")","");
                    usr = Integer.parseInt(splitLine[5]);

                    procesy.add(new ActiveProcess(pid, name, procTime));
                } catch (IOException ex) {
                }
            }

        System.out.println("Tworzy liste");
        return procesy;
    }


    private void refresh() {

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {

            boolean swap = mem.isSwap();
            float procUssing;
            int pid, usr;
            long procTime,memUssing, memTotal,swapTotal,swapFree;
            String name;
            File dir;
            int tmp;
            @Override
            public void handle(ActionEvent ev) {

                for (int i = 0; i < proc.size(); i++) {
                    if (!proc.get(i).processUpdate()) proc.remove(i);
                }
                dir = new File("/proc");
                for (File kat : dir.listFiles()) {
                    if (kat.isDirectory()) {
                        try {
                            pid = Integer.parseInt(kat.getName());
                        } catch (Exception ex) {
                            continue;
                        }
                        if (pid > maxPid) {
                            //System.out.println("nowy proces");
                            Path file = Paths.get("/proc/" + pid + "/stat");
                            try (BufferedReader read = Files.newBufferedReader(file)) {
                                String[] splitLine = read.readLine().split("\\s+");
                                //for(String str : splitLine) System.out.print(" "+str);
                                procTime = Long.parseLong(splitLine[13]);
                                name = splitLine[1].replace("(", "").replace(")", "");
                                usr = Integer.parseInt(splitLine[5]);

                                proc.add(new ActiveProcess(pid, name, procTime));
                            } catch (IOException ex) {
                                System.out.println("problem przy dodaniu nowego procesu " + ex);
                            }
                            maxPid = pid;
                        }
                    }
                }
                procesor.updateCpuUsing();
                mem.updateUssingMem();

                memUssing = mem.getUssingMem();
                memTotal = mem.getTotalMem();
                procUssing = procesor.getCpuUssing();

                if(swap){
                    swapTotal = mem.getTotalSwap();
                    swapFree = mem.getFreeSwap();
                    swapTotalLabel.setText("Swap: "+(swapTotal-swapFree)+" z "+swapTotal+" mb");
                } else {
                    swapTotalLabel.setText("Swap Niedostepny");
                }
                ramTotalLabel.setText("Ram: "+memUssing+" z "+memTotal+" mb");
                cpuPercentLabel.setText(procUssing+"%");

                seriesCpu.getData().add(new XYChart.Data(ft.format(new Date()), procUssing));
                seriesCpu.getData().remove(0);
                seriesRam.getData().add(new XYChart.Data(ft.format(new Date()), mem.getUsInPerc()));
                seriesRam.getData().remove(0);
                if(swap){
                    seriesSwap.getData().add(new XYChart.Data(ft.format(new Date()), mem.getSwapPerc()));
                    seriesSwap.getData().remove(0);
                }

                processTableView.refresh();

            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

}
