import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class BloomFilter {
	int[] bitMap;
	int numberOfElements;
	int numberOfBits;
	int[] S;

	BloomFilter(int numberOfElements, int numberOfBits, int numberOfHashes) {
		// number of Elements in each set
		// no of bits in the bloom filter
		//  no of hashes for each each element in the set
		this.numberOfElements = numberOfElements;
		this.numberOfBits = numberOfBits;
		bitMap = new int[numberOfBits];
		S = new int[numberOfHashes];
		Set<Integer> uniqueHashVal = new HashSet<>();
		for (int i = 0; i < S.length; i++) {
			while (true) {
				// Generate a random positive number
				int newHashGenerated = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
				if (!uniqueHashVal.contains(newHashGenerated)) {
					S[i] = newHashGenerated;
					uniqueHashVal.add(newHashGenerated);
					break;
				}
			}
		}
	}

	// Driver Function for the program for two sets of Input Set A and Set B
	public int[] fillBloomFilter() {
		int[] result = new int[2];
		Map<Integer, int[]> filterMapA = new HashMap<>();
		Map<Integer, int[]> filterMapB = new HashMap<>();
		// Set<Integer> globalSet = new HashSet<>();
		// Generating random numbers for Set A
		generateRandomElements(filterMapA);
		// After generating , encode them
		encode(filterMapA);
		// Looking up in the filter
		int countA = lookup(filterMapA);
		// Generate random elements for Set B
		generateRandomElements(filterMapB);
		// Lookup in the filter
		int countB = lookup(filterMapB);
		result[0] = countA;
		result[1] = countB;
		return result;
	}

	// Encode all Bits of the seen value to 1
	private void encode(Map<Integer, int[]> filterMap) {
		for (Map.Entry<Integer, int[]> entry : filterMap.entrySet()) {
			int[] resultHash = entry.getValue();
			for (int hash : resultHash) {
				bitMap[hash % numberOfBits] = 1;
			}
		}
	}

	// Check if the value exists in the filter/table
	private int lookup(Map<Integer, int[]> filterMap) {
		int count = 0;
		for (Map.Entry<Integer, int[]> entry : filterMap.entrySet()) {
			int[] resultHash = entry.getValue();
			// if one of the k - hashes in bit map is zero, then the element is not present
			for (int hash : resultHash) {
				if (bitMap[hash % numberOfBits] == 0) {
					count++;
					break;
				}
			}
		}
		return numberOfElements - count;
	}

	// Fill the Sets A and B with random numbers
	private void generateRandomElements(Map<Integer, int[]> filterMap) {

		for (int i = 0; i < numberOfElements; i++) {
			int newRandom = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);

			// Calculating the k hashes for each entry of the Set into the filter
			int[] XorResult = new int[S.length];
			for (int j = 0; j < S.length; j++) {
				XorResult[j] = newRandom ^ S[j];
			}

			filterMap.put(newRandom, XorResult);

		}

	}

//	int element = 0;do
//	{
//		element = random();
//	}while(globalSet.contains(element));
//
//	int[] resultHash = generateHashFunction(element);parentMap.put(element,resultHash);globalSet.add(element);
//	}}

	// Generating random values for A and B
//	private void generateRandomElements(Map<Integer, int[]> parentMap, Set<Integer> globalSet) {
//		for (int i = 0; i < numberOfElements; i++) {
//			int element = 0;
//			do {
//				element = random();
//			} while (globalSet.contains(element));
//
//			int[] resultHash = generateHashFunction(element);
//			parentMap.put(element, resultHash);
//			globalSet.add(element);
//		}
//	}

	// Create the hash function
//	private int[] generateHashFunction(int element) {
//		int[] result = new int[S.length];
//		for (int i = 0; i < S.length; i++) {
//			result[i] = element ^ S[i];
//		}
//		return result;
//	}

	public static void main(String[] args) throws IOException {

		BloomFilter bloomFilter = null;
		try {
			if (args.length != 3) {
				throw new IllegalArgumentException("Enter valid number(3) of arguments");
			} else {
				bloomFilter = new BloomFilter(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
						Integer.parseInt(args[2]));
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Please provide a valid Input");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		File fout = new File("NewOutputBloomFilter.txt");
		FileWriter fileWriter = new FileWriter(fout);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		int[] result = bloomFilter.fillBloomFilter();

		for (int i = 0; i < result.length; i++) {
			if (i == 0) {
				printWriter.println(
						"After lookup of elements in A the number of elements are: " + Integer.toString(result[i]));
			} else if (i == 1) {
				printWriter.println(
						"After lookup of elements in B the number of elements are: " + Integer.toString(result[i]));
			}
		}
		printWriter.close();
	}
}