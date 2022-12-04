import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class CountingBloomFilter {
	int[] countingFilter;
	int numberOfElements;
	int numberOfElementsToBeRemoved;
	int numberOfElementsToBeAdded;
	int numberOfBits;
	int[] s;

	CountingBloomFilter(int numberOfElements, int numberOfElementsToBeRemoved, int numberOfElementsToBeAdded,
			int numberOfBits, int numberOfHashes) {
		this.numberOfElements = numberOfElements;
		this.numberOfBits = numberOfBits;
		this.numberOfElementsToBeRemoved = numberOfElementsToBeRemoved;
		this.numberOfElementsToBeAdded = numberOfElementsToBeAdded;
		countingFilter = new int[numberOfBits];
		s = new int[numberOfHashes];
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

	public int fillBloomFilter() {
		Map<Integer, int[]> filterMap = new HashMap<>();
		Set<Integer> originalElements = new HashSet<>();
		generateRandomElements(filterMap, originalElements);
		encode(filterMap);
		removeElements(filterMap);
		addElements(filterMap, originalElements);
		return lookup(filterMap, originalElements);
	}

	// Removing the values from Counting Filter
	private void removeElements(Map<Integer, int[]> filterMap) {
		int size = numberOfElementsToBeRemoved;
		for (Map.Entry<Integer, int[]> entry : filterMap.entrySet()) {
			int[] resultHash = entry.getValue();
			for (int hash : resultHash) {
				// Subtract the count for each of the k-hashes
				countingFilter[hash % numberOfBits]--;
			}
			size--;
			if (size == 0) {
				break;
			}
		}
	}

	// Generate new Set of Elements and Encode them in the Filter
	private void addElements(Map<Integer, int[]> filterMap, Set<Integer> orignalElementSet) {

		for (int i = 0; i < numberOfElementsToBeAdded; i++) {

			int newRandom = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);

			// Calculating the k hashes for each entry of the Set into the filter
			int[] XorResult = new int[s.length];
			for (int j = 0; j < s.length; j++) {
				XorResult[j] = newRandom ^ s[j];
			}

			filterMap.put(newRandom, XorResult);

			for (int XORedHash : XorResult) {
				countingFilter[XORedHash % numberOfBits]++;
			}
		}
	}

	// Put all the bits that are hashed and increase count
	private void encode(Map<Integer, int[]> parentMap) {
		for (Map.Entry<Integer, int[]> entry : parentMap.entrySet()) {
			int[] resultHash = entry.getValue();
			for (int hash : resultHash) {
				// Increase the count
				countingFilter[hash % numberOfBits]++;
			}
		}
	}

	// Check if the value exists in the table
	private int lookup(Map<Integer, int[]> filterMap, Set<Integer> originalElement) {
		int count = 0;
		for (int element : originalElement) {
			int[] resultHash = filterMap.get(element);
			for (int hash : resultHash) {
				if (countingFilter[hash % numberOfBits] <= 0) {
					count++;
					break;
				}
			}
		}
		return numberOfElements - count;
	}

	// Generating random values for A
	private void generateRandomElements(Map<Integer, int[]> filterMap, Set<Integer> orignalElementSet) {

		for (int i = 0; i < numberOfElements; i++) {
			int newRandom;
			while (true) {
				newRandom = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
				if (!orignalElementSet.contains(newRandom)) {
					orignalElementSet.add(newRandom);
					break;
				}
			}
			// Calculating the k hashes for each entry of the Set into the filter
			int[] XorResult = new int[s.length];
			for (int j = 0; j < s.length; j++) {
				XorResult[j] = newRandom ^ s[j];
			}

			filterMap.put(newRandom, XorResult);

		}
	}

	public static void main(String[] args) throws IOException {

		CountingBloomFilter countingBloomFilter = null;
		try {
			if (args.length != 5) {
				throw new IllegalArgumentException("Enter valid number(5) of arguments");
			} else {
				countingBloomFilter = new CountingBloomFilter(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
						Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Please provide a valid Input");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		File fout = new File("NewOutputCountingBloomFilter.txt");
		FileWriter fileWriter = new FileWriter(fout);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		int result = countingBloomFilter.fillBloomFilter();

		printWriter.println("After lookup of elements in A the number of elements are: " + Integer.toString(result));

		printWriter.close();
	}

}
