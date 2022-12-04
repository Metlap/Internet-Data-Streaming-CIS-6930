import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class DLefthash {

	int[] hashTable;
	int[] s;
	int numberOfFlows;
	int numberOfEntries;
	int segmentSize;
	int[] flowArray;

	// Constructor for initializing the values for the DLeftHashTable
	DLefthash(int numberOfEntries, int numberofFlows, int numberOfHashSegments) {
		this.numberOfEntries = numberOfEntries;
		this.numberOfFlows = numberofFlows;
		hashTable = new int[numberOfEntries];
		s = new int[numberOfHashSegments];
		this.segmentSize = this.numberOfEntries / numberOfHashSegments;
		// filling s[i] with random values
		Set<Integer> uniqueHashVal = new HashSet<>();
		for (int i = 0; i < s.length; i++) {
			while (true) {
				// Generate a random positive number
				int newHashGen = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
				if (!uniqueHashVal.contains(newHashGen)) {
					s[i] = newHashGen;
					break;
				}
			}
		}

		// Generate Random FlowIDs and store in an array
		this.flowArray = new int[this.numberOfFlows];

		for (int i = 0; i < numberOfFlows; i++) {

			this.flowArray[i] = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
		}
	}

	// Fill in the Hash Table with Flow ID's
	public int fillHashTable() {
		int totalCount = 0;
		for (int i = 0; i < numberOfFlows; i++) {
			int flowID = 0;
			// Generate Unique HashID's
			flowID = this.flowArray[i];

			int[] resultHash = new int[s.length];
			for (int j = 0; j < resultHash.length; j++) {
				resultHash[j] = flowID ^ s[j];
			}

			// Hash each flow to each segment
			for (int j = 0; j < resultHash.length; j++) {
				int index = (resultHash[j] % this.segmentSize) + (j * this.segmentSize);
				if (hashTable[index] == 0) {
					hashTable[index] = flowID;
					totalCount++;
					break;
				}
			}
		}
		return totalCount;
	}

	public static void main(String args[]) throws IOException {
		DLefthash dleftHashTable = null;

		try {
			if (args.length != 3) {
				throw new IllegalArgumentException("Enter valid number(3) of arguments");
			} else {
				dleftHashTable = new DLefthash(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
						Integer.parseInt(args[2]));
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Please provide a valid Input");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		File fout = new File("NewOutputDLeftHashTable.txt");
		FileWriter fileWriter = new FileWriter(fout);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		int totalCount = dleftHashTable.fillHashTable();
		printWriter.println(Integer.toString(totalCount));

		for (int i = 0; i < dleftHashTable.hashTable.length; i++) {
			printWriter.println(Integer.toString(dleftHashTable.hashTable[i]));
		}
		printWriter.close();
	}
}
