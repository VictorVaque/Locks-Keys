package edu.epsevg.prop.ac1.cerca;
import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import java.util.*;


public class CercaIDS extends Cerca {
    
    private static final int PROFUNDITAT_MAXIMA_GLOBAL = 40;
    
    public CercaIDS(boolean usarLNT) {
        super(usarLNT);
    }
    
    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        rc.startTime();
        
        for (int limitProfunditat = 0; limitProfunditat <= PROFUNDITAT_MAXIMA_GLOBAL; limitProfunditat++) {
            
            Map<Mapa, Integer> visitatsProfunditat = usarLNT ? new HashMap<>() : null;
            
            List<Moviment> resultat = dls(inicial, limitProfunditat, new ArrayList<>(), visitatsProfunditat, rc);
            
            if (resultat != null) {
                rc.setCami(resultat);
                rc.stopTime();
                return;
            }
        }
        
        rc.stopTime();
    }
    

    private List<Moviment> dls(Mapa mapa, int limit, List<Moviment> cami, Map<Mapa, Integer> visitatsProfunditat, ResultatCerca rc) {
        
        int profunditatActual = cami.size();
        
        if (mapa.esMeta()) {
            return new ArrayList<>(cami);
        }
        

        if (profunditatActual >= limit) {
            rc.incNodesTallats();
            return null;
        }
        

        if (usarLNT && visitatsProfunditat != null) {
            Integer profunditatPrevia = visitatsProfunditat.get(mapa);
            if (profunditatPrevia != null && profunditatPrevia <= profunditatActual) {
                rc.incNodesTallats();
                return null;
            }
            visitatsProfunditat.put(mapa, profunditatActual);
        }
        
        rc.incNodesExplorats();
        rc.updateMemoria(cami.size() + (usarLNT && visitatsProfunditat != null ? visitatsProfunditat.size() : 0));
        

        for (Moviment moviment : mapa.getAccionsPossibles()) {
            Mapa seguentMapa = mapa.mou(moviment);
            
            cami.add(moviment);
            List<Moviment> resultat = dls(seguentMapa, limit, cami, visitatsProfunditat, rc);
            
            if (resultat != null) {
                return resultat;
            }
            
            cami.remove(cami.size() - 1);
        }
        
        return null;
    }
}