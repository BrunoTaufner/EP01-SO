package listas;

import kernel.PCB;
import kernel.SeuSO;
import operacoes.OperacaoES;

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

    public static void SRTF(List<PCB> processos, Listas listsAndQueues, boolean CPUexecuting) {

        for(PCB p : processos) {
            if(p.fimBurstOperacaoES) {
                p.tempoBurst  = p.calculaTamanhoBurst(p.tempoBurst);
                p.estimativaBurst = p.tempoBurst;
                p.contadorBurst = 0;
                p.cicloBurst = 0;
                p.fimBurstOperacaoES = false;
            }
        }
        List<PCB> procsCPU = listsAndQueues.getProcessosCPU();
        procsCPU.sort(new SortBurst());

        listsAndQueues.getPronto().sort(new SortBurst());

        SeuSO contexto = new SeuSO();
        if(!procsCPU.isEmpty() && procsCPU.get(0).estado.equals(PCB.Estado.PRONTO) && CPUexecuting) {
            PCB executando = null;
            for(PCB p : procsCPU) {
                if(p.estado.equals(PCB.Estado.EXECUTANDO)) executando = p;
            }
            contexto.trocaContexto(procsCPU.get(0), executando);
        }
    }

    public static void RRQ5(List<PCB> processos, Listas listsAndQueues) {
        SeuSO so = new SeuSO();
        for(PCB p : processos) {
            if(p.estado.equals(PCB.Estado.EXECUTANDO)){
                // CASO TERMINOU OS 5 CICLOS
                if(p.contadorCiclos == 5) {
                    if(p.operacao < p.codigo.length && !(p.codigo[p.operacao] instanceof OperacaoES)) {
                        if(listsAndQueues.getPronto().size() > 0){
                            so.trocaContexto(p,listsAndQueues.getPronto().get(0));
                            p.contadorCiclos = 0;
                        }
                    }
                }
            }
        }
    }
}
