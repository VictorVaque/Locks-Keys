package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;


public class CercaBFS extends Cerca {
    public CercaBFS(boolean usarLNT) { super(usarLNT); }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        
        rc.startTime();
        Queue<Mapa> queue = new LinkedList<>();
        Queue<List<Moviment>> pathQueue = new LinkedList<>();
        Set<Mapa> visited = new HashSet<>();

        queue.add(inicial);
        pathQueue.add(new ArrayList<>()); // Camí buit inicial
        if (usarLNT) {
            visited.add(inicial);
        }

        
        while (!queue.isEmpty()) {
            // node actual
            Mapa currentMap = queue.poll();
            List<Moviment> currentPath = pathQueue.poll();
            rc.incNodesExplorats();
            rc.updateMemoria(queue.size() + (usarLNT ? visited.size() : 0));

            
            if (currentMap.esMeta()) {
                rc.setCami(currentPath);
                rc.stopTime();
                return; // Solució trobada
            }

            // nodes fills
            for (Moviment mov : currentMap.getAccionsPossibles()) {
                Mapa nextMap = currentMap.mou(mov);

                if (usarLNT) {
                    if (visited.contains(nextMap)) {
                        rc.incNodesTallats();
                        continue; // Node ja visitat, l'ignorem
                    }
                    visited.add(nextMap);
                }

                // nou camí
                List<Moviment> newPath = new ArrayList<>(currentPath);
                newPath.add(mov);

                queue.add(nextMap);
                pathQueue.add(newPath);
            }
        }

        // Si no hi ha solució
        rc.stopTime();

    }
   
}
