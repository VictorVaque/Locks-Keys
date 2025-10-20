package edu.epsevg.prop.ac1.cerca;
 
import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class CercaDFS extends Cerca {
    
    private static final int PROFUNDITAT_MAXIMA = 50;
 
    public CercaDFS(boolean usarLNT) { super(usarLNT); }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        
        rc.startTime();
        Stack<Mapa> pilaEstats = new Stack<>();
        Stack<List<Moviment>> pilaCamins = new Stack<>();

        Set<Mapa> visitats = new HashSet<>();

        

        pilaEstats.push(inicial);
        pilaCamins.push(new ArrayList<>());

        if (usarLNT) {
            visitats.add(inicial);
        }

        while (!pilaEstats.isEmpty()) {

            Mapa mapaActual = pilaEstats.pop();
            List<Moviment> camiActual = pilaCamins.pop();

            rc.incNodesExplorats();
            rc.updateMemoria(pilaEstats.size() + (usarLNT ? visitats.size() : 0));
               
            if (camiActual.size() >= PROFUNDITAT_MAXIMA) {
                rc.incNodesTallats();
                continue;
            }
            
            if (mapaActual.esMeta()) {
                rc.setCami(camiActual);
                rc.stopTime();
                return;
            }

            for (Moviment moviment : mapaActual.getAccionsPossibles()) {
                Mapa seguentMapa = mapaActual.mou(moviment);

                if (usarLNT && visitats.contains(seguentMapa)) {
                    rc.incNodesTallats();
                    continue;
                }

                if (usarLNT) {
                    visitats.add(seguentMapa);
                }

                List<Moviment> seguentCami = new ArrayList<>(camiActual);
                seguentCami.add(moviment);

                pilaEstats.push(seguentMapa);
                pilaCamins.push(seguentCami);
            }
        }

        // No hi ha solució
        rc.stopTime();
    }
}
