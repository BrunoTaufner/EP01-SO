package kernel;

import java.util.*;

import listas.Listas;
import listas.SortInstanteChegada;
import operacoes.Carrega;
import operacoes.Operacao;
import operacoes.OperacaoES;
import operacoes.Soma;


public class SeuSO extends SO {

    Escalonador esc;
    List<PCB> processos = new LinkedList<>();
    Listas listsandqueues = new Listas();
    int idProcesso = 0;

    @Override
    // ATENCÃO: cria o processo mas o mesmo
    // só estará "pronto" no próximo ciclo
    protected void criaProcesso(Operacao[] codigo) {
        PCB proc = new PCB();
        proc.codigo = codigo;
        proc.instanteChegada = getContadorCiclos();
        idProcesso++;
        proc.idProcesso = idProcesso;
        processos.add(proc);
        listsandqueues.addFilaTarefas(proc);

        for(Operacao p : proc.codigo) { // troca de contexto, vai para lá
            if(p instanceof Soma || p instanceof Carrega) {
                proc.opCarregaSoma.add(p);
            }
            else if(p instanceof OperacaoES) {
                proc.opES.add((OperacaoES) p);
            }
        }

    }

    @Override
    protected void trocaContexto(PCB pcbAtual, PCB pcbProximo) {

    }

    @Override
    // Assuma que 0 <= idDispositivo <= 4
    protected OperacaoES proximaOperacaoES(int idDispositivo) {
        if (!processos.isEmpty() && processos.get(0).estado.equals(PCB.Estado.EXECUTANDO)) {
            for (OperacaoES op : processos.get(0).opES) {
                if (op.idDispositivo == idDispositivo) {
                    if(op.ciclos <= 1) {
                        if(!processos.get(0).opES.isEmpty()) processos.get(0).opES.remove(op);
                        return null;
                    }
                    return op;
                }
            }
        }
        return null;
    }

    @Override
    protected Operacao proximaOperacaoCPU() {
        if(!processos.isEmpty()) processos.get(0).contadorDePrograma++;
        Operacao op = null;
        if (!processos.isEmpty() && !processos.get(0).opCarregaSoma.isEmpty() && processos.get(0).estado.equals(PCB.Estado.EXECUTANDO)) {
            if(processos.get(0).opCarregaSoma.size() > 1
                    || (processos.get(0).opES.size() == 1 && processos.get(0).opES.get(0).ciclos == 1)
                        || (processos.get(0)).opES.isEmpty() && processos.get(0).opCarregaSoma.size() == 1)
            {
                op = processos.get(0).opCarregaSoma.poll();
            }
            else return processos.get(0).opCarregaSoma.peek();
        }
        else if (processos.isEmpty() || !processos.get(0).estado.equals(PCB.Estado.EXECUTANDO)) return getCarregavazia();
        return op;
    }

    @Override
    protected void executaCicloKernel() {
        carregaRegistradoresVirtuais(getProcessador());
        if(!processos.isEmpty() && processos.get(0).estado.equals(PCB.Estado.EXECUTANDO) && processos.get(0).opES.isEmpty() && processos.get(0).opCarregaSoma.isEmpty()) {
            processos.get(0).estado = PCB.Estado.TERMINADO;
            listsandqueues.addListaTerminados(processos.get(0));
        }
        if (!processos.isEmpty() && processos.get(0).estado.equals(PCB.Estado.TERMINADO)) {
            processos.remove(0);
        }
        if(processos.isEmpty()) return;
        switch (esc) {
            case FIRST_COME_FIRST_SERVED:
                if (processos.get(0).estado.equals(PCB.Estado.NOVO) && processos.get(0).contadorDePrograma > 0) {
                    processos.get(0).estado = PCB.Estado.PRONTO;
                    listsandqueues.addFilaPronto(processos.get(0));
                    processos.sort(new SortInstanteChegada());
                    break;
                } else if (processos.get(0).estado.equals(PCB.Estado.PRONTO) && processos.get(0).contadorDePrograma > 1) {
                    processos.get(0).estado = PCB.Estado.EXECUTANDO;
                    listsandqueues.delFilaPronto();
                    break;
                }
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
        Queue<PCB> tarefas = listsandqueues.getTarefas();
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
        Queue<PCB> pronto = listsandqueues.getPronto();
        for (PCB processo : pronto) {
            if (processo.estado.equals(PCB.Estado.PRONTO)) {
                id_ProcessosProntos.add(processo.idProcesso);
            }
        }
        return id_ProcessosProntos;
    }

    @Override
    protected Integer idProcessoExecutando() {
        Queue<PCB> tarefas = listsandqueues.getTarefas();
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
        Queue<PCB> tarefas = listsandqueues.getTarefas();
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
        List<PCB> tarefas = listsandqueues.getTerminados();
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

    public Carrega getCarregavazia() {
        return new Carrega(0, 0);
    }

}
