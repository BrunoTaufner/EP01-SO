package kernel;

import java.util.*;

import listas.Dispositivos;
import listas.Listas;
import listas.SortBurst;
import listas.SortIdProcesso;
import operacoes.Carrega;
import operacoes.Operacao;
import operacoes.OperacaoES;
import operacoes.Soma;

public class SeuSO extends SO {

    public static Escalonador esc;

    public List<PCB> processos = new LinkedList<>();

    public static Listas listsAndQueues = new Listas();
    public boolean CPUexecuting = false;
    static int trocaContexto = 0;
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
    }

    @Override
    public void trocaContexto(PCB pcbAtual, PCB pcbProximo) {
        switch (esc) {
            case SHORTEST_REMANING_TIME_FIRST:
                listsAndQueues.getProcessosCPU().sort(new SortBurst());
                pcbAtual.estado = PCB.Estado.EXECUTANDO;
                listsAndQueues.delListaPronto(pcbAtual);
                pcbProximo.estado = PCB.Estado.PRONTO;
                listsAndQueues.addListaPronto(pcbProximo);
                listsAndQueues.getPronto().sort(new SortIdProcesso());
                trocaContexto += 1;
                break;
            case ROUND_ROBIN_QUANTUM_5:
                listsAndQueues.addListaPronto(pcbAtual);
                pcbAtual.estado = PCB.Estado.PRONTO;
                listsAndQueues.delListaPronto(pcbProximo);
                pcbProximo.estado = PCB.Estado.EXECUTANDO;
                trocaContexto += 1;
                break;
        }
    }

    @Override
    // Assuma que 0 <= idDispositivo <= 4
    protected OperacaoES proximaOperacaoES(int idDispositivo) {

        //if(CPUexecuting) return null;
        OperacaoES op = null;
        List<Dispositivos> disp = listsAndQueues.getDispositivo(idDispositivo);
        if(disp.isEmpty()) return op;

        Dispositivos opES = disp.get(0);
        if(opES.op.idDispositivo == idDispositivo
                && opES.processo.estado.equals(PCB.Estado.ESPERANDO)) {
            opES.processo.contadorDePrograma++;
            op = opES.op;
            opES.processo.tamanhoProcesso--;
            if(opES.op.ciclos == 1) {
                opES.processo.ESexecuting = false;
                opES.processo.operacao++;
                if(opES.processo.operacao < opES.processo.codigo.length && !(opES.processo.codigo[opES.processo.operacao] instanceof  OperacaoES) && opES.processo.cicloBurst > 0) {
                    opES.processo.fimBurstOperacaoES = true;
                    opES.processo.entrou = false;
                }
                disp.remove(opES);
            }
            return op;
        }

        return op;
    }

    @Override
    protected Operacao proximaOperacaoCPU() {

        Operacao op = null;

        for (PCB p : processos){
            if (p.estado.equals(PCB.Estado.EXECUTANDO)) {
                if(p.codigo[p.operacao] instanceof Carrega || p.codigo[p.operacao] instanceof Soma) {
                    if (!p.ESexecuting) {
                        op = p.codigo[p.operacao];
                        p.operacao++;
                        p.contadorDePrograma++;
                        p.estimativaBurst--;
                        p.contadorCiclos++;
                        p.tamanhoProcesso--;
                        if(p.cicloBurst == 0) p.contadorBurst++;
                        // SAI DA CPU
                        if(p.operacao >= p.codigo.length || p.codigo[p.operacao] instanceof OperacaoES) {
                            CPUexecuting = false;
                            p.cicloBurst++;
                            p.estimativaBurst = p.tempoBurst;
                        }
                        return op;
                    }
                }
            }
        }
        return op;
    }

    @Override
    protected void executaCicloKernel() {
        List<PCB.Estado> estados = criaEstados();
        for(PCB.Estado estado : estados) {
            for (int i = 0; i < processos.size(); i++) {
                escalonadores();
                PCB p = processos.get(i);
                if (p.estado.equals(estado)) {
                    // PROCESSO TERMINADO
                    if (p.operacao >= p.codigo.length) {
                        if (p.estado.equals(PCB.Estado.NOVO)) {
                            listsAndQueues.delListaNovos(p);
                        }
                        else if (p.estado.equals(PCB.Estado.ESPERANDO)) {
                            listsAndQueues.delListaEsperando(p);
                        }
                        else if(p.estado.equals(PCB.Estado.EXECUTANDO)) {
                            listsAndQueues.delListaProcessosCPU(p);
                        }
                        p.estado = PCB.Estado.TERMINADO;
                        listsAndQueues.addListaTerminados(p);
                        p.retorno = getContadorCiclos() - p.idProcesso;
                        processos.remove(p);

                    } else {
                        // PROCESSO NOVO
                        if (p.estado.equals(PCB.Estado.NOVO) && p.tempoProcesso > 0) {
                            p.estado = PCB.Estado.PRONTO;
                            listsAndQueues.addListaPronto(p);
                            listsAndQueues.delListaNovos(p);
                        }

                        // PROCESSO PRONTO
                        if (p.estado.equals(PCB.Estado.PRONTO)) {
                            // PRONTO PARA EXECUTANDO
                            if (!CPUexecuting && !(p.codigo[p.operacao] instanceof OperacaoES) && listsAndQueues.getPronto().get(0).equals(p)) {
                                p.estado = PCB.Estado.EXECUTANDO;
                                CPUexecuting = true;
                                listsAndQueues.delListaPronto(p);
                                if(!listsAndQueues.getProcessosCPU().contains(p)) listsAndQueues.addListaProcessosCPU(p);
                            }
                            // PRONTO PARA ESPERANDO
                            else if (p.codigo[p.operacao] instanceof OperacaoES) {
                                p.estado = PCB.Estado.ESPERANDO;
                                listsAndQueues.addListaEsperando(p);
                                addListaDispositivos(p);
                                listsAndQueues.delListaPronto(p);
                                listsAndQueues.delListaProcessosCPU(p);
                            }
                        }
                        // PROCESSO EXECUTANDO
                        else if (p.estado.equals(PCB.Estado.EXECUTANDO)) {
                            // EXECUTANDO PARA ESPERANDO;
                            if (p.codigo[p.operacao] instanceof OperacaoES) {
                                p.estado = PCB.Estado.ESPERANDO;
                                listsAndQueues.addListaEsperando(p);
                                addListaDispositivos(p);
                                listsAndQueues.delListaProcessosCPU(p);
                                p.contadorCiclos = 0;
                            }
                        }
                        // PROCESSO ESPERANDO
                        else if (p.estado.equals(PCB.Estado.ESPERANDO)) {
                            if (p.codigo[p.operacao] instanceof OperacaoES) addListaDispositivos(p);
                            // ESPERANDO PARA EXECUTANDO
                            if (!(p.codigo[p.operacao] instanceof OperacaoES) && !CPUexecuting && listsAndQueues.getPronto().isEmpty()) {
                                listsAndQueues.delListaEsperando(p);
                                if(!listsAndQueues.getProcessosCPU().contains(p)) listsAndQueues.addListaProcessosCPU(p);
                                p.estado = PCB.Estado.EXECUTANDO;
                                CPUexecuting = true;
                            }
                            // ESPERANDO PARA PRONTO
                            else if (!(p.codigo[p.operacao] instanceof OperacaoES)) {
                                listsAndQueues.delListaEsperando(p);
                                p.estado = PCB.Estado.PRONTO;
                                listsAndQueues.addListaPronto(p);
                                if(!listsAndQueues.getProcessosCPU().contains(p)) listsAndQueues.addListaProcessosCPU(p);
                            }
                        }
                    }
                }
            }
        }
        for(PCB proc : processos){
            if(proc.estado.equals((PCB.Estado.PRONTO))) {
                proc.espera++;
                if(!proc.executed) proc.resposta++;
            }
            if(proc.estado.equals(PCB.Estado.EXECUTANDO) && !proc.executed) proc.executed = true;
            proc.tempoProcesso++;
        }
    }

    private List<PCB.Estado> criaEstados() {
        List<PCB.Estado> estados = new LinkedList<>();
        estados.add(PCB.Estado.NOVO);
        estados.add(PCB.Estado.ESPERANDO);
        estados.add(PCB.Estado.PRONTO);
        estados.add(PCB.Estado.EXECUTANDO);
        return estados;
    }

    private void escalonadores() {
        carregaRegistradoresVirtuais(getProcessador());
        if(processos.isEmpty()) return;
        switch (esc) {
            case SHORTEST_JOB_FIRST:
                listas.Escalonadores.SJF(processos, listsAndQueues);
                break;
            case SHORTEST_REMANING_TIME_FIRST:
                listas.Escalonadores.SRTF(processos, listsAndQueues, CPUexecuting);
                break;
            case ROUND_ROBIN_QUANTUM_5:
                listas.Escalonadores.RRQ5(processos, listsAndQueues);
                break;
            case SHORTEST_JOB_FIRST_BOLA_DE_CRISTAL:
                listas.Escalonadores.SJFBolaDeCristal(processos, listsAndQueues);
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
                listsAndQueues.addOperacaoES(p);
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
        List<PCB> pronto = listsAndQueues.getPronto();
        for (PCB processo : pronto) {
                id_ProcessosProntos.add(processo.idProcesso);
        }
//        Collections.sort(id_ProcessosProntos);
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
        Collections.sort(id_ProcessosEsperando);
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
        Collections.sort(id_ProcessosTerminados);
        return id_ProcessosTerminados;
    }

    @Override
    protected int tempoEsperaMedio() {
        int x = 0;
        for(PCB proc : listsAndQueues.getTarefas()) {
            x += proc.espera;
        }
        if(listsAndQueues.getTarefas().size() == 0) return 0;
        int size = listsAndQueues.getTarefas().size();

        return x/size;
    }

    @Override
    protected int tempoRespostaMedio() {
        int soma = 0;
        for (PCB p : listsAndQueues.getTarefas()) {
            soma += p.resposta;
        }
        return soma/listsAndQueues.getTarefas().size();
    }

    @Override
    protected int tempoRetornoMedio() {
        int soma = 0;
        for (PCB p : listsAndQueues.getTarefas()) {
            soma += p.retorno;
        }
        return soma/listsAndQueues.getTarefas().size();
    }

    @Override
    protected int trocasContexto() {
        return trocaContexto;
    }

    @Override
    public void defineEscalonador(Escalonador e) {
        listsAndQueues.getProcessosCPU().clear();
        listsAndQueues.getNovos().clear();
        listsAndQueues.getPronto().clear();
        listsAndQueues.getTarefas().clear();
        listsAndQueues.getEsperando().clear();
        listsAndQueues.getTerminados().clear();
        listsAndQueues.getDispositivos().clear();
        trocaContexto = 0;
        esc = e;
    }

}
