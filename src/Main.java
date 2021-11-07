import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import kernel.SO;
import kernel.SO.Escalonador;
import kernel.SeuSO;

public class Main {
	public static void main(String[] args) throws FileNotFoundException {
		List<Escalonador> escalonadores = new LinkedList<>();	
		escalonadores.add(SO.Escalonador.FIRST_COME_FIRST_SERVED);
		escalonadores.add(SO.Escalonador.SHORTEST_JOB_FIRST);
		escalonadores.add(SO.Escalonador.SHORTEST_REMANING_TIME_FIRST);
		escalonadores.add(SO.Escalonador.ROUND_ROBIN_QUANTUM_5);
		escalonadores.add(SO.Escalonador.SHORTEST_REMANING_TIME_FIRST_BOLA_DE_CRISTAL);
		String arq [] = {
				"entradas/teste1.txt",
				"entradas/teste2.txt",
				"entradas/teste3.txt",
				"entradas/teste4.txt",
				"entradas/teste5.txt",
				"entradas/teste6.txt",
				"entradas/teste7.txt",
				"entradas/teste8.txt",
				"entradas/teste9.txt",
				"entradas/teste10.txt",
				"entradas/teste12.txt",
				"entradas/teste13.txt",
				"entradas/teste14.txt",
				"entradas/teste15.txt",
				"entradas/teste16.txt",
				"entradas/teste17.txt",
				"entradas/teste18.txt",
				"entradas/teste19.txt",
				"entradas/teste20.txt",
				"entradas/teste21.txt",
				"entradas/teste22.txt",
				"entradas/teste23.txt",
				"entradas/teste24.txt",
		};
		for(String arqv : arq) {
			for(Escalonador esc : escalonadores) {
				SO so = new SeuSO();
				so.defineEscalonador(esc);
				so.leArquivoEntrada(arqv);
				so.simula();
			}
		}
	}

}
