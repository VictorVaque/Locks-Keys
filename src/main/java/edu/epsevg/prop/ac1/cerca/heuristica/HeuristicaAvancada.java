package edu.epsevg.prop.ac1.cerca.heuristica;
import edu.epsevg.prop.ac1.model.Mapa;
import edu.epsevg.prop.ac1.model.Posicio;
import edu.epsevg.prop.ac1.model.Direccio;
import java.util.*;

/**
 * Heuristica avançada: Optimización del código base con mejoras incrementales
 */
public class HeuristicaAvancada implements Heuristica {
    
    private boolean inicialitzat = false;
    private Posicio sortida;
    private int n, m;
    
    private Map<Character, Posicio> posClaus;
    private Map<Posicio, Map<Posicio, Integer>> distanciesBFS;
    
    // MEJORA 1: Cache del análisis de criticidad
    private Set<Character> clausCritiques;
    
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
                
                // MEJORA 2: Bonus mejorado basado en criticidad pre-calculada
                if (clausCritiques.contains(clau)) {
                    distanciaTotal = Math.max(1, distanciaTotal - 2);
                }
                
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
        
        // Escanear grid
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                Posicio p = new Posicio(i, j);
                int cell = mapa.getCellValue(p);
                
                if (Character.isLowerCase(cell)) {
                    posClaus.put((char) cell, p);
                }
            }
        }
        
        // Pre-calcular distancias BFS
        distanciesBFS.put(sortida, bfsDesde(mapa, sortida));
        
        for (Posicio posClau : posClaus.values()) {
            distanciesBFS.put(posClau, bfsDesde(mapa, posClau));
        }
        
        // MEJORA 3: Pre-calcular llaves críticas de forma eficiente
        clausCritiques = identificarClausCritiques(mapa);
        
        inicialitzat = true;
    }
    
    /**
     * MEJORA 4: Identificación eficiente de llaves críticas
     * Una llave es crítica si su puerta bloquea el acceso directo a la salida
     */
    private Set<Character> identificarClausCritiques(Mapa mapa) {
        Set<Character> critiques = new HashSet<>();
        
        // BFS desde salida para encontrar puertas cercanas
        Queue<NodeBFS> queue = new LinkedList<>();
        Set<Posicio> visitats = new HashSet<>();
        
        queue.add(new NodeBFS(sortida, 0));
        visitats.add(sortida);
        
        while (!queue.isEmpty()) {
            NodeBFS current = queue.poll();
            
            // OPTIMIZACIÓN: Solo explorar hasta distancia razonable
            if (current.cost > Math.min(n, m)) {
                break;
            }
            
            for (Direccio dir : Direccio.values()) {
                Posicio next = current.pos.translate(dir);
                
                if (visitats.contains(next)) {
                    continue;
                }
                
                int cell = mapa.getCellValue(next);
                
                if (cell == Mapa.PARET) {
                    continue;
                }
                
                if (Character.isUpperCase(cell)) {
                    char clauCritica = Character.toLowerCase((char) cell);
                    critiques.add(clauCritica);
                    // No explorar más allá de puertas
                } else {
                    visitats.add(next);
                    queue.add(new NodeBFS(next, current.cost + 1));
                }
            }
        }
        
        return critiques;
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