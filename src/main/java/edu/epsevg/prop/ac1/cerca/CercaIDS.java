package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import java.util.*;

public class CercaIDS extends Cerca {
    
    private static final int PROFUNDITAT_MAXIMA_GLOBAL = 5; // Poso 5 perque esta 5 anys per fer el mapaD
    
    public CercaIDS(boolean usarLNT) {
        super(usarLNT);
    }
    
    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        rc.startTime();
        
        for (int limitProfunditat = 0; limitProfunditat <= PROFUNDITAT_MAXIMA_GLOBAL; limitProfunditat++) {
            
            Map<Mapa, Integer> visitatsProfunditat = usarLNT ? new HashMap<>() : null;
            
            List<Moviment> resultat = dlsIterativa(inicial, limitProfunditat, visitatsProfunditat, rc);
            
            if (resultat != null) {
                rc.setCami(resultat);
                rc.stopTime();
                return;
            }
        }
        
        rc.stopTime();
    }

    private List<Moviment> dlsIterativa(Mapa inicial, int limit, Map<Mapa, Integer> visitatsProfunditat, ResultatCerca rc) {
        

        Stack<Mapa> pilaEstats = new Stack<>();

        Stack<List<Moviment>> pilaCamins = new Stack<>();
        
        pilaEstats.push(inicial);
        pilaCamins.add(new ArrayList<>());

        while (!pilaEstats.isEmpty()) {
            Mapa mapaActual = pilaEstats.pop();
            List<Moviment> camiActual = pilaCamins.pop();
            int profunditatActual = camiActual.size();
            
            if (mapaActual.esMeta()) {
                return new ArrayList<>(camiActual);
            }

            if (profunditatActual >= limit) {
                rc.incNodesTallats();
                continue;
            }
            
            if (usarLNT && visitatsProfunditat != null) {
                Integer profunditatPrevia = visitatsProfunditat.get(mapaActual);
                if (profunditatPrevia != null && profunditatPrevia < profunditatActual) {
                    rc.incNodesTallats();
                    continue;
                }
                visitatsProfunditat.put(mapaActual, profunditatActual);
            }
            
            rc.incNodesExplorats();
            rc.updateMemoria(pilaEstats.size() + (usarLNT && visitatsProfunditat != null ? visitatsProfunditat.size() : 0));

            List<Moviment> accions = mapaActual.getAccionsPossibles();
            Collections.reverse(accions);
            for (Moviment moviment : accions) {
                Mapa seguentMapa = mapaActual.mou(moviment);
                
                List<Moviment> seguentCami = new ArrayList<>(camiActual);
                seguentCami.add(moviment);

                pilaEstats.push(seguentMapa);
                pilaCamins.push(seguentCami);
            }
        }
        
        return null;
    }
}