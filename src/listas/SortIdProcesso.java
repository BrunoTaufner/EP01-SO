package listas;

import kernel.PCB;
import kernel.SeuSO;

import java.util.Comparator;

public class SortIdProcesso extends PCB implements Comparator<PCB> {

    @Override
    public int compare(PCB o1, PCB o2) {
        if (o1.idProcesso > o2.idProcesso) return 1;
        else if(o1.idProcesso < o2.idProcesso) return -1;
        else return 0;
    }

}
