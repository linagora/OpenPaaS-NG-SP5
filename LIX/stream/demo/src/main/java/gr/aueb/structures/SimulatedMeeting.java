/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aueb.structures;

import gr.aueb.demo.Simulation;
import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author midas
 */
public class SimulatedMeeting {

    private List<String> raw;
    private Integer timeInterval;
    private List<String> intervalText;
    private Integer maxId;
    private final int startTime;
    private final int endTime;
    private ArrayList<String> intervalSummaryGround;
    private ArrayList<Double> bins;
    public SimulatedMeeting(List<String> raw, Integer timeInterval) {
        this.raw = raw;
        this.timeInterval = timeInterval;
        startTime = Double.valueOf(raw.get(0).split("\t")[1]).intValue();
        endTime = Double.valueOf(raw.get(raw.size() - 1).split("\t")[1]).intValue();
        maxId = (int) ((endTime - startTime) / timeInterval);
        intervalText = new ArrayList<String>();
        bins=new ArrayList<Double>();
        String aggregator = "";
        int counter = startTime / timeInterval;
        for (String s : raw) {
            String[] tokens = s.split("\t");
            Integer idx = (int) (Double.valueOf(tokens[1]) / timeInterval);
            if (idx == counter) {
                aggregator += tokens[3] + "\t" + tokens[8] + "\n";
            } else {
                aggregator += tokens[3] + "\t" + tokens[8] + "\n";
                bins.add(Double.valueOf(tokens[1]));
                intervalText.add(aggregator);
                counter = idx;
                aggregator = "";
            }

        }
    }

    public String getTextAtInterval(int id) {
        return intervalText.get(min(id, intervalText.size()-1));
    }

    public String getGroundAtInterval(int id) {
        return intervalSummaryGround.get(min(id, intervalSummaryGround.size()-1));
    }

    public Integer getMaxId() {
        return maxId;
    }

    public void setMaxId(Integer maxId) {
        this.maxId = maxId;
    }
    private HashMap<String,String> stem2original;
    
    public void setGroundTruth(List<String> lines) {
        stem2original=new HashMap<String,String>();
        intervalSummaryGround = new ArrayList<String>(intervalText.size());
        for (int i = 0; i < intervalText.size(); i++) {
            intervalSummaryGround.add("");
        }
        for (String s : lines) {
            
            String[] tokens = s.split("\t");
            String line = tokens[3] + "\t" + tokens[8] + "\n";
            for(String t:tokens[8].split(" ")){
                stem2original.put(Simulation.stemTerm(t), t);
            }
            Double stamp = Double.valueOf(tokens[1]);
            int idx = -1;
            for(int i=0;i<bins.size();i++){
                Double bin = bins.get(i);
                if (stamp<bins.get(i)){
                    idx=i;  
                    break;
                }
            }
            if(idx>-1 && idx<this.maxId){
                intervalSummaryGround.set(idx, intervalSummaryGround.get(idx)+line); 
            }
        }
        
    }

    public HashMap<String, String> getStem2original() {
        return stem2original;
    }

    public void setStem2original(HashMap<String, String> stem2original) {
        this.stem2original = stem2original;
    }
    
}
