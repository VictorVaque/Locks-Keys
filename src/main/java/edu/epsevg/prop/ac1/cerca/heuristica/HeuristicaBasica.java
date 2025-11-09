package edu.epsevg.prop.ac1.cerca.heuristica;

import edu.epsevg.prop.ac1.model.Mapa;
import edu.epsevg.prop.ac1.model.Posicio;
import java.util.List;


public class HeuristicaBasica implements Heuristica {
    @Override
    public int h(Mapa estat) {
        if (estat.esMeta()) {
            return 0;
        }

        List<Posicio> agents = estat.getAgents();
        Posicio sortida = estat.getSortidaPosicio();

        int minDistanciaTotal = Integer.MAX_VALUE;

        for (int y = 0; y < estat.getN(); y++) {
            for (int x = 0; x < estat.getM(); x++) {
                Posicio p = new Posicio(y, x); 
                int cellValue = estat.getCellValue(p);

                if (Character.isLowerCase(cellValue)) {
                    char keyChar = (char) cellValue;
                    if (!estat.teClau(keyChar)) {
                        int distAgentAClau = calcularMinimaDistancia(agents, p);
                        int distClauASortida = distanciaManhattan(p, sortida);
                        
                        int distanciaTotal = distAgentAClau + distClauASortida;
                        if (distanciaTotal < minDistanciaTotal) {
                            minDistanciaTotal = distanciaTotal;
                        }
                    }
                }
            }
        }

        if (minDistanciaTotal == Integer.MAX_VALUE) {
            minDistanciaTotal = calcularMinimaDistancia(agents, sortida);
        }

        return minDistanciaTotal;
    }

    private int calcularMinimaDistancia(List<Posicio> agents, Posicio objectiu) {
        int minDist = Integer.MAX_VALUE;
        for (Posicio agent : agents) {
            int dist = distanciaManhattan(agent, objectiu);
            if (dist < minDist) {
                minDist = dist;
            }
        }
        return minDist;
    }

    private int distanciaManhattan(Posicio p1, Posicio p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }
}