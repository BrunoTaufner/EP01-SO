package kernel;

import java.util.*;

import listas.Dispositivos;
import listas.Listas;
import operacoes.Carrega;
import operacoes.Operacao;
import operacoes.OperacaoES;
import operacoes.Soma;

public class SeuSO extends SO {

    Escalonador esc;
    List<PCB> processos = new LinkedList<>();
    public static Listas listsAndQueues = new Listas();
    public boolean CPUexecuting = false;
    int trocaContexto = 0;

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
        addListaDispositivos(proc);
        if(proc.codigo[0] instanceof OperacaoES) {
            proc.estado = PCB.Estado.ESPERANDO;
            listsAndQueues.delListaNovos(proc);
        }
    }

    @Override
    protected void trocaContexto(PCB pcbAtual, PCB pcbProximo) {

    }

    @Override
    // Assuma que 0 <= idDispositivo <= 4
    protected OperacaoES proximaOperacaoES(int idDispositivo) {

        OperacaoES op = null;
        List<Dispositivos> disp = listsAndQueues.getDispositivo(idDispositivo);
        Dispositivos opES = new Dispositivos();
        if(disp.size() > 0) opES = disp.get(0);
        boolean its_possible_to_executeES = true;

        for(PCB proc : processos) {

            if(proc.operacao < proc.codigo.length && proc.codigo[proc.operacao] instanceof OperacaoES && disp.isEmpty()) addListaDispositivos(proc);

            if(!disp.isEmpty()) {
                if (disp.size() > 0) break;
                if (!proc.ESexecuting && !proc.equals(disp.get(0).processo)) {
                    its_possible_to_executeES = false;
                    break;
                }
                if(proc.equals(disp.get(0).processo)) {
                    opES.processo.ESexecuting = true;
                    break;
                }
            }
        }

        if(disp.isEmpty()) return op;

        if(its_possible_to_executeES && processos.size() > 0 && opES.op.idDispositivo == idDispositivo && opES.processo.estado.equals(PCB.Estado.ESPERANDO)) {
            opES.processo.contadorDePrograma++;
            op = opES.op;
            if(opES.op.ciclos == 1)  {
                opES.processo.ESexecuting = false;
                opES.processo.operacao++;
                disp.remove(opES);
            }
            return op;
        }

        return op;
    }

    @Override
    protected Operacao proximaOperacaoCPU() {

        Operacao op = null;
        PCB p;
        if (!processos.isEmpty()) {
            p = processos.get(0);
            if (p.operacao < p.codigo.length) {
                if (p.codigo[p.operacao] instanceof Carrega || p.codigo[p.operacao] instanceof Soma) {
                    if (!p.ESexecuting && p.estado.equals(PCB.Estado.EXECUTANDO)) {
                        op = p.codigo[p.operacao];
                        p.operacao++;
                        p.contadorDePrograma++;
                        CPUexecuting = false;
                        return op;
                    }
                }
            }
        }
        return op;
    }

    @Override
    protected void executaCicloKernel() {
        PCB ant = null;
        for (PCB p : processos) {

            // COLOCA O PROCESSO NA LISTA DE TERMINADOS
            if ((p.estado.equals(PCB.Estado.EXECUTANDO) || p.estado.equals(PCB.Estado.ESPERANDO)) && p.operacao >= p.codigo.length) {
                p.estado = PCB.Estado.TERMINADO;
                if(p.estado.equals(PCB.Estado.ESPERANDO)) listsAndQueues.delListaEsperando(p);
                listsAndQueues.addListaTerminados(p);
                processos.remove(p);
            }

            // PROXIMA OPERAÇÃO É DE ES
            if (p.operacao < p.codigo.length && p.codigo[p.operacao] instanceof OperacaoES) {
                p.estado = PCB.Estado.ESPERANDO;
                listsAndQueues.addListaEsperando(p);
            }

            // PROCESSO PRONTO, ENTÃO PROCESSO PASSA A SER EXECUTANDO
            if (p.operacao < p.codigo.length && !(p.codigo[p.operacao] instanceof OperacaoES) && !CPUexecuting
                    && (p.estado.equals(PCB.Estado.EXECUTANDO) || p.estado.equals(PCB.Estado.PRONTO))) {
                p.estado = PCB.Estado.EXECUTANDO;
                CPUexecuting = true;
                listsAndQueues.delFilaPronto();
            }

            // PROCESSO ESPERANDO VAI PARA PRONTO
            if (p.estado.equals(PCB.Estado.ESPERANDO)
                    && p.operacao < p.codigo.length && !(p.codigo[p.operacao] instanceof  OperacaoES)) {
                p.estado = PCB.Estado.PRONTO;
                if(!CPUexecuting) p.estado = PCB.Estado.EXECUTANDO;
                listsAndQueues.delListaEsperando(p);
                Queue<PCB> prontos = listsAndQueues.getPronto();
                prontos.add(p);
            }

            // PROCESSO NOVO E CONTADOR DE PROGRAMA > 0, ENTÃO PROCESSO PASSA A SER PRONTO
            if (p.estado.equals(PCB.Estado.NOVO) && p.tempoProcesso > 0) {
                p.estado = PCB.Estado.PRONTO;
                listsAndQueues.addFilaPronto(p);
                listsAndQueues.delListaNovos(p);
            }
            ant = p;
        }
        carregaRegistradoresVirtuais(getProcessador()); // vai para troca de contexto
        if(processos.isEmpty()) return;
        switch (esc) {
            case FIRST_COME_FIRST_SERVED:
                listas.Escalonadores.FCFS(processos);
                break;
            case SHORTEST_JOB_FIRST:
                listas.Escalonadores.SJF(processos,listsAndQueues);
                break;
            case SHORTEST_REMANING_TIME_FIRST:
                break;
            case ROUND_ROBIN_QUANTUM_5:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + esc);
        }
        for(PCB proc : processos){
            proc.tempoProcesso++;
            if(proc.estado.equals(PCB.Estado.ESPERANDO)) proc.espera++;
        }

    }

    private void addListaDispositivos(PCB p) {
        boolean achou = false;
        if(listsAndQueues.getDispositivos().isEmpty()) listsAndQueues.inicializaHashMap();
        if(p.operacao < p.codigo.length && p.codigo[p.operacao] instanceof OperacaoES) {
            List<Dispositivos> dispositivo = listsAndQueues.getDispositivo(((OperacaoES) p.codigo[p.operacao]).idDispositivo);
            for (Dispositivos disp : dispositivo) {
                if (disp.processo.idProcesso == p.idProcesso) {
                    achou = true;
                    break;
                }
            }
        }
            if(!achou)
                listsAndQueues.addOperacaoESHashMap(p);
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
    protected List<Integer>idProcessoNovo() {
        Queue<PCB> tarefas = listsAndQueues.getTarefas();
        List<Integer> id_ProcessosNovo = new LinkedList<>();
        for (PCB processo : tarefas) {
            if (processo.estado.equals(PCB.Estado.NOVO)) {
                id_ProcessosNovo.add(processo.idProcesso);
            }
        }
        return id_ProcessosNovo;
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
    protected List<Integer> idProcessoExecutando() {
        Queue<PCB> tarefas = listsAndQueues.getTarefas();
        List<Integer> id_ProcessosExecutando = new LinkedList<>();
        for (PCB processo : tarefas) {
            if (processo.estado.equals(PCB.Estado.EXECUTANDO)) {
                id_ProcessosExecutando.add(processo.idProcesso);
            }
        }
        return id_ProcessosExecutando;
    }

    @Override
    protected List<Integer> idProcessosEsperando() {
        List<Integer> id_ProcessosEsperando = new LinkedList<>();
        for (PCB processo : listsAndQueues.getEsperando()) {
            id_ProcessosEsperando.add(processo.idProcesso);
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
        int x = 0;
        for(PCB proc : listsAndQueues.getTarefas()) {
            x += proc.espera;
        }
        if(listsAndQueues.getTarefas().size() == 0) return 0;
        return x/listsAndQueues.getTarefas().size();
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
        return trocaContexto;
    }

    @Override
    public void defineEscalonador(Escalonador e) {
        this.esc = e;
    }

}
