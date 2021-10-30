package listas;

import kernel.PCB;

import java.util.Comparator;

public class SortBurst implements Comparator<PCB> {

    /*
    public int compare(PCB o1, PCB o2) {
        if(o1.tempoBurst > o2.tempoBurst) return 1;
        else if(o1.tempoBurst < o2.tempoBurst) return 1;
        else {
            if(o1.idProcesso > o2.idProcesso) return -1;
            else return -1;
        }
    }
    */
    public int compare(PCB o1, PCB o2) {
        if(o1.operacao > o2.operacao) return 1;
        else if(o1.operacao < o2.operacao) return -1;
        else return -1;
    }
}
