package edu.epsevg.prop.ac1.cerca.heuristica;
import edu.epsevg.prop.ac1.model.Mapa;
import edu.epsevg.prop.ac1.model.Posicio;
import edu.epsevg.prop.ac1.model.Direccio;
import java.util.*;

/**
 * RESUM DE LA HEURISTICA:
 * - Pre-calcula distàncies BFS des de punts clau (sortida i claus)
 * - Per a cada estat, avalua el cost d'anar a recollir cada clau pendent
 * - Aplica bonus agressius a claus crítiques i penalitzacions a claus no crítiques
 */
public class HeuristicaAvancada implements Heuristica {
    

    private boolean inicialitzat = false;
    
    private Posicio sortida;
    private int n, m; 
    
    private Map<Character, Posicio> posClaus;
    
    // Distàncies BFS pre-calculades
    // Mapa: posició_origen -> (posició_destí -> distància)
    private Map<Posicio, Map<Posicio, Integer>> distanciesBFS;
    
    // Conjunt de claus crítiques (aquelles les portes de les quals bloquegen l'accés a la sortida)
    private Set<Character> clausCritiques;
    
    /**
     * @param estat Estat actual del mapa
     * @return Estimació del cost per arribar a l'objectiu des d'aquest estat
     */
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
        
        // Comptador de claus crítiques pendents (per a penalització addicional)
        int numClausCritiquesPendents = 0;
        
        // Avaluar cada clau al mapa
        for (Map.Entry<Character, Posicio> entry : posClaus.entrySet()) {
            char clau = entry.getKey();
            
            // Si aquesta clau encara no ha estat recollida
            if (!estat.teClau(clau)) {
                hiHaClauPendent = true;
                
                if (clausCritiques.contains(clau)) {
                    numClausCritiquesPendents++;
                }
                
                Posicio posClau = entry.getValue();
                

                int distAgentAClau = calcularMinimaDistanciaReal(agents, posClau);
                
                if (distAgentAClau == Integer.MAX_VALUE) {
                    continue; // Clau inaccessible, provar següent
                }
                

                int distClauASortida = getDistanciaReal(posClau, sortida);
                
                if (distClauASortida == Integer.MAX_VALUE) {
                    distClauASortida = distanciaManhattan(posClau, sortida);
                }
                
                // Cost base: agent→clau + clau→sortida
                int distanciaTotal = distAgentAClau + distClauASortida;
                
                // SISTEMA DE BONIFICACIONS/PENALITZACIONS:
                // Objectiu: Prioritzar claus crítiques i evitar claus no importants
                if (clausCritiques.contains(clau)) {
                    // BONIFICACIÓ per a claus crítiques
                    if (numClausCritiquesPendents == 1) {
                        // Si és l'única clau crítica pendent, màxima prioritat
                        distanciaTotal = Math.max(1, distanciaTotal - 4);
                    } else {
                        // Si hi ha múltiples claus crítiques, prioritat alta però no màxima
                        distanciaTotal = Math.max(1, distanciaTotal - 3);
                    }
                } else {
                    // PENALITZACIÓ per a claus no crítiques
                    distanciaTotal += 2;
                }
                
                minDistanciaTotal = Math.min(minDistanciaTotal, distanciaTotal);
            }
        }
        
        // Si no hi ha claus pendents, calcular distància directa a la sortida
        if (!hiHaClauPendent) {
            minDistanciaTotal = calcularMinimaDistanciaReal(agents, sortida);
        }
        
        // PENALITZACIÓ ADDICIONAL: Si hi ha múltiples claus crítiques pendents,
        // afegir penalització per reflectir que es necessiten múltiples passos seqüencials
        if (numClausCritiquesPendents > 1) {
            minDistanciaTotal += (numClausCritiquesPendents - 1);
        }
        
