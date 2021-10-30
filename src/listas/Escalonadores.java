package listas;

import kernel.PCB;

import java.util.List;

public class Escalonadores {

    public static void SJF(List<PCB> processos, Listas listsAndQueues) {
        for(PCB p : processos) {
            if(p.cicloBurst > 0) {
                p.tempoBurst  = p.calculaTamanhoBurst(p.tempoBurst);
                if(!p.executouBurstInicial) p.executouBurstInicial = true;
                p.cicloBurst = 0;
                p.contadorBurst = 0;
            }
        }
        listsAndQueues.getPronto().sort(new SortBurst());
    }

}
