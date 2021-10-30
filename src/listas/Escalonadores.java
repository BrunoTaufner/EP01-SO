package listas;

import kernel.PCB;

import java.util.List;

public class Escalonadores {

    public static void SJF(List<PCB> processos) {
        int proximoChute = 5;
        boolean primeira = true;
        for(PCB p : processos) {
            if(!primeira) p.tempoBurst = proximoChute;
            proximoChute = p.calculaTamanhoBurst(proximoChute);
            primeira = false;

        }
        processos.sort(new SortBurst());
    }

}
