package listas;

import kernel.PCB;

import java.util.List;

public class Escalonadores {

    public static void FCFS(List<PCB> processos, Listas listsAndQueues) {

        //processos.sort(new SortIdProcesso());

        for (PCB p : processos) {

            // PROCESSO TERMINOU, ENTÃO:
            // 1) REMOVO ELE DA LISTA DE EXECUTANDO
            // 2) ADICIONO NA LISTA DE TERMINADOS
            // 3) REMOVO DA LISTA DE PROCESSOS

            if (p.estado.equals(PCB.Estado.EXECUTANDO) && p.operacao >= p.codigo.length) {
                p.estado = PCB.Estado.TERMINADO;
                listsAndQueues.addListaTerminados(p);
                processos.remove(p);
                if(!processos.isEmpty()) {
                    p = processos.get(0);
                }

            }
            // PROCESSO NOVO E CONTADOR DE PROGRAMA > 0, ENTÃO PROCESSO PASSA A SER PRONTO
            if (p.estado.equals(PCB.Estado.NOVO) && p.tempoProcesso > 0) {
                p.estado = PCB.Estado.PRONTO;
                listsAndQueues.addFilaPronto(p);
                listsAndQueues.delListaNovos(p);
            }

        }
    }
}
