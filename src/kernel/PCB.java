package kernel;
import operacoes.Operacao;
import operacoes.OperacaoES;

import java.util.LinkedList;
import java.util.Queue;

public class PCB {

	/*
	NOVO = processo novo, porém não roda ainda
	PRONTO = próximo ciclo de programa (depois do NOVO) <<< AQUI ESCOLHEMOS O ESCALONADOR
	 */



	public enum Estado {NOVO, PRONTO, EXECUTANDO, ESPERANDO, TERMINADO}
	public int idProcesso; // primeiro processo criado deve ter id = 0
	public Estado estado = Estado.NOVO;
	public int[] registradores = new int[5];
	public int contadorDePrograma = 0;
	public Operacao[] codigo;
	public int operacao = 0;
	public boolean ESexecuting = false;

	public int proxChuteTamBurstCPU; //GUSTAVO COLOCOU ESSE TROÇO AQUI, PARA O ESCALONADOR LÁ, ESQUECI O NOME
	public int contadorBurst = 0;
	public int instanteChegada = 0;
	public int tempoProcesso = 0;
	public int contadorCiclos = 0; //Para Round Robin
	public int remainingTime; // Shortest Job First
	public int espera = 0; // tempo de espera do processo

	public int retorno = 0; // tempo para terminar o processo
	public int resposta = 0; // tempo para espera até executar a primeira execução
	// SHORTEST JOB FIRST ORDENAR LISTA DE PROCESSOS E PEGAR O PRIMEIRO PROCESSO

	public void calculaCicloBurst() {
		boolean achouES = false;
		for(Operacao op : codigo) {
			if(op instanceof OperacaoES) {
				contadorBurst += ((OperacaoES) op).ciclos;
			}
			else if(!(op instanceof OperacaoES)){
				contadorBurst++;
			}
		}
	}

}