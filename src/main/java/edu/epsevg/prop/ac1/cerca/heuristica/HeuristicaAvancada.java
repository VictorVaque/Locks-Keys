package edu.epsevg.prop.ac1.cerca.heuristica;
import edu.epsevg.prop.ac1.model.Mapa;
import edu.epsevg.prop.ac1.model.Posicio;
import edu.epsevg.prop.ac1.model.Direccio;
import java.util.*;

public class HeuristicaAvancada implements Heuristica {
    
    private boolean inicialitzat = false;
    private Posicio sortida;
    private int n, m;
    
    private Map<Character, Posicio> posClaus;
    private Map<Posicio, Map<Posicio, Integer>> distanciesBFS;
    
    @Override
    public int h(Mapa estat) {
        if (estat.esMeta()) {
            return 0;
        }
        
        if (!inicialitzat) {
            inicialitzar(estat);
        }
        
        List<Posicio> agents = estat.getAgents();
        int minDistanciaTotal = Integer.MAX_VALUE;
        boolean hiHaClauPendent = false;
        

        for (Map.Entry<Character, Posicio> entry : posClaus.entrySet()) {
            char clau = entry.getKey();
            
            if (!estat.teClau(clau)) {
                hiHaClauPendent = true;
                Posicio posClau = entry.getValue();
                

                int distAgentAClau = calcularMinimaDistanciaReal(agents, posClau);
                
                if (distAgentAClau == Integer.MAX_VALUE) {
                    continue;
                }
                

                int distClauASortida = getDistanciaReal(posClau, sortida);
                
                if (distClauASortida == Integer.MAX_VALUE) {
                    distClauASortida = distanciaManhattan(posClau, sortida);
                }
                
                int distanciaTotal = distAgentAClau + distClauASortida;
                
                distanciaTotal = aplicarBonus(estat, clau, distanciaTotal);
                
                minDistanciaTotal = Math.min(minDistanciaTotal, distanciaTotal);
            }
        }
        
        if (!hiHaClauPendent) {
            minDistanciaTotal = calcularMinimaDistanciaReal(agents, sortida);
        }
        
        return minDistanciaTotal;
    }
    
    private void inicialitzar(Mapa mapa) {
        sortida = mapa.getSortidaPosicio();
        n = mapa.getN();
        m = mapa.getM();
        
        posClaus = new HashMap<>();
        distanciesBFS = new HashMap<>();
        

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                Posicio p = new Posicio(i, j);
                int cell = mapa.getCellValue(p);
                
                if (Character.isLowerCase(cell)) {
                    posClaus.put((char) cell, p);
                }
            }
        }
        

        distanciesBFS.put(sortida, bfsDesde(mapa, sortida));
        
        for (Posicio posClau : posClaus.values()) {
            distanciesBFS.put(posClau, bfsDesde(mapa, posClau));
        }
        
        inicialitzat = true;
    }
    

    private Map<Posicio, Integer> bfsDesde(Mapa mapa, Posicio origen) {
        Map<Posicio, Integer> distancies = new HashMap<>();
        Queue<NodeBFS> queue = new LinkedList<>();
        
        queue.add(new NodeBFS(origen, 0));
        distancies.put(origen, 0);
        
        while (!queue.isEmpty()) {
            NodeBFS current = queue.poll();
            
            for (Direccio dir : Direccio.values()) {
                Posicio next = current.pos.translate(dir);
                
                if (distancies.containsKey(next)) {
                    continue;
                }
                
                int cell = mapa.getCellValue(next);
                
                if (cell == Mapa.PARET) {
                    continue;
                }
                
                distancies.put(next, current.cost + 1);
                queue.add(new NodeBFS(next, current.cost + 1));
            }
        }
        
        return distancies;
    }
    

    private int calcularMinimaDistanciaReal(List<Posicio> agents, Posicio objectiu) {
        int minDist = Integer.MAX_VALUE;
        
        Map<Posicio, Integer> distsDesdeObjectiu = distanciesBFS.get(objectiu);
        
        if (distsDesdeObjectiu != null) {
            for (Posicio agent : agents) {
                Integer dist = distsDesdeObjectiu.get(agent);
                if (dist != null) {
                    minDist = Math.min(minDist, dist);
                } else {
                    int distManhattan = distanciaManhattan(agent, objectiu);
                    minDist = Math.min(minDist, distManhattan);
                }
            }
        } else {
            // Fallback total a Manhattan
            for (Posicio agent : agents) {
                int dist = distanciaManhattan(agent, objectiu);
                minDist = Math.min(minDist, dist);
            }
        }
        
        return minDist;
    }
    

    private int getDistanciaReal(Posicio desde, Posicio fins) {
        Map<Posicio, Integer> distsDesde = distanciesBFS.get(desde);
        
        if (distsDesde != null) {
            Integer dist = distsDesde.get(fins);
            if (dist != null) {
                return dist;
            }
        }
        
        return Integer.MAX_VALUE;
    }
    

    private int aplicarBonus(Mapa mapa, char clau, int distanciaBase) {

        char portaAssociada = Character.toUpperCase(clau);
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                Posicio p = new Posicio(i, j);
                int cell = mapa.getCellValue(p);
                
                if (cell == portaAssociada) {

                    int distPortaSortida = distanciaManhattan(p, sortida);
                    

                    if (distPortaSortida < 5) {
                        return Math.max(1, distanciaBase - 1);
                    }
                    
                    break;
                }
            }
        }
        
        return distanciaBase;
    }
    

    private int distanciaManhattan(Posicio a, Posicio b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
    
    private static class NodeBFS {
        Posicio pos;
        int cost;
        
        NodeBFS(Posicio pos, int cost) {
            this.pos = pos;
            this.cost = cost;
        }
    }
}