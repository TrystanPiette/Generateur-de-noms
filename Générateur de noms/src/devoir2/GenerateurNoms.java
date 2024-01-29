package devoir2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class GenerateurNoms {

	private static int MATRIX_SIZE;
	private static String nomFichier = "Noms";
	private static List<String> listeNoms = getListeNoms(nomFichier);

	public static void main(String[] args) throws IOException {

		// ----------------------------
		// Menu générateur de noms
		// ----------------------------
		int choix = -1;
		System.out.println("********************************************");
		System.out.println("*Bienvenue au générateur aléatoire de Noms!*");
		System.out.println("********************************************");

		do {
			System.out.println("\n----------------Menu----------------");
			System.out.println("1: Générer un ou plusieurs nom saléatoires");
			System.out.println("2: Ajouter un nom aléatoire à la liste de noms à partir de cette liste");
			System.out.println("3: Ajouter un ou plusieurs noms manuellement");
			System.out.println("4: Réinitialiser le générateur à zéro");
			System.out.println("5: Quitter l'application");
			System.out.println("------------------------------------");
			System.out.println("Votre choix:");

			// Création de la matrice de Markov
			MATRIX_SIZE = countLetters(listeNoms);
			double[][] matriceTransition = new double[MATRIX_SIZE][MATRIX_SIZE];
			Map<Character, Integer> charToIndex = new HashMap<>();
			charToIndex.put('\0', 0); // Char final est à index 0
			charToIndex.put('\1', 1); // Char initial est à index 1

			populateCharToIndexAndInitialTransitions(charToIndex, matriceTransition, listeNoms);
			populatematriceTransition(matriceTransition, charToIndex, listeNoms);
			normaliserTransitions(matriceTransition);

			try {
				choix = getInt();
				switch (choix) {
				case 1: // Générer un nom aléatoire
					if (listeNoms.size() > 0) {
						int choixNbNoms = 0;
						try {
							System.out.println("Combien de noms voulez-vous générer?");
							choixNbNoms = getInt();
						} catch (NumberFormatException e) { // Si le input est un String, il recommence
							System.out.println("\nInput invalide! Veuillez entrer un entier.");
						}
						System.out.println("Voici vos noms aléatoires générés:\n");
						for (int i = 0; i < choixNbNoms; i++) {
							String nomRandom = genererNomRandom(matriceTransition, charToIndex);
							System.out.println(nomRandom);
						}

					} else {
						System.out.println("La liste est vide, rajoutez un nom manuellement pour continuer!");
					}
					break;

				case 2: // Ajouter un nom aléatoire
					if (listeNoms.size() > 0) {
						String nomRandom = genererNomRandom(matriceTransition, charToIndex);
						System.out.println("Voici le nom aléatoire ajouté à la liste de noms:");
						listeNoms.add(nomRandom);
						System.out.println(nomRandom);
					} else {
						System.out.println("La liste est vide, rajoutez un nom manuellement pour continuer!");
					}
					break;

				case 3: // Réinitialiser le générateur à zéro et ajouter des noms manuellement
					int choixNbNoms = 0;
					Scanner scanner = new Scanner(System.in);
					StringBuilder nomAjoutes = new StringBuilder();
					try {
						System.out.println("Combien de noms voulez-vous rajouter?");
						choixNbNoms = getInt();
					} catch (NumberFormatException e) { // Si le input est un String, il recommence
						System.out.println("\nInput invalide! Veuillez entrer un entier.");
					}

					for (int i = 0; i < choixNbNoms; i++) {
						System.out.print("Nom #" + (i + 1) + ": ");
						String nom = scanner.nextLine();

						while (nom.matches(".*\\d.*")) {
							System.out.print("Le nom ne doit pas contenir de chiffres. Réessayez : ");
							nom = scanner.nextLine();
						}
						nom = nom.toLowerCase().replace(" ", ""); // Enlever les espaces puis les lettres majuscules
						listeNoms.add(nom);
						nomAjoutes.append("\n").append(nom);
					}

					System.out.println("\nLes noms entrés et rajoutés sont :" + nomAjoutes);
					break;

				case 4: // Réinitialiser le générateur à zéro
					listeNoms.clear();
					System.out.println("Le tout a été réinitialisé!");
					break;

				case 5:
					System.out.println("Merci d'avoir participé!");
					System.out.println("Terminaison du programme...");
					return; // Exit the program

				default:
					System.out.println("\nChoix invalide! Veuillez recommencer.");
					break;
				}
			} catch (NumberFormatException e) { // Si le input est un String, il recommence
				System.out.println("\nInput invalide! Veuillez entrer un entier.");
			}
		} while (choix != 5);
	}

	public static String getString() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		return br.readLine();
	}

	public static int getInt() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = br.readLine();
		return Integer.parseInt(input);
	}

	// ----------------------------
	// MÉTHODE populateCharToIndexAndInitialTransitions: Rempli le mappage
	// caractère-index et les transitions initiales de la matrice
	// ----------------------------
	private static void populateCharToIndexAndInitialTransitions(Map<Character, Integer> charToIndex,
			double[][] matriceTransition, List<String> nameList) {
		int index = 2; // Start character index
		for (String name : nameList) {
			charToIndex.putIfAbsent(name.charAt(0), index++);
			for (char c : name.toCharArray()) {
				charToIndex.putIfAbsent(c, index++);
			}
			matriceTransition[1][charToIndex.get(name.charAt(0))]++;
		}
	}

	// ----------------------------
	// MÉTHODE populatematriceTransition: Calcul les transitions et rempli la matrix
	// en fonction de celui-ci
	// ----------------------------
	private static void populatematriceTransition(double[][] matriceTransition, Map<Character, Integer> charToIndex,
			List<String> nameList) {
		for (String name : nameList) {
			Integer previousChar = 1; // Start character
			for (char c : name.toCharArray()) {
				int charIndex = charToIndex.get(c);
				if (previousChar != null) {
					matriceTransition[previousChar][charIndex]++;
				}
				previousChar = charIndex;
			}
			matriceTransition[charToIndex.get(name.charAt(name.length() - 1))][0]++; // Transition to end character
		}
	}

	// ----------------------------
	// MÉTHODE normaliserTransitions: Normalise les tansitions afin de calculer les
	// probabilités
	// ----------------------------
	private static void normaliserTransitions(double[][] matriceTransition) {
		for (int i = 0; i < matriceTransition.length; i++) {
			double sum = Arrays.stream(matriceTransition[i]).sum();
			for (int j = 0; j < matriceTransition[i].length; j++) {
				matriceTransition[i][j] /= sum;
			}
		}
	}

	// ----------------------------
	// MÉTHODE genererNomRandom: Génère un nom aléatoire selon le mappage
	// caractère-index et la
	// matrice de noms
	// ----------------------------
	private static String genererNomRandom(double[][] matrix, Map<Character, Integer> charMapping) {
		StringBuilder nom = new StringBuilder();
		Integer lastChar = 1; // Start character

		while (nom.length() < 100) {
			char nextChar;
			double[] probabilities = matrix[lastChar];
			int nextCharIndex = getRandomIndexWithProbabilities(probabilities);
			if (nextCharIndex == 0) { // End character
				break;
			}
			Optional<Character> optionalChar = getCleParvaleur(charMapping, nextCharIndex);
			if (optionalChar.isPresent()) {
				nextChar = optionalChar.get();
			} else {
				break;
			}

			nom.append(nextChar);
			lastChar = charMapping.get(nextChar);
		}
		return nom.toString();
	}

	// ----------------------------
	// MÉTHODE getRandomIndexWithProbabilities: Choisi un index aléatoire selon les
	// probabilitiés fournies
	// ----------------------------
	private static int getRandomIndexWithProbabilities(double[] probabilities) {
		double random = new Random().nextDouble();
		double ProbabiliteCumulative = 0.0;
		for (int i = 0; i < probabilities.length; i++) {
			ProbabiliteCumulative += probabilities[i];
			if (random <= ProbabiliteCumulative) {
				return i;
			}
		}
		return probabilities.length - 1;
	}

	// ----------------------------
	// MÉTHODE countLetters: Boucle for qui calcule la montant de
	// lettres présents dans la liste
	// ----------------------------
	public static int countLetters(List<String> listeNoms) {
		int compteur = 0;
		for (String nom : listeNoms) {
			compteur += nom.replaceAll("[^a-zA-Z]", "").length();
		}
		if (listeNoms.isEmpty()) {
			return 2;
		}
		return compteur + listeNoms.size() + 2; // l'ajout de listeNoms.size() est présent afin de représenter l'espace
												// vide à
												// la fin des noms dans la liste
	}

	// ----------------------------
	// MÉTHODE getCleParvaleur: Facilite la manipulation de clés associées avec une
	// valeur spécifique de map
	// ----------------------------
	private static <K, V> Optional<K> getCleParvaleur(Map<K, V> carte, V valeur) {
		for (Map.Entry<K, V> entre : carte.entrySet()) {
			if (Objects.equals(valeur, entre.getValue())) {
				return Optional.of(entre.getKey());
			}
		}
		return Optional.empty();
	}

	// ----------------------------
	// MÉTHODE getListeNoms: Permet de lire le document 'Noms' puis transforme son
	// contenu dans une liste de Strings
	// ----------------------------
	public static List<String> getListeNoms(String nomFichier) {
		List<String> listeNoms = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(nomFichier))) {
			String ligne;
			while ((ligne = br.readLine()) != null) {
				listeNoms.add(ligne);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return listeNoms;
	}

}
