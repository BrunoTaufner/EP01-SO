package kernel;
import operacoes.Operacao;

public class PCB implements Comparable<PCB> {

	@Override
	public int compareTo(PCB outro) {
		if (this.idProcesso > outro.idProcesso) return 1;
		else return -1;
	}

	public enum Estado {NOVO, PRONTO, EXECUTANDO, ESPERANDO, TERMINADO};
	public int idProcesso; // primeiro processo criado deve ter id = 0
	public Estado estado = Estado.NOVO;
	public int[] registradores = new int[5];
	public int contadorDePrograma;
	public Operacao[] codigo;
	public int proxChuteTamBurstCPU;
	public int contadorBurst;
	public int contadorCiclos; //Para Round Robin

}