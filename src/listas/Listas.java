package listas;

import kernel.*;
import operacoes.OperacaoES;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Listas implements Comparable<PCB>{

    Queue<PCB> tarefas = new LinkedList<>(); //FILA COM TODOS OS PROCESSOS DO SISTEMA
    List<PCB> pronto = new LinkedList<>(); //FILA COM TODOS OS PROCESSOS QUE ESTÃO PRONTOS
    List<PCB> novos = new LinkedList<>(); //FILA COM TODOS OS PROCESSOS QUE ESTÃO NOVOS
    List<PCB> terminados = new LinkedList<>(); //FILA DE TODOS OS PROCESSOS TERMINADOS
    List<PCB> processosCPU = new LinkedList<>(); // LISTAS COM TODOS OS PROCESSOS ESPERANDO PELA CPU OU EXECUTANDO NA CPU
    List<PCB> esperando = new LinkedList<>();

    HashMap<Integer,List<Dispositivos>> dispositivos = new HashMap<>(); //FILA COM TODOS OS PROCESSOS ESPERANDO POR UM DISPOSITIVO DE ENTRADA E SAÍDA

    public List<PCB> getEsperando() {
        return esperando;
    }

    public List<PCB> getNovos() {
        return novos;
    }

    public void addListaProcessosCPU(PCB p) {
        this.processosCPU.add(p);
    }

    public List<PCB> getProcessosCPU() {
        return processosCPU;
    }

    public void delListaProcessosCPU(PCB p) {
        this.processosCPU.remove(p);
    }

    public void addFilaTarefas(PCB processo){
        tarefas.add(processo);
    }

    public void addListaEsperando(PCB processo) {
        esperando.add(processo);
    }

    public void delListaEsperando(PCB processo) {
        esperando.remove(processo);
    }

    public void addListaPronto(PCB processo){
        pronto.add(processo);
    }

    public void addFilaNovo(PCB processo){
        novos.add(processo);
    }

    public void inicializaHashMap() {
        if(dispositivos.size() == 0) {
            List<Dispositivos> lista1 = new LinkedList<>();
            List<Dispositivos> lista2 = new LinkedList<>();
            List<Dispositivos> lista3 = new LinkedList<>();
            List<Dispositivos> lista4 = new LinkedList<>();
            List<Dispositivos> lista5 = new LinkedList<>();
            dispositivos.put(0,lista1);
            dispositivos.put(1,lista2);
            dispositivos.put(2,lista3);
            dispositivos.put(3,lista4);
            dispositivos.put(4,lista5);
        }
    }


    // ADD OPERAÇÃO NO DISPOSITIVO
    public void addOperacaoES(PCB pro) {
        Dispositivos op = new Dispositivos();
        if(pro.codigo[pro.operacao] instanceof OperacaoES) {
            op.op = (OperacaoES) pro.codigo[pro.operacao];
            op.processo = pro;
            List<Dispositivos> lista = dispositivos.get(op.op.idDispositivo);
            lista.add(op);
        }
    }

    public void addListaTerminados(PCB processo) { terminados.add(processo); }

    public void delListaPronto(PCB processo){
        if(!pronto.isEmpty()) pronto.remove(processo);
    }

    public void delListaNovos(PCB processo) {
        if(!pronto.isEmpty()) novos.remove(processo);
    }

    public Queue<PCB> getTarefas() {
        return tarefas;
    }

    public List<PCB> getPronto() {
        return pronto;
    }

    public List<PCB> getTerminados() {
        return terminados;
    }

    public HashMap<Integer, List<Dispositivos>> getDispositivos() {
        return dispositivos;
    }

    public List<Dispositivos> getDispositivo(int index) {
        if (SeuSO.listsAndQueues.dispositivos.isEmpty()) inicializaHashMap();
        return dispositivos.get(index);
    }


    @Override
    public int compareTo(PCB o) {
        return 0;
    }
}
