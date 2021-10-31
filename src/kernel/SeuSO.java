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

    Escalonador esc;

    public static List<PCB> processos = new LinkedList<>();

    public static Listas listsAndQueues = new Listas();
    public static boolean CPUexecuting = false;
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
        System.out.println();
        for(PCB p : listsAndQueues.getProcessosCPU()) {
            System.out.print(p.idProcesso + " ");
        }
        System.out.println("<-");
        listsAndQueues.getProcessosCPU().sort(new SortBurst());
        pcbAtual.estado = PCB.Estado.EXECUTANDO;
        listsAndQueues.delListaPronto(pcbAtual);
        pcbProximo.estado = PCB.Estado.PRONTO;
        listsAndQueues.addFilaPronto(pcbProximo);
        listsAndQueues.getPronto().sort(new SortIdProcesso());
        trocaContexto += 1;
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
            if(opES.op.ciclos == 1) {
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

        for (PCB p : processos){
            if (p.estado.equals(PCB.Estado.EXECUTANDO)) {
                if(p.codigo[p.operacao] instanceof Carrega || p.codigo[p.operacao] instanceof Soma) {
                    if (!p.ESexecuting) {
                        op = p.codigo[p.operacao];
                        p.operacao++;
                        p.contadorDePrograma++;
                        p.estimativaBurst--;
                        if(p.cicloBurst == 0) p.contadorBurst++;
                        // SAI DA CPU
                        if(p.operacao >= p.codigo.length || p.codigo[p.operacao] instanceof OperacaoES) {
                            CPUexecuting = false;
                            p.cicloBurst++;
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
                //System.out.print(estado+": ");
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
                        if (!processos.isEmpty()) i = -1;

                    } else {
                        // PROCESSO NOVO
                        if (p.estado.equals(PCB.Estado.NOVO) && p.tempoProcesso > 0) {
                            p.estado = PCB.Estado.PRONTO;
                            listsAndQueues.addFilaPronto(p);
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
                                // TEMPO BURST PARA SJF
                                if (esc.equals(Escalonador.SHORTEST_JOB_FIRST) || esc.equals(Escalonador.SHORTEST_REMANING_TIME_FIRST) ) {
                                    if(p.cicloBurst > 0) {
                                        p.tempoBurst  = p.calculaTamanhoBurst(p.tempoBurst);
                                        p.estimativaBurst = p.tempoBurst;
                                        p.cicloBurst = 0;
                                        p.contadorBurst = 0;
                                    }
                                    listsAndQueues.getPronto().sort(new SortBurst());
                                }
                            }
                            // ESPERANDO PARA PRONTO
                            else if (!(p.codigo[p.operacao] instanceof OperacaoES)) {
                                listsAndQueues.delListaEsperando(p);
                                p.estado = PCB.Estado.PRONTO;
                                listsAndQueues.addFilaPronto(p);
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
        carregaRegistradoresVirtuais(getProcessador()); // vai para troca de contexto
        if(processos.isEmpty()) return;
        switch (esc) {
            case SHORTEST_JOB_FIRST:
                listas.Escalonadores.SJF(processos, listsAndQueues);
                break;
            case SHORTEST_REMANING_TIME_FIRST:
                listas.Escalonadores.SRTF(processos, listsAndQueues);
                break;
            case ROUND_ROBIN_QUANTUM_5:
                break;
        }
        /*
        for(PCB p : listsAndQueues.getProcessosCPU()){
            System.out.print(p.idProcesso+" ");
        }
        System.out.println();

         */
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
        return x/listsAndQueues.getTarefas().size();
    }

    @Override
    protected int tempoRespostaMedio() {
        int soma = 0;
        for (PCB p : listsAndQueues.getTarefas()) {
            //System.out.println("id: "+ p.idProcesso +"\t resposta: "+ p.resposta);
            soma += p.resposta;
        }
        return soma/listsAndQueues.getTarefas().size();
    }

    @Override
    protected int tempoRetornoMedio() {
        int soma = 0;
        for (PCB p : listsAndQueues.getTarefas()) {
            //System.out.println("id: "+ p.idProcesso +"\t retorno: "+ p.retorno);
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
        this.esc = e;
    }

}
