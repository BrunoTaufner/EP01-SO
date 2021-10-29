package listas;

import kernel.PCB;
import operacoes.OperacaoES;

import java.util.List;

public class Escalonadores {


    public static void SJF(List<PCB> processos) {

        processos.sort(new SortIdProcesso());

    }
}
