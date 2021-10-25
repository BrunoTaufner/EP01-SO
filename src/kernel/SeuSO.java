package kernel;

import java.util.*;

import listas.Listas;
import operacoes.Carrega;
import operacoes.Operacao;
import operacoes.OperacaoES;
import operacoes.Soma;

public class SeuSO extends SO {

    Escalonador esc;
    List<PCB> processos = new LinkedList<>();
    Listas listsAndQueues = new Listas();

    @Override
    // ATENÇÃO: cria o processo mas o mesmo
    // só estará "pronto" no próximo ciclo
    protected void criaProcesso(Operacao[] codigo) {

        PCB proc = new PCB();
        proc.codigo = codigo;
        proc.idProcesso = getContadorCiclos();
        processos.add(proc);
        processos.get(processos.size() - 1).instanteChegada = getContadorCiclos();
        listsAndQueues.addFilaNovo(proc);
        listsAndQueues.addFilaTarefas(proc);
        Collections.addAll(proc.operacoes, proc.codigo);
        proc.calculaCicloBurst();

    }

    @Override
    protected void trocaContexto(PCB pcbAtual, PCB pcbProximo) {

    }

    @Override
    // Assuma que 0 <= idDispositivo <= 4
    protected OperacaoES proximaOperacaoES(int idDispositivo) {

        OperacaoES op = null;
        List<OperacaoES> disp = listsAndQueues.getDispositivo(idDispositivo);
        if(disp != null && !disp.isEmpty()) {
            if(!processos.isEmpty() && !processos.get(0).estado.equals(PCB.Estado.NOVO) && !processos.get(0).estado.equals(PCB.Estado.PRONTO)){
                op = disp.get(0);
                if(op.ciclos > 1) processos.get(0).ESexecuting = true;
                else if (op.ciclos == 1) {
                    processos.get(0).ESexecuting = false;
                    processos.get(0).operacoes.poll();
                    op = disp.remove(0);
                }
            }
        }

        return op;
    }

    @Override
    protected Operacao proximaOperacaoCPU() {

        Operacao op = null;
        if (!processos.isEmpty() && !processos.get(0).operacoes.isEmpty()) {
            processos.get(0).contadorDePrograma++;
            if (processos.get(0).operacoes.peek() instanceof Carrega || processos.get(0).operacoes.peek() instanceof Soma) {
                if (processos.get(0).estado.equals(PCB.Estado.EXECUTANDO) && processos.get(0).ESexecuting == false) op = processos.get(0).operacoes.poll();
            }
        }

        return op;
    }

    @Override
    protected void executaCicloKernel() {
        carregaRegistradoresVirtuais(getProcessador()); // vai para troca de contexto
        if(listsAndQueues.getDispositivos().isEmpty()) listsAndQueues.inicializaHashMap();
        if(processos.isEmpty()) return;
        if(!processos.get(0).operacoes.isEmpty() && processos.get(0).operacoes.peek() instanceof OperacaoES
                && !listsAndQueues.getDispositivo(((OperacaoES) processos.get(0).operacoes.peek()).idDispositivo).contains(processos.get(0).operacoes.peek()))
            listsAndQueues.addOperacaoESHashMap((OperacaoES) processos.get(0).operacoes.peek());
        switch (esc) {
            case FIRST_COME_FIRST_SERVED:
                listas.Escalonadores.FCFS(processos,listsAndQueues);
                break;
            case SHORTEST_JOB_FIRST:
                
                break;
            case SHORTEST_REMANING_TIME_FIRST:
                break;
            case ROUND_ROBIN_QUANTUM_5:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + esc);
        }
        for(PCB proc : processos)
            proc.tempoProcesso++;
    }

    private void carregaRegistradoresVirtuais(Processador processador) {
        if(!processos.isEmpty()) {
            System.arraycopy(processador.registradores, 0, processos.get(0).registradores, 0, processador.registradores.length);
        }
    }

    @Override
    protected boolean temTarefasPendentes() {
        return !processos.isEmpty();
    }

    @Override
    protected Integer idProcessoNovo() {
        Queue<PCB> tarefas = listsAndQueues.getTarefas();
        int i = 0;
        for (PCB processo : tarefas) {
            if (processo.estado.equals(PCB.Estado.NOVO)) {
                i++;
            }
        }
        return i;
    }

    @Override
    protected List<Integer> idProcessosProntos() {
        List<Integer> id_ProcessosProntos = new LinkedList<>();
        Queue<PCB> pronto = listsAndQueues.getPronto();
        for (PCB processo : pronto) {
            if (processo.estado.equals(PCB.Estado.PRONTO)) {
                id_ProcessosProntos.add(processo.idProcesso);
            }
        }
        return id_ProcessosProntos;
    }

    @Override
    protected Integer idProcessoExecutando() {
        Queue<PCB> tarefas = listsAndQueues.getTarefas();
        int i = 0;
        for (PCB processo : tarefas) {
            if (processo.estado.equals(PCB.Estado.EXECUTANDO)) {
                i++;
            }
        }
        return i;
    }

    @Override
    protected List<Integer> idProcessosEsperando() {
        List<Integer> id_ProcessosEsperando = new LinkedList<>();
        Queue<PCB> tarefas = listsAndQueues.getTarefas();
        for (PCB processo : tarefas) {
            if (processo.estado.equals(PCB.Estado.ESPERANDO)) {
                id_ProcessosEsperando.add(processo.idProcesso);
            }
        }
        return id_ProcessosEsperando;
    }

    @Override
    protected List<Integer> idProcessosTerminados() {
        List<Integer> id_ProcessosTerminados = new LinkedList<>();
        List<PCB> tarefas = listsAndQueues.getTerminados();
        for (PCB processo : tarefas) {
            if (processo.estado.equals(PCB.Estado.TERMINADO)) {
                id_ProcessosTerminados.add(processo.idProcesso);
            }
        }
        return id_ProcessosTerminados;
    }

    @Override
    protected int tempoEsperaMedio() {

        return 0;
    }

    @Override
    protected int tempoRespostaMedio() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected int tempoRetornoMedio() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected int trocasContexto() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void defineEscalonador(Escalonador e) {
        this.esc = e;
    }

}
