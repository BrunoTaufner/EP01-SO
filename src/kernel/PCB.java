package kernel;
import operacoes.Carrega;
import operacoes.Operacao;
import operacoes.OperacaoES;
import operacoes.Soma;

public class PCB {

	/*
	NOVO = processo novo, porém não roda ainda
	PRONTO = próximo ciclo de programa (depois do NOVO) <<< AQUI ESCOLHEMOS O ESCALONADOR
	 */
	public enum Estado {NOVO, PRONTO, EXECUTANDO, ESPERANDO, TERMINADO}


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
	public int tempoBurst = 5;
	public int estimativaBurst = tempoBurst;
	public int ult = 0;
	public boolean fimBurstOperacaoES = false;
	public int contadorCiclos = 0; //Para Round Robin
	public int tamanhoProcesso = 0;
	public boolean opCouS = false;
	public boolean entrou = false;

	public int calculaTamanhoBurst(int chute) {
		int proximoChute;
		if(cicloBurst > 0) ult = contadorBurst;
		if(ult == 0)  proximoChute = (contadorBurst + chute) / 2;
		else proximoChute = (ult + chute) / 2;
		return proximoChute;
	}

	public void bolaDeCristal () {
		tamanhoProcesso = 0;
		entrou = true;
		int i = operacao;
		while(i < codigo.length) {
			Operacao cod = codigo[i];
			if(cod instanceof Carrega || cod instanceof Soma) {
				tamanhoProcesso++;
			}
			else if(cod instanceof OperacaoES){
				break;
			}
			i++;
		}

	}

}