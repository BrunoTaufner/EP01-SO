package kernel;
import operacoes.Operacao;

public class PCB {

	/*
	NOVO = processo novo, porém não roda ainda
	PRONTO = próximo ciclo de programa (depois do NOVO) <<< AQUI ESCOLHEMOS O ESCALONADOR
	 */


	public enum Estado {NOVO, PRONTO, EXECUTANDO, ESPERANDO, TERMINADO;}
	public int idProcesso; // primeiro processo criado deve ter id = 0
	public Estado estado = Estado.NOVO;
	public Operacao[] codigo;
	public int operacao = 0;
	public boolean ESexecuting = false;
	public int contadorDePrograma = 0;
	public int tempoProcesso = 0;
	public int instanteChegada = 0;
	public int espera = 0; // tempo de espera do processo
	public int resposta = 0; // tempo de resposta
	public int retorno = 0; // tempo de retorno
	public boolean executed = false;
	public int[] registradores = new int[5];
	public int contadorBurst = 0;
	public int cicloBurst = 0;
	public int tempoBurst = 0;

	public int calculaTamanhoBurst(int chute) {
		int proximoChute = (contadorBurst + chute) / 2;
		return proximoChute;
	}
	/*
	public int contadorCiclos = 0; //Para Round Robin
	public int remainingTime; // Shortest Job First

	public int retorno = 0; // tempo para terminar o processo
	public int resposta = 0; // tempo para espera até executar a primeira execução
	*/
	// SHORTEST JOB FIRST ORDENAR LISTA DE PROCESSOS E PEGAR O PRIMEIRO PROCESSO

}