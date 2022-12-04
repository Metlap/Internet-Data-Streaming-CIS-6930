import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class CodedBloomFilter {
	int numberOfSets;
	int numberOfElements;
	int numberOfFilters;
	// no of bits per filter
	int numberOfBits;
	// No of hashes for each element in Set
	int[] s;
	// Map containing log(g +1 ) bloom filters
	Map<Integer, int[]> bloomFilterCodeMap = new HashMap<>();
	int codeLength = 0;

	CodedBloomFilter(int numberOfSets, int numberOfElements, int numberOfFilters, int numberOfBits,
			int numberOfHashes) {
		this.numberOfSets = numberOfSets;
		this.numberOfElements = numberOfElements;
		this.numberOfFilters = numberOfFilters;
		this.numberOfBits = numberOfBits;
		s = new int[numberOfHashes];
		// Reducing lookup from g to log(g+1) filters
		assignCode();
		// Generating 7000 distinct elements
		Set<Integer> uniqueHashVal = new HashSet<>();
		for (int i = 0; i < s.length; i++) {
			while (true) {
				// Generate a random positive number
				int newHashGenerated = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
				if (!uniqueHashVal.contains(newHashGenerated)) {
					s[i] = newHashGenerated;
					uniqueHashVal.add(newHashGenerated);
					break;
				}
			}
		}
	}

	private void assignCode() {
		this.codeLength = (int) Math.ceil(Math.log(numberOfSets + 1) / Math.log(2));
		for (int i = 0; i < codeLength; i++) {
			bloomFilterCodeMap.put(i, new int[numberOfBits]);
		}
		this.numberOfFilters = this.codeLength;
	}

	// Driver Function for the program
	public int fillBloomFilter() {
		// List of all sets and their corresponding hashes
		List<Map<Integer, int[]>> list = new ArrayList<>();
		// List of all the elements
		Map<Integer, int[]> allElements = new HashMap<>();
		for (int i = 0; i < numberOfSets; i++) {
			Map<Integer, int[]> filterMap = new HashMap<>();
			for (int k = 0; k < numberOfElements; k++) {
				int newRandom;
				while (true) {
					newRandom = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
					if (!allElements.containsKey(newRandom)) {
						break;
					}
				}
				// Calculating the k hashes for each entry of the Set into the filter
				int[] XorResult = new int[s.length];
				for (int j = 0; j < s.length; j++) {
					XorResult[j] = newRandom ^ s[j];
				}

				filterMap.put(newRandom, XorResult);
				allElements.put(newRandom, XorResult);

			}
			list.add(filterMap);
		}
		Map<Integer, String> lookupCodeMap = new HashMap<>();
		for (int k = 0; k < list.size(); k++) {
			// Assign each set a code of length log(g+1)
			String binaryString = String.format("%" + codeLength + "s", Integer.toBinaryString(k + 1)).replaceAll(" ",
					"0");
			// Iterating through the above generated code
			for (int j = 0; j < binaryString.length(); j++) {
				// char at j corresponds to jth bloom filter -> if 1 then insert in that bloom
				// filter
				if (binaryString.charAt(j) == '1') {
					encode(list.get(k), j, lookupCodeMap, binaryString);
				}
			}
		}
		int count = lookUp(lookupCodeMap, allElements);
		return count;
	}

	private int lookUp(Map<Integer, String> lookupCodeMap, Map<Integer, int[]> allElements) {
		// Lookup
		int count = 0;
		for (Map.Entry<Integer, int[]> entry : allElements.entrySet()) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < numberOfFilters; j++) {
				int numHashCount = 0;
				int[] filter = bloomFilterCodeMap.get(j);
				int[] resultHash = entry.getValue();
				for (int i = 0; i < resultHash.length; i++) {
					if (filter[resultHash[i] % numberOfBits] == 1) {
						numHashCount++;
					}
				}
				if (numHashCount == s.length) {
					sb.append(1);
				} else {
					sb.append(0);
				}
			}

			if (lookupCodeMap.containsKey(entry.getKey()) && lookupCodeMap.get(entry.getKey()).equals(sb.toString())) {
				count++;
			}
		}
		return count;
	}

	// Encode all Bits of the seen value to 1
	private void encode(Map<Integer, int[]> filterMap, int bloomFilterCodeMapIndex, Map<Integer, String> lookupCodeMap,
			String binaryString) {
		int[] bitMapAtIndex = bloomFilterCodeMap.get(bloomFilterCodeMapIndex);
		for (Map.Entry<Integer, int[]> entry : filterMap.entrySet()) {
			lookupCodeMap.put(entry.getKey(), binaryString);
			int[] resultHash = entry.getValue();
			for (int hash : resultHash) {
				bitMapAtIndex[hash % numberOfBits] = 1;
			}
		}
		// after changing the bitmap values, put the new bitmap back into the map
		bloomFilterCodeMap.replace(bloomFilterCodeMapIndex, bitMapAtIndex);
	}

	public static void main(String[] args) throws IOException {

		CodedBloomFilter codedBloomFilter = null;
		try {
			if (args.length != 5) {
				throw new IllegalArgumentException("Enter valid number(5) of arguments");
			} else {
				codedBloomFilter = new CodedBloomFilter(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
						Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Please provide a valid Input");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		File fout = new File("NewOutputCodedBloomFilter.txt");
		FileWriter fileWriter = new FileWriter(fout);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		int result = codedBloomFilter.fillBloomFilter();

		printWriter.println("Number of elements whose lookup results are correct: " + Integer.toString(result));

		printWriter.close();
	}
}