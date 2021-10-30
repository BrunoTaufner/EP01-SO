package listas;

import kernel.PCB;

import java.util.List;

public class Escalonadores {


    public static void SJF(List<PCB> processos) {
        int burstAnt = 5;
        for(PCB p : processos) {
            if(p.cicloBurst > 0) burstAnt = p.calculaTamanhoBurst(burstAnt);
        }
        processos.sort(new SortBurst());
    }
}
