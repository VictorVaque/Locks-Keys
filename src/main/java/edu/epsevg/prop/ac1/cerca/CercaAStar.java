package edu.epsevg.prop.ac1.cerca;
import edu.epsevg.prop.ac1.cerca.heuristica.Heuristica;
import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import java.util.*;

public class CercaAStar extends Cerca {
    private final Heuristica heur;
    
    public CercaAStar(boolean usarLNT, Heuristica heur) { 
        super(usarLNT); 
        this.heur = heur; 
    }
    
    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        rc.startTime();
        
        PriorityQueue<NodeAStar> openSet = new PriorityQueue<>();
        Set<Mapa> closedSet = new HashSet<>();
        Map<Mapa, Integer> millorsGCosts = new HashMap<>(); // NUEVO: trackear mejores g-costs
        
        int hInicial = heur.h(inicial);
        NodeAStar nodeInicial = new NodeAStar(inicial, new ArrayList<>(), 0, hInicial);
        
        openSet.add(nodeInicial);
        millorsGCosts.put(inicial, 0);
        
        while (!openSet.isEmpty()) {
            NodeAStar currentNode = openSet.poll();
            Mapa mapaActual = currentNode.getEstat();
            
            rc.updateMemoria(openSet.size() + closedSet.size());
            
            if (mapaActual.esMeta()) {
                rc.setCami(currentNode.getCami());
                rc.stopTime();
                return;
            }
            
            closedSet.add(mapaActual);
            rc.incNodesExplorats();
            
            for (Moviment moviment : mapaActual.getAccionsPossibles()) {
                Mapa seguentEstat = mapaActual.mou(moviment);
                
                if (closedSet.contains(seguentEstat)) {
                    rc.incNodesTallats();
                    continue;
                }
                
                int gCost = currentNode.getGCost() + 1;
                

                Integer millorGAnterior = millorsGCosts.get(seguentEstat);
                
                if (millorGAnterior != null && millorGAnterior <= gCost) {

                    rc.incNodesTallats();
                    continue;
                }
                

                int hCost = heur.h(seguentEstat);
                NodeAStar seguentNode = new NodeAStar(seguentEstat, currentNode.getCami(), gCost, hCost);
                seguentNode.getCami().add(moviment);
                
                openSet.add(seguentNode);
                millorsGCosts.put(seguentEstat, gCost);
            }
        }
        
        rc.stopTime();
    }
    
    private static class NodeAStar implements Comparable<NodeAStar> {
        private final Mapa estat;
        private final List<Moviment> cami;
        private final int gCost;
        private final int fCost;
        
        public NodeAStar(Mapa estat, List<Moviment> cami, int gCost, int hCost) {
            this.estat = estat;
            this.cami = new ArrayList<>(cami);
            this.gCost = gCost;
            this.fCost = gCost + hCost;
        }
        
        public Mapa getEstat() { return estat; }
        public List<Moviment> getCami() { return cami; }
        public int getGCost() { return gCost; }
        public int getFCost() { return fCost; }
        
        @Override
        public int compareTo(NodeAStar other) {
            return Integer.compare(this.fCost, other.fCost);
        }
    }
}