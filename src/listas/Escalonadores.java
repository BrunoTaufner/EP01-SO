package listas;

import kernel.PCB;
import kernel.SeuSO;

import java.util.LinkedList;
import java.util.List;

public class Escalonadores {

    public static void SJF(List<PCB> processos, Listas listsAndQueues) {
        for(PCB p : processos) {
            if(p.cicloBurst > 0) {
                p.tempoBurst  = p.calculaTamanhoBurst(p.tempoBurst);
                p.estimativaBurst = p.tempoBurst;
                p.cicloBurst = 0;
                p.contadorBurst = 0;
            }
        }
        listsAndQueues.getPronto().sort(new SortBurst());
    }

    public static void SRTF(List<PCB> processos, Listas listsAndQueues) {

        for(PCB p : processos) {
            if(p.cicloBurst > 0) {
                p.tempoBurst  = p.calculaTamanhoBurst(p.tempoBurst);
                p.estimativaBurst = p.tempoBurst;
                p.cicloBurst = 0;
                p.contadorBurst = 0;
            }
        }
        List<PCB> procsCPU = listsAndQueues.getProcessosCPU();

        procsCPU.sort(new SortBurst());
        listsAndQueues.getPronto().sort(new SortBurst());

        SeuSO contexto = new SeuSO();
        if(!procsCPU.isEmpty() && procsCPU.get(0).estado.equals(PCB.Estado.PRONTO) && SeuSO.CPUexecuting) {
            PCB executando = null;
            for(PCB p : procsCPU) {
                if(p.estado.equals(PCB.Estado.EXECUTANDO)) executando = p;
            }
            contexto.trocaContexto(procsCPU.get(0), executando);
        }
    }

}
