import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Multihash {
	int[] hashTable;
	int[] s;
	int numberOfFlows;
	int numberOfEntries;
	int[] flowArray;
	public static int totalCount = 0;

	// Parametrized Constructor for initializing the values for the MultiHashTable
	Multihash(int numberOfEntries, int numberOfFlows, int numOfHashes) {
		this.numberOfEntries = numberOfEntries;
		this.numberOfFlows = numberOfFlows;
		hashTable = new int[numberOfEntries];
		s = new int[numOfHashes];
		generateHash(s);
		
		// Generate Random FlowIDs and store in an array
		this.flowArray = new int[this.numberOfFlows];

		for (int i = 0; i < numberOfFlows; i++) {

			this.flowArray[i] = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
		}
	}

	// Initializing s with random numbers
	private void generateHash(int[] s) {

		// Using a set to make sure the generated each of the hash functions are unique
		Set<Integer> uniqueHash = new HashSet<>();
		for (int i = 0; i < s.length; i++) {
			while (true) {
				int newHash = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
				if (!uniqueHash.contains(newHash)) {
					uniqueHash.add(newHash);
					s[i] = newHash;
					break;
				}
			}
		}
	}

	// Fill in the Hash Table with Flow ID's
	public void fillHashTable() {

		// Set<Integer> uniqueHashTable = new HashSet<>();
		for (int i = 0; i < numberOfFlows; i++) {

			int flowID = this.flowArray[i];

			int[] resultHash = new int[s.length];
			for (int j = 0; j < resultHash.length; j++) {
				resultHash[j] = flowID ^ s[j];
			}

			for (int j = 0; j < resultHash.length; j++) {
				// If no flow id is present in the entry , insert this flow ID
				if (hashTable[resultHash[j] % numberOfEntries] == 0) {
					// After one insert break
					hashTable[resultHash[j] % numberOfEntries] = flowID;
					totalCount++;
					break;
				}
			}
		}
	}

	public static void main(String[] arg) throws IOException {
		Multihash multiHashTable = null;
		try {
			if (arg.length != 3) {
				throw new IllegalArgumentException("Enter valid number(3) of arguments");
			} else {
				multiHashTable = new Multihash(Integer.parseInt(arg[0]), Integer.parseInt(arg[1]),
						Integer.parseInt(arg[2]));
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Please provide a valid Input");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		File fout = new File("NewOutputMultiHashTable.txt");
		FileWriter fileWriter = new FileWriter(fout);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		multiHashTable.fillHashTable();
		printWriter.println(Integer.toString(totalCount));

		for (int i = 0; i < multiHashTable.hashTable.length; i++) {
			printWriter.println(Integer.toString(multiHashTable.hashTable[i]));
		}
		printWriter.close();
	}
}


// Java program for the above approach
