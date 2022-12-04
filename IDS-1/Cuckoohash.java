import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Cuckoohash {

	int[] hashTable;
	int[] s;
	int numberOfFlows;
	int numberOfEntries;
	int numberOfCuckooSteps;
	int[] flowArray;
	Map<Integer, int[]> flowIDHashedMap = new HashMap<>();

	// Constructor for initializing the values for the CuckooHashTable
	Cuckoohash(int numOfEntries, int numberOfFlows, int numberOfHashes, int numberOfCuckooSteps) {
		this.numberOfEntries = numOfEntries;
		this.numberOfFlows = numberOfFlows;
		hashTable = new int[numOfEntries];
		this.numberOfCuckooSteps = numberOfCuckooSteps;
		s = new int[numberOfHashes];
		generateHash(s);
		
		// Generate Random FlowIDs and store in an array
		this.flowArray = new int[this.numberOfFlows];

		for (int i = 0; i < numberOfFlows; i++) {

			this.flowArray[i] = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
		}
	}

	// Generate all the Unique Hash values in the s array for k hashes
	private void generateHash(int[] s) {
		Set<Integer> uniqueHash = new HashSet<>();
		for (int i = 0; i < s.length; i++) {
			while (true) {
				int newHash = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
				if (!uniqueHash.contains(newHash)) {
					s[i] = newHash;
					break;
				}
			}
		}
	}

	// Fill in the Hash Table with Flow IDs
	public int fillHashTable() {
		int totalCount = 0;

		for (int i = 0; i < numberOfFlows; i++) {
			int flowID = this.flowArray[i];
			boolean foundEmptySlot = false;
			// Generate Unique HashID's

			// k hashes for each of the flow -> Hi[fi] = s[i] ^ fi
			int resultHash[] = new int[s.length];
			for (int j = 0; j < resultHash.length; j++) {
				resultHash[j] = flowID ^ s[j];
			}
			flowIDHashedMap.put(flowID, resultHash);
			for (int j = 0; j < resultHash.length; j++) {
				if (hashTable[resultHash[j] % numberOfEntries] == 0) {
					hashTable[resultHash[j] % numberOfEntries] = flowID;
					foundEmptySlot = true;
					totalCount++;
					break;
				}
			}
			if (!foundEmptySlot) {
				for (int j = 0; j < resultHash.length; j++) {
					int numOfCuckooSteps = numberOfCuckooSteps;
					if (move(resultHash[j], numOfCuckooSteps)) {
						hashTable[resultHash[j] % numberOfEntries] = flowID;
						totalCount++;
						break;
					}
				}
			}
		}
		return totalCount;
	}

	// this function moves recursively back the cuckoo steps
	private boolean move(int newHashValue, int numOfCuckooSteps) {

		if (numOfCuckooSteps == 0) {
			return false;
		} else {
			int newFlowID = hashTable[newHashValue % numberOfEntries];
			int resultHash[] = flowIDHashedMap.get(newFlowID);
			for (int i = 0; i < resultHash.length; i++) {
				if (resultHash[i] != newHashValue && hashTable[resultHash[i] % numberOfEntries] == 0) {
					// If another slot of the flow f' is empty, move f' to there
					hashTable[resultHash[i] % numberOfEntries] = newFlowID;
					return true;
				}
			}
			// If no empty slot found in first cuckoo step, try in next cuckoo steps
			for (int i = 0; i < resultHash.length; i++) {
				if (resultHash[i] != newHashValue && move(resultHash[i], numOfCuckooSteps - 1)) {
					hashTable[resultHash[i] % numberOfEntries] = newFlowID;
					return true;
				}
			}
		}
		return false;
	}

	public static void main(String[] args) throws IOException {
		Cuckoohash cuckooHashTable = null;
		try {
			if (args.length != 4) {
				throw new IllegalArgumentException("Enter valid number(4) of arguments");
			} else {
				cuckooHashTable = new Cuckoohash(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
						Integer.parseInt(args[2]), Integer.parseInt(args[3]));
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Please provide a valid Input");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		File fout = new File("NewOutputCuckooHashTable.txt");
		FileWriter fileWriter = new FileWriter(fout);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.println(Integer.toString(cuckooHashTable.fillHashTable()));

		for (int i = 0; i < cuckooHashTable.hashTable.length; i++) {
			printWriter.println(Integer.toString(cuckooHashTable.hashTable[i]));
		}
		printWriter.close();
	}

}