        return minDistanciaTotal;
    }
    
    /**
     * Aquesta funció s'executa UNA SOLA VEGADA en avaluar el primer estat.
     * L'objectiu és fer un preprocessament costós una vegada, per després
     * tenir lookups O(1) a cada crida de h().
     * 
     * @param mapa Estat inicial del mapa
     */
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
        
        // Pre-calcular distàncies BFS des de punts clau
        // Des de la sortida: útil per calcular distància(clau→sortida)
        distanciesBFS.put(sortida, bfsDesde(mapa, sortida));
        
        // Des de cada clau: útil per calcular distància(agent→clau)
        for (Posicio posClau : posClaus.values()) {
            distanciesBFS.put(posClau, bfsDesde(mapa, posClau));
        }
        
        // Identificar claus crítiques
        clausCritiques = identificarClausCritiques(mapa);
        
        inicialitzat = true;
    }
    
    /**
     * Identifica quines claus són "crítiques" (les seves portes bloquegen accés directe a la sortida).
     * @param mapa Mapa del joc
     * @return Conjunt de caràcters de claus crítiques
     */
    private Set<Character> identificarClausCritiques(Mapa mapa) {
        Set<Character> critiques = new HashSet<>();
        Queue<NodeBFS> queue = new LinkedList<>();
        Set<Posicio> visitats = new HashSet<>();
        
        // Iniciar BFS des de la sortida
        queue.add(new NodeBFS(sortida, 0));
        visitats.add(sortida);
        
        while (!queue.isEmpty()) {
            NodeBFS current = queue.poll();
            
            // limitar cerca per no explorar tot el mapa
            if (current.cost > Math.min(n, m)) {
                break;
            }
            
            // Explorar en les 4 direccions
            for (Direccio dir : Direccio.values()) {
                Posicio next = current.pos.translate(dir);
                
                if (visitats.contains(next)) {
                    continue;
                }
                
                int cell = mapa.getCellValue(next);
                
                if (cell == Mapa.PARET) {
                    continue;
                }
                
                // Si trobem una porta (lletra majúscula), la seva clau és crítica
                if (Character.isUpperCase(cell)) {
                    char clauCritica = Character.toLowerCase((char) cell);
                    critiques.add(clauCritica);
                    // NO seguir explorant més enllà d'aquesta porta
                } else {
                    // Espai lliure, clau, o sortida: continuar explorant
                    visitats.add(next);
                    queue.add(new NodeBFS(next, current.cost + 1));
                }
            }
        }
        
        return critiques;
    }
    
    /**
     * Executa BFS des d'una posició origen per calcular distàncies a totes les altres.
     * 
     * @param mapa Mapa
     * @param origen Posició des de la qual calcular distàncies
     * @return Mapa de posició → distància des de l'origen
     */
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
    
    /**
     * Calcula la distància mínima de qualsevol agent a un objectiu usant BFS pre-calculat.
     * 
     * @param agents Llista de posicions d'agents
     * @param objectiu Posició objectiu
     * @return Distància mínima (usa BFS pre-calculat o Manhattan com a fallback)
     */
    private int calcularMinimaDistanciaReal(List<Posicio> agents, Posicio objectiu) {
        int minDist = Integer.MAX_VALUE;
        
        // Intentar obtenir distàncies pre-calculades des de l'objectiu
        Map<Posicio, Integer> distsDesdeObjectiu = distanciesBFS.get(objectiu);
        
        if (distsDesdeObjectiu != null) {

            for (Posicio agent : agents) {
                Integer dist = distsDesdeObjectiu.get(agent);
                if (dist != null) {
                    minDist = Math.min(minDist, dist);
                } else {
                    // Si no hi ha distància pre-calculada, usar Manhattan
                    int distManhattan = distanciaManhattan(agent, objectiu);
                    minDist = Math.min(minDist, distManhattan);
                }
            }
        } else {
            // Si no hi ha BFS pre-calculat, usar Manhattan per a tots
            for (Posicio agent : agents) {
                int dist = distanciaManhattan(agent, objectiu);
                minDist = Math.min(minDist, dist);
            }
        }
        
        return minDist;
    }
    
    /**
     * 
     * @param desde Posició origen
     * @param fins Posició destí
     * @return Distància BFS o Integer.MAX_VALUE si no existeix
     */
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
    
    /**
     * @param a Primera posició
     * @param b Segona posició
     * @return Distància Manhattan
     */
    private int distanciaManhattan(Posicio a, Posicio b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
    
    /**
     * NodeBFS representa un node amb la seva posició i cost acumulat.
     */
    private static class NodeBFS {
        Posicio pos;
        int cost;
        
        NodeBFS(Posicio pos, int cost) {
            this.pos = pos;
            this.cost = cost;
        }
    }
}