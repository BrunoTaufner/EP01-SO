package listas;

import kernel.PCB;
import operacoes.OperacaoES;

import java.util.Comparator;

public class Dispositivos extends PCB implements Comparator<PCB> {
    public PCB processo;
    public OperacaoES op;


    @Override
    public int compare(PCB o1, PCB o2) {
        return Integer.compare(o1.idProcesso, o2.idProcesso);
    }
}
