/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aueb.kcore;

import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 *
 * @author Midas
 */
public class NeighborIndexWrapper {
    NeighborIndex ni;
    DirectedNeighborIndex ni_d;
    Graph g;
    Integer direction=0;
    public NeighborIndexWrapper(Graph g,Integer direction) {
        this.g=g;
        this.direction=direction;
        if(direction==0)
            ni  = new NeighborIndex<String, DefaultWeightedEdge>(g);
        else
            ni_d=new DirectedNeighborIndex<String, DefaultWeightedEdge>((DirectedGraph<String, DefaultWeightedEdge>) g);
    }
    
   public Set<String> neighborsOf(Object v,Boolean inNeighbor){
       if (direction==0)
           return ni.neighborsOf(v);
       else if (direction!=0){
           if (inNeighbor)
                return ni_d.predecessorsOf(v);
           else
               return ni_d.successorsOf(v);
       }   
       else
           return null;
   }
    
}
