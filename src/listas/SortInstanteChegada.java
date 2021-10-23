package listas;

import kernel.PCB;

import java.util.Comparator;

public class SortInstanteChegada extends PCB implements Comparator<PCB> {

    @Override
    public int compare(PCB o1, PCB o2) {
        if (o1.instanteChegada > o2.instanteChegada) return 1;
        else if (o1.instanteChegada < o2.instanteChegada) return -1;
        else return 0;
    }
}
