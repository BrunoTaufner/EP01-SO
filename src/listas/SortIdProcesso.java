package listas;

import kernel.PCB;

import java.util.Comparator;

public class SortIdProcesso extends PCB implements Comparator<PCB> {

    @Override
    public int compare(PCB o1, PCB o2) {
        return Integer.compare(o1.idProcesso, o2.idProcesso);
    }

}
