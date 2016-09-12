package gr.aueb.kcore;

import gr.aueb.wordgraph.GraphOfWords;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.reverseOrder;

/**
 * Created by Midas on 7/11/2016.
 */
public class KCore {

    public static void main(String[] args) throws IOException, Exception {

        List<String> lines = Files.readAllLines(Paths.get(args[0]));
        int scaleFactor = 1;
        Integer direction=null;
        if (args.length == 2) {
            direction = Integer.valueOf(args[1]);
            if (direction>2)
                throw new Exception("direction can be integer between [0-2]");
        }
        if (args.length == 3) {
            scaleFactor = Integer.valueOf(args[2]);
        }

        WeightedGraph g;
        //WeightedGraph<String, DefaultWeightedEdge> g =new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        g =new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        if(direction!=null && direction!=0)
            g =new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
         
        for (String line : lines) {
            String[] components = line.split(" ");
            String v1 = components[0];
            //System.out.println(v1);
            String v2 = components[1];
            Double w = Double.valueOf(components[2]);

            if (!g.containsVertex(v1)) {    
                g.addVertex(v1);                
            }
            if (!g.containsVertex(v2)) {
                g.addVertex(v2);
            }
            
            DefaultWeightedEdge e=null;
            try {
                 if (direction!=null && direction==0){
                    if (g.containsEdge(v2, v1)){
                        DefaultWeightedEdge e1 = (DefaultWeightedEdge) g.getEdge(v2, v1);
                        g.setEdgeWeight(e1, g.getEdgeWeight(e1)+w);
                    }
                    else{
                        e =  (DefaultWeightedEdge) g.addEdge(v1, v2);
                        g.setEdgeWeight(e, w);
                    }
                }
                else{
                    e =  (DefaultWeightedEdge) g.addEdge(v1, v2);
                    g.setEdgeWeight(e, w);
                }
                
            } catch (Exception exc) {
                System.out.println("Failed to set edge weight, NO duplicate entries are allowed");
                exc.printStackTrace();
                System.out.println("contains v1: "+g.containsVertex(v1));
                System.out.println("contains v2: "+g.containsVertex(v2));
                System.out.println("v1-v2"+e.toString());
                
                
                System.exit(-1);
            }
        }
        WeightedGraphKCoreDecomposer decomposer = new WeightedGraphKCoreDecomposer(g, scaleFactor,direction);
        Map<String, Double> map = decomposer.coreNumbers();
        map=sortByValue(map);
        String cores = "";
        PrintWriter p = new PrintWriter(args[0].split("\\.")[0] + "_cores.csv");
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            p.println(key + "  " + value);
        }
        p.close();
    }

    public Map<String, Double> kcoreDecomposition(WeightedGraph g,int scaleFactor,Integer direction){
        WeightedGraphKCoreDecomposer decomposer = new WeightedGraphKCoreDecomposer(g, scaleFactor,direction);
        Map<String, Double> map = decomposer.coreNumbers();
        map=sortByValue(map);
        return map;
    }
    
    public Map<String, Double> kcoreDecomposition(String text,int scaleFactor){
        
        GraphOfWords grow=new GraphOfWords(text);
        WeightedGraph g = grow.getGraph();
        WeightedGraphKCoreDecomposer decomposer = new WeightedGraphKCoreDecomposer(g, scaleFactor,0);
        Map<String, Double> map = decomposer.coreNumbers();
        map=sortByValue(map);
        return map;
    }
    public static <K, V extends Comparable<? super V>> Map<K, V>
            sortByValue(Map<K, V> map) {              
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();
        st.sorted(reverseOrder(Map.Entry.comparingByValue()))
                .forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }
            
     
}
