package listas;

import kernel.PCB;

import java.util.Comparator;

public class SortBolaCristal implements Comparator<PCB> {

    @Override
    public int compare(PCB o1, PCB o2) {
        if(o1.tamanhoProcesso > o2.tamanhoProcesso) return 1;
        else if(o1.tamanhoProcesso < o2.tamanhoProcesso) return -1;
        else {
            if(o1.idProcesso > o2.idProcesso) return 1;
            else return -1;
        }
    }
}
