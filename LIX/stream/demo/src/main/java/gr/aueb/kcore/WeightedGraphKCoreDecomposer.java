package gr.aueb.kcore;

import org.jgrapht.WeightedGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jgrapht.graph.DefaultWeightedEdge;

public class WeightedGraphKCoreDecomposer implements KCoreDecomposer {

    WeightedGraph<String, DefaultWeightedEdge> g;
    NeighborIndexWrapper ni;
    Map<String, Integer> vi;
    String[] iv;
    Integer direction;
    int numberOfNodes;

    int scaleFactor = 1;

    public WeightedGraphKCoreDecomposer(WeightedGraph<String, DefaultWeightedEdge> g, Integer scaleFactor, Integer direction) {
        this.g = g;
        this.direction = direction;
        //ni = new NeighborIndex<String, DefaultWeightedEdge>(this.g);
        ni = new NeighborIndexWrapper(this.g, direction);
        this.scaleFactor = scaleFactor;
        Set<String> vertices = g.vertexSet();
        numberOfNodes = vertices.size();
    }

    public Map<String, Double> coreNumbers() {
        vi = verticesToInts();
        iv = intsToVertices();
        int[] degree = new int[numberOfNodes];
        int maxDegree = -1;
        Set<String> neighbors;
        DefaultWeightedEdge edge;

        for (Map.Entry<String, Integer> entry : vi.entrySet()) {
            neighbors = ni.neighborsOf(entry.getKey(), getNeighborBoolDirected(true));
            for (String neighbor : neighbors) {
                if (this.direction == 1) {
                    edge = g.getEdge(neighbor, entry.getKey());
                } else {
                    edge = g.getEdge(entry.getKey(), neighbor);
                }
                degree[entry.getValue()] += g.getEdgeWeight(edge) * scaleFactor;
            }

            if (degree[entry.getValue()] > maxDegree) {
                maxDegree = degree[entry.getValue()];
            }
        }

        int[] nodesWithDegree = new int[maxDegree + 1];
        for (int i = 0; i < numberOfNodes; i++) {
            nodesWithDegree[degree[i]]++;
        }

        int[] pointers = new int[maxDegree + 1];
        for (int i = 1; i < maxDegree + 1; i++) {
            pointers[i] = pointers[i - 1] + nodesWithDegree[i - 1];
        }

        int[] vert = new int[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            vert[pointers[degree[i]]] = i;
            pointers[degree[i]]++;
        }

        for (int i = 0; i < maxDegree + 1; i++) {
            pointers[i] = pointers[i] - nodesWithDegree[i];
        }

        int[] positions = new int[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            positions[vert[i]] = i;
        }

        int degreeToGet;
        HashMap<String, Double> coreNumbers = new HashMap<String, Double>();
        for (int i = 0; i < numberOfNodes; i++) {
            coreNumbers.put(iv[vert[i]], degree[vert[i]] / (double) scaleFactor);
            neighbors = ni.neighborsOf(iv[vert[i]], getNeighborBoolDirected(false));
            for (String neighbor : neighbors) {
                if (degree[vi.get(neighbor)] > degree[vert[i]]) {
                    if (this.direction == 1) {
                        edge = g.getEdge( iv[vert[i]],neighbor);
                    } else if(direction==2){
                        edge = g.getEdge( neighbor,iv[vert[i]]);
                    }
                    else{
                        edge = g.getEdge( iv[vert[i]],neighbor);
                    }
                    if ((degree[vi.get(neighbor)] - (int) (g.getEdgeWeight(edge) * scaleFactor)) > degree[vert[i]]) {
                        degreeToGet = degree[vi.get(neighbor)] - (int) (g.getEdgeWeight(edge) * scaleFactor);
                    } else {
                        degreeToGet = degree[vert[i]];
                    }

                    vert[positions[vi.get(neighbor)]] = vert[pointers[degree[vi.get(neighbor)]]];
                    positions[vert[positions[vi.get(neighbor)]]] = positions[vi.get(neighbor)];
                    pointers[degree[vi.get(neighbor)]]++;
                    for (int j = degree[vi.get(neighbor)] - 1; j > degreeToGet; j--) {
                        if (degree[vert[pointers[j]]] == j) {
                            vert[pointers[j + 1] - 1] = vert[pointers[j]];
                            positions[vert[pointers[j]]] = pointers[j + 1] - 1;
                            pointers[j]++;
                        } else {
                            pointers[j]++;
                        }
                    }
                    vert[pointers[degreeToGet + 1] - 1] = vi.get(neighbor);
                    positions[vi.get(neighbor)] = pointers[degreeToGet + 1] - 1;
                    degree[vi.get(neighbor)] = degreeToGet;
                }
            }
        }
        return coreNumbers;
    }

    private Map<String, Integer> verticesToInts() {

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        Set<String> vertices = g.vertexSet();

        int counter = 0;

        for (String vertex : vertices) {
            map.put(vertex, counter);
            counter++;
        }

        return map;

    }

    private String[] intsToVertices() {

        String[] map = new String[numberOfNodes];

        for (Map.Entry<String, Integer> entry : vi.entrySet()) {
            map[entry.getValue()] = entry.getKey();
        }

        return map;
    }

    private Boolean getNeighborBoolDirected(Boolean s) {
        if (this.direction == 1) {
            return s;
        } else {
            return !s;
        }
    }
}
