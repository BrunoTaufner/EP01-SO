package escalonadores;

import kernel.*;
import operacoes.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Escalona {
    Queue<PCB> tarefas = new LinkedList<>(); //FILA COM TODOS OS PROCESSOS DO SISTEMA
    Queue<PCB> pronto = new LinkedList<>(); //FILA COM TODOS OS PROCESSOS QUE ESTÃO PRONTOS
    Queue<PCB> dispositivos = new LinkedList<>(); //FILA COM TODOS OS PROCESSOS ESPERANDO POR UM DISPOSITIVO DE ENTRADA E SAÍDA
    List<PCB> terminados = new LinkedList<>(); //FILA DE TODOS OS PROCESSOS TERMINADOS


    public Escalona(){

    }

    public void addFilaTarefas(PCB processo){
        tarefas.add(processo);
    }

    public void addFilaPronto(PCB processo){
        pronto.add(processo);
    }

    public void addFilaDispositivos(PCB processo){
        dispositivos.add(processo);
    }

    public void addListaTerminados(PCB processo) { terminados.add(processo); }

    public void delFilaTarefas(){
        if(!tarefas.isEmpty()) tarefas.poll();
    }

    public void delFilaPronto(){
        if(!pronto.isEmpty()) pronto.poll();
    }

    public void delFilaDispositivos(){
        if(!dispositivos.isEmpty()) dispositivos.poll();
    }

    public Queue<PCB> getTarefas() {
        return tarefas;
    }

    public Queue<PCB> getPronto() {
        return pronto;
    }

    public List<PCB> getTerminados() {
        return terminados;
    }


}
