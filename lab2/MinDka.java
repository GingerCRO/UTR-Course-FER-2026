package hr.unizg.fer.utr.lab2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MinDka {

	public static void main(String[] args) {
		
		// citanje sa standardnog ulaza
		Scanner sc = new Scanner(System.in);
		
		// liste za spremanje definicije automata
		List<String> stanja = new ArrayList<String>();
		List<String> simboliAbecede = new ArrayList<String>();
		List<String> prihvatljivaStanja = new ArrayList<String>();
		String pocetnoStanje = null;
		
		for (int i = 1; i <= 4; ++i) {
			if (sc.hasNextLine()) {
				String linija = sc.nextLine();
				
				switch (i) {
				
				// citanje i parsiranje stanja
				case 1:
					String[] tempStanja = linija.split(",");
					for (int j = 0; j < tempStanja.length; ++j) {
						stanja.add(tempStanja[j]);
					}
					break;
				
				// citanje i parsiranje simbola abecede
				case 2:
					String[] tempAbeceda = linija.split(",");
					for (int j = 0; j < tempAbeceda.length; ++j) {
						simboliAbecede.add(tempAbeceda[j]);
					}
					break;
					
				// citanje i parsiranje prihvatljivih stanja
				case 3:
					if (!linija.trim().isEmpty()) {
						String[] tempPrihvatljiva = linija.split(",");
						for (int j = 0; j < tempPrihvatljiva.length; ++j) {
							prihvatljivaStanja.add(tempPrihvatljiva[j]);
						}
					}
					break;
					
				// citanje i parsiranje pocetnog stanja
				case 4:
					pocetnoStanje = linija.trim();
					break;
				
				}
			}
		}
		
		// citanje i parsiranje funkcija prijelaza
		Map<String, Map<String, String>> mapaStanja = new TreeMap<String, Map<String,String>>();		// stanje, simbol -> novo stanje
		
		// stavi sva stanja u mapu
		for (String str : stanja) {
			mapaStanja.put(str, new TreeMap<String, String>());
		}
		
		// citanje i parsiranje funkcija prijelaza (DKA ima samo jedan prijelaz za jedno trenutno stanje za jedan ulazni simbol)
		while (sc.hasNextLine()) {
			String linija = sc.nextLine();
			
			String[] dijelovi = linija.split("->");
			String[] lijeviDijelovi = dijelovi[0].split(",");
			String staroStanje = lijeviDijelovi[0];
			String simbol = lijeviDijelovi[1];
			String novoStanje = dijelovi[1];
			
			mapaStanja.get(staroStanje).put(simbol, novoStanje);			// dodaj u mapu: staro stanje, simbol -> novo stanje
		}

		sc.close();
		
		// pronadi nedohvatljiva stanja
		Set<String> dohvatljivaStanja = new HashSet<String>();
		Queue<String> redStanja = new LinkedList<String>();
		
		dohvatljivaStanja.add(pocetnoStanje);
		redStanja.add(pocetnoStanje);
		
		while (!redStanja.isEmpty()) {
			String stanje = redStanja.poll();
			for (String simbol : simboliAbecede) {
				String sljedeceStanje = mapaStanja.get(stanje).get(simbol);
				if (!dohvatljivaStanja.contains(sljedeceStanje)) {
					dohvatljivaStanja.add(sljedeceStanje);
					redStanja.add(sljedeceStanje);
				}
			}
		}
		
		// brisanje nedohvatljivih stanja
		mapaStanja.keySet().retainAll(dohvatljivaStanja);		// u mapi stanja ostavi samo stanja koja postoje u dohvatljivaStanja, ostala obrisi
		stanja.retainAll(dohvatljivaStanja);					// u listi stanja ostavi samo stanja koja postoje u dohvatljivaStanja, ostala obrisi
		prihvatljivaStanja.retainAll(dohvatljivaStanja);		// u listi prihvatljivaStanja ostavi samo stanja koja postoje u dohvatljivaStanja, ostala obrisi 
	
		// minimizacija automata, odnosno uklanjanje istovjetnih stanja
		// realizirao sam Algoritam 2 iz udzbenika, 24. stranica (dijeljenje u podskupove)
		
		// stanja dijelimo u dvije grupe: u jednoj grupi su sva prihvatljiva, a u drugoj su sva neprihvatljiva stanja
		Map<String, Integer> grupa = new TreeMap<String, Integer>();
		for (String stanje : mapaStanja.keySet()) {
			if (prihvatljivaStanja.contains(stanje)) {
				grupa.put(stanje, 1);			// prihvatljiva stanja
			} else {
				grupa.put(stanje, 0);			// neprihvatljiva stanja
			}
		}
		
		// podijeli grupu na podskupove
		// ako na kraju nije bilo promjene, gotovi smo
		boolean promjena = true;
		while (promjena) {
			promjena = false;
			
			// kako se ponasa stanje s obzirom na sve njegove ulazne simbole
			Map<List<Integer>, Integer> ponasanjeStanja = new HashMap<List<Integer>, Integer>();
			Map<String, Integer> novaGrupa = new TreeMap<String, Integer>();
			int sljedecaGrupa = 0;
			
			for (String stanje : mapaStanja.keySet()) {
				// ponasanje: trenutnaGrupa, grupa(stanje,simbol1), grupa(stanje,simbol2), ...
				List<Integer> ponasanje = new ArrayList<Integer>();
				ponasanje.add(grupa.get(stanje));
				for (String simbol : simboliAbecede) {
					String iduceStanje = mapaStanja.get(stanje).get(simbol);
					ponasanje.add(grupa.get(iduceStanje));
				}
				
				// oznaka grupe za ovo ponasanje
				if (!ponasanjeStanja.containsKey(ponasanje)) {
					ponasanjeStanja.put(ponasanje, sljedecaGrupa++);
				}
				novaGrupa.put(stanje, ponasanjeStanja.get(ponasanje));				
			}
			
			// ako se grupa promijenila, nastavi s podjelom u podgrupe
			if (!novaGrupa.equals(grupa)) {
				promjena = true;
				grupa = novaGrupa;
			}
		}
		
		// iz svake grupe spremi leksikografski najmanje stanje
		Map<Integer, String> najmanji = new TreeMap<Integer, String>();
		for (String stanje : mapaStanja.keySet()) {
			int grupaBroj = grupa.get(stanje);
			if (!najmanji.containsKey(grupaBroj) || stanje.compareTo(najmanji.get(grupaBroj)) < 0) {
				najmanji.put(grupaBroj, stanje);
			}
		}
		
		// svako stanje spoji s njemu najmanjim sljedecim stanjem
		Map<String, String> spojiStanja = new TreeMap<String, String>();
		for (String stanje : mapaStanja.keySet()) {
			spojiStanja.put(stanje, najmanji.get(grupa.get(stanje)));
		}
		
		// nakon sto smo minimizirali automat, moramo jos definirati minimalan DKA
		
		// nova stanja
		Set<String> novaStanja = new TreeSet<String>(spojiStanja.values());
		
		// nova prihvatljiva stanja
		Set<String> novaPrihvatljivaStanja = new TreeSet<String>();
		for (String stanje : prihvatljivaStanja) {
			novaPrihvatljivaStanja.add(spojiStanja.get(stanje));
		}
		
		// novo pocetno stanje
		String novoPocetnoStanje = spojiStanja.get(pocetnoStanje);
		
		// nove funkcije prijelaza
		Map<String, Map<String, String>> novaMapaStanja = new TreeMap<String, Map<String,String>>();
		for (String stanje : novaStanja) {
			novaMapaStanja.put(stanje, new TreeMap<String, String>());
		}
		for (String stanje : novaStanja) {
			for (String simbol : simboliAbecede) {
				String iduceStanje = mapaStanja.get(stanje).get(simbol);
				novaMapaStanja.get(stanje).put(simbol, spojiStanja.get(iduceStanje));
			}
		}
		
		// ISPIS
		// na stdout
		
		StringBuilder sb = new StringBuilder();
		
		// stanja
		sb.append(String.join(",", novaStanja)).append("\n");
		
		// simboli abecede
		sb.append(String.join(",", simboliAbecede)).append("\n");
		
		// prihvatljiva stanja
		sb.append(String.join(",", novaPrihvatljivaStanja)).append("\n");
		
		// pocetno stanje
		sb.append(novoPocetnoStanje).append("\n");
		
		// funkcije prijelaza
		for (String stanje : novaStanja) {
			for (String simbol : simboliAbecede) {
				String iduceStanje = novaMapaStanja.get(stanje).get(simbol);
				sb.append(stanje).append(",").append(simbol).append("->").append(iduceStanje).append("\n");
			}
		}
		
		// ispis definicije DKA
		System.out.print(sb.toString());
		
	}

}
