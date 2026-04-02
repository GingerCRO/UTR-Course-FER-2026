package hr.unizg.fer.utr.lab1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

class Stanje {
	
	String naziv;								// naziv trenutnog stanja
	Map<String, Set<String>> prijelazi;			// ulaz -> sljedeca stanja
	
	// konstruktor za stvaranje novog stanja
	public Stanje(String naziv) {
		this.naziv = naziv;
		this.prijelazi = new HashMap<String, Set<String>>();
	}
	
	// dodavanje novog prijelaza
	public void dodajPrijelaz(String simbol, String novoStanje) {
		if (prijelazi.containsKey(simbol)) {
			prijelazi.get(simbol).add(novoStanje);
		} else {
			Set<String> novaStanja = new TreeSet<String>();				// koristimo TreeSet zbog leksikografskog poretka stanja
			novaStanja.add(novoStanje);
			prijelazi.put(simbol, novaStanja);
		}
	}
	
	// dohvati sljedeca stanja za odredeni ulazni znak
	public Set<String> iducaStanja(String simbol) {
		return prijelazi.getOrDefault(simbol, new TreeSet<String>());
	}
	
}

public class SimEnka {

	public static void main(String[] args) {
		
		// citanje sa standardnog ulaza
		Scanner sc = new Scanner(System.in);
		
		// liste za spremanje definicije automata
		List<List<String>> ulazniNizovi = new ArrayList<List<String>>();
		List<String> stanja = new ArrayList<String>();
		List<String> simboliAbecede = new ArrayList<String>();
		List<String> prihvatjlivaStanja = new ArrayList<String>();
		String pocetnoStanje = null;
		
		for (int i = 1; i <= 5; ++i) {
			if (sc.hasNextLine()) {
				String linija = sc.nextLine();
				
				switch (i) {
				
				// citanje i parsiranje ulaznih nizova
				case 1:
					String[] temp = linija.split("\\|");
					for (int j = 0; j < temp.length; ++j) {
						String[] tempComma = temp[j].split(",");
						List<String> tempList = new ArrayList<String>();
						for (int k = 0; k < tempComma.length; ++k) {
							tempList.add(tempComma[k]);
						}
						ulazniNizovi.add(tempList);
					}
					break;
				
				// citanje i parsiranje stanja
				case 2:
					String[] tempStanja = linija.split(",");
					for (int j = 0; j < tempStanja.length; ++j) {
						stanja.add(tempStanja[j]);
					}
					break;
				
				// citanje i parsiranje simbola abecede
				case 3:
					String[] tempSimboli = linija.split(",");
					for (int j = 0; j < tempSimboli.length; ++j) {
						simboliAbecede.add(tempSimboli[j]);
					}
					break;
				
				// citanje i parsiranje prihvatljivih stanja
				case 4:
					String[] tempPrihvatljivaStanja = linija.split(",");
					for (int j = 0; j < tempPrihvatljivaStanja.length; ++j) {
						prihvatjlivaStanja.add(tempPrihvatljivaStanja[j]);
					}
					break;
					
				// citanje i spremanje pocetnog stanja
				case 5:
					pocetnoStanje = linija;
					break;
				
				}
				
			}
		}
		
		// citanje i parsiranje funkcija prijelaza stanja automata
		Map<String, Stanje> mapaStanja = new HashMap<String, Stanje>();
		while (sc.hasNextLine()) {
			
			String linija = sc.nextLine().trim();
			if (linija.isEmpty()) {
				continue;
			}
			
			String[] parts = linija.split("->");
			String lijevo = parts[0];					// staroStanje,simbol
			String desno = parts[1];					// novoStanje
			
			// lijevi dio
			String[] leftParts = lijevo.split(",");
			String staroStanje = leftParts[0];
			String simbol = leftParts[1];
			
			// desni dio
			String[] novaStanja = desno.split(",");
			
			if (!mapaStanja.containsKey(staroStanje)) {
				mapaStanja.put(staroStanje, new Stanje(staroStanje));
			}
			
			Stanje s = mapaStanja.get(staroStanje);
			
			// prijelazi iz starog u novo stanje
			for (String str : novaStanja) {
				if (!str.equals("#")) {
					s.dodajPrijelaz(simbol, str);
				}
			}
			
		}

		sc.close();
		
		// preko funkcija prijelaza, prateci inpute, mijenjamo i ispisujemo stanja u kojima se automat nalazi
		for (List<String> ulazniNiz : ulazniNizovi) {
		    
		    StringBuilder sb = new StringBuilder();								// rezultantni string koji se na kraju iteracije ispisuje
		    Set<String> trenutnaStanja = new TreeSet<>();						// Set u kojeg zapisujemo stanja u koja smo dosli
		    trenutnaStanja.add(pocetnoStanje);									// dodaj pocetno stanje u Set
		    
		    trenutnaStanja = epsilonOkolina(trenutnaStanja, mapaStanja);		// provjeri epsilon okolinu pocetnog stanja
		    
		    sb.append(String.join(",", trenutnaStanja));						// dodaj pocetno stanje (i njegova epsilon stanja) u string za ispis
		    
		    // iteriramo po ulaznim simbolima
		    for (String simbol : ulazniNiz) {
		        sb.append("|");
		        
		        Set<String> novaStanja = new TreeSet<>();						// Set u kojeg dodajemo nova stanja za svaki novi simbol
		        for (String stanje : trenutnaStanja) {
		            Stanje st = mapaStanja.get(stanje);							// dohvati nova stanja u koja prelazimo
		            if (st != null) {
		                novaStanja.addAll(st.iducaStanja(simbol));				// nova stanja spremi u Set novih stanja
		            }
		        }
		        
		        novaStanja = epsilonOkolina(novaStanja, mapaStanja);			// za sva nova stanja provjeri njihovu epsilon okolinu
		        
		        if (novaStanja.isEmpty()) {							
		            sb.append("#");												// ako nema novih stanja, stavi znak '#' u string za ispis
		            trenutnaStanja.clear();
		        } else {
		            sb.append(String.join(",", novaStanja));					// ako ima novih stanja, spoji ih
		            trenutnaStanja = novaStanja;								// nova stanja postaju trenutna (priprema za novu iteraciju)
		        }
		    }
		    
		    System.out.println(sb.toString());									// ispisi stanja u koja smo dosli s trenutnim ulaznim simbolom
		}
		
	}
	
	// funkcija koja vraca Set stanja koja se nalaze u epsilon okolini nekog stanja
	static Set<String> epsilonOkolina(Set<String> stanja, Map<String, Stanje> mapaStanja) {
		
		Set<String> rez = new TreeSet<String>(stanja);							// Set u kojeg spremamo stanja koja su pronadena u epsilon okolinama
		Stack<String> stack = new Stack<String>();								// koristimo stack zbog LIFO pristupa
		
		stack.addAll(stanja);													// dodaj sva stanja na stack
		
		while (!stack.isEmpty()) {						
			String trenutno = stack.pop();										// uzmi stanje na stacka
			Stanje stanje = mapaStanja.get(trenutno);							// u koja stanja mozemo prijeci iz trenutnog
			
			if (stanje != null) {
				Set<String> epsilonStanja = stanje.iducaStanja("$");			// pronadi sva epsilon stanja trenutnog stanja
				for (String novo : epsilonStanja) {
					if (!rez.contains(novo)) {									// dodaj nova epsilon stanja u Set i dodaj ih na stack kako bismo i za njih provjerili epsilon okolinu
						rez.add(novo);											
						stack.push(novo);
					}
				}
			}
		}
		
		return rez;																// vrati konacni Set stanja epsilon okoline
		
	}

}
