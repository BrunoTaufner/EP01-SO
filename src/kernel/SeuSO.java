package kernel;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import escalonadores.Escalona;
import operacoes.Carrega;
import operacoes.Operacao;
import operacoes.OperacaoES;
import operacoes.Soma;


public class SeuSO extends SO {

    Escalonador esc;
    Queue<PCB> processos = new LinkedList<>();
    Escalona escalona = new Escalona();
    int idProcesso = 0;


    @Override
    // ATENCÃO: cria o processo mas o mesmo
    // só estará "pronto" no próximo ciclo
    protected void criaProcesso(Operacao[] codigo) {

        PCB proc = new PCB();
        proc.codigo = codigo;
        proc.contadorCiclos = getContadorCiclos();
        proc.idProcesso = idProcessoNovo();
        processos.add(proc);
        escalona.addFilaTarefas(proc);
        for(Operacao p : proc.codigo) {
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

        if (!processos.isEmpty() /*&& processos.peek().estado.equals(PCB.Estado.EXECUTANDO)*/) {
            for (OperacaoES op : processos.peek().opES) {
                if (op.idDispositivo == idDispositivo) {
                    incrementaContadorCiclos();
                    return op;
                }
            }
        }
        return null;
    }

    @Override
    protected Operacao proximaOperacaoCPU() {
        Operacao op = null;
        if (!processos.isEmpty() && !processos.peek().opCarregaSoma.isEmpty() /*&& processos.peek().estado.equals(PCB.Estado.EXECUTANDO)*/) {
            incrementaContadorCiclos();
            op = processos.peek().opCarregaSoma.poll();
        }
        return op;
    }

    @Override
    protected void executaCicloKernel() {
        if(!processos.isEmpty() && processos.peek().estado.equals(PCB.Estado.EXECUTANDO) && processos.peek().opES.isEmpty() && processos.peek().opCarregaSoma.isEmpty()) {
            processos.peek().estado = PCB.Estado.TERMINADO;
        }
        if (!processos.isEmpty() && processos.peek().estado.equals(PCB.Estado.TERMINADO)) {
            escalona.addListaTerminados(processos.poll());
        }
        if(processos.isEmpty()) return;
        switch (esc) {
            case FIRST_COME_FIRST_SERVED:

                if (processos.peek().estado.equals(PCB.Estado.NOVO) && processos.peek().contadorDePrograma > 0) {
                    processos.peek().estado = PCB.Estado.PRONTO;
                    break;
                } else if (processos.peek().estado.equals(PCB.Estado.PRONTO) && processos.peek().contadorDePrograma > 1) {
                    processos.poll();
                    processos.peek().estado = PCB.Estado.EXECUTANDO;
                    break;
                }
                break;
            case SHORTEST_JOB_FIRST:
                break;
            case SHORTEST_REMANING_TIME_FIRST:
                break;
            case ROUND_ROBIN_QUANTUM_5:
                break;
        }
        processos.peek().contadorDePrograma++;

    }

    @Override
    protected boolean temTarefasPendentes() {
        for (PCB proc : processos) {
            if (!proc.estado.equals(PCB.Estado.TERMINADO)) return true;
        }
        return false;
    }

    @Override
    protected Integer idProcessoNovo() {
        idProcesso++;
        return idProcesso - 1;
    }

    @Override
    protected List<Integer> idProcessosProntos() {
        List<Integer> id_ProcessosProntos = new LinkedList<>();
        Queue<PCB> tarefas = escalona.getTarefas();
        for (PCB processo : tarefas) {
            if (processo.estado.equals(PCB.Estado.PRONTO)) {
                id_ProcessosProntos.add(processo.idProcesso);
            }
        }
        return id_ProcessosProntos;
    }

    @Override
    protected Integer idProcessoExecutando() {
        Queue<PCB> tarefas = escalona.getTarefas();
        for (PCB processo : tarefas) {
            if (processo.estado.equals(PCB.Estado.EXECUTANDO)) {
                return processo.idProcesso;
            }
        }
        return -1;
    }

    @Override
    protected List<Integer> idProcessosEsperando() {
        List<Integer> id_ProcessosEsperando = new LinkedList<>();
        Queue<PCB> tarefas = escalona.getTarefas();
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
        Queue<PCB> tarefas = escalona.getTarefas();
        for (PCB processo : tarefas) {
            if (processo.estado.equals(PCB.Estado.TERMINADO)) {
                id_ProcessosTerminados.add(processo.idProcesso);
            }
        }
        return id_ProcessosTerminados;
    }

    @Override
    protected int tempoEsperaMedio() {
        // TODO Auto-generated method stub
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
