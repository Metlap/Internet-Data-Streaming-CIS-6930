package com.ids.three;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class CounterSketch {
	String[] flowIds;
	int[][] CounterArray;
	int[][] hashes;
	int[] s;
	Map<String, Integer> hashCodes;
	Map<String, Integer> estimatedSize;
	Map<String, Integer> actualSize;
	Map<String, Integer> error;
	int numberOfHashes;
	int sizeOfCounter;
	int numberOfFlows;
	int min = -65536;
	int max = 65535;

	public CounterSketch(int numberOfFlows, int numberOfHahses, int sizeOfCounterArray, String[] flowIds) {
		this.numberOfHashes = numberOfHahses;
		this.numberOfFlows = numberOfFlows;
		this.sizeOfCounter = sizeOfCounterArray;
		s = new int[numberOfHahses];
		CounterArray = new int[numberOfHahses][sizeOfCounterArray];
		hashes = new int[numberOfHahses][];
		estimatedSize = new HashMap<String, Integer>();
		actualSize = new HashMap<String, Integer>();
		error = new HashMap<String, Integer>();
		hashCodes = new HashMap<String, Integer>();
		this.flowIds = flowIds;
		// Genereate random values in S and also random flowIds
		Set<Integer> uniqueHashVal = new HashSet<>();
		for (int i = 0; i < s.length; i++) {
			while (true) {
				// Generate a random positive number
				int newHashGenerated = ThreadLocalRandom.current().nextInt(65536);
				if (!uniqueHashVal.contains(newHashGenerated)) {
					s[i] = newHashGenerated;
					uniqueHashVal.add(newHashGenerated);
					break;
				}
			}
		}

		for (int i = 0; i < flowIds.length; i++) {
			String s = flowIds[i];
			if (s == null)
				continue;
			String[] arr = s.split("\\s+");
			String flowId = arr[0];
			hashCodes.put(flowId, ThreadLocalRandom.current().nextInt(max - min) + min);
		}

	}


	public void recordAll() {
		for (int i = 0; i < flowIds.length; i++) {
			String s = flowIds[i];
			if (s == null)
				continue;
			String[] arr = s.split("\\s+");
			String flowId = arr[0];
			String flowSize = arr[1];
			actualSize.put(flowId, Integer.parseInt(flowSize));
			record(flowId);
		}
	}

	public void record(String flowId) {
		// get a hash for each of the numberOfHashes arrays by xoring with the string
		// hashcode and record 1
		int value = actualSize.get(flowId);
		for (int i = 0; i < numberOfHashes; i++) {
			int hash = (hashCodes.get(flowId) ^ s[i]) % sizeOfCounter;
			// based on first bit of hash, consider +ve or -ve
			char firstBit = getFirstBit(hash);
			hash = Math.abs(hash);
			// loop over all elements of a packet
			for (int j = 0; j < value; j++) {
				if (firstBit == '0')
					CounterArray[i][hash]--;
				else
					CounterArray[i][hash]++;
			}

		}
	}

	public char getFirstBit(int hash) {
		String binary = Integer.toBinaryString(hash);
		if (binary.length() == 32)
			return binary.charAt(0);
		return '0';
	}

	public void queryAll() {
		for (int i = 0; i < flowIds.length; i++) {
			String s = flowIds[i];
			if (s == null)
				continue;
			String flowId = s.split("\\s+")[0];
			query(flowId);
		}
	}

	public void query(String flowId) {
		int estimate = 0;
		List<Integer> estimates = new ArrayList<>();
		for (int i = 0; i < numberOfHashes; i++) {
			int hash = (hashCodes.get(flowId) ^ s[i]) % sizeOfCounter;
			char firstBit = getFirstBit(hash);
			hash = Math.abs(hash);

			if (firstBit == '0') {
				estimate = -CounterArray[i][hash];
			} else {
				estimate = CounterArray[i][hash];
			}
			estimates.add(estimate);
		}
		// Retrieving the median
		Collections.sort(estimates);
		int n = estimates.size();
		estimate = 0;
		if (n % 2 == 1) {
			estimate = estimates.get(n / 2);
		} else {
			estimate = (estimates.get(n / 2) + estimates.get((n / 2) - 1)) / 2;
		}
		estimatedSize.put(flowId, estimate);
		error.put(flowId, estimate - actualSize.get(flowId));
	}

	public double getAverage() {
		double avg = 0.0;
		double total = 0.0;
		for (Map.Entry<String, Integer> entry : error.entrySet()) {
			total += Math.abs(entry.getValue());
		}
		avg = total / error.size();
		return avg;
	}

	public List<Entry<String, Integer>> sort() {
		List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(estimatedSize.entrySet());
		Collections.sort(sortedList,
				((Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) -> e2.getValue() - e1.getValue()));
		// Get first 100 elements from the sorted list

		return sortedList;
	}

	public static void main(String[] args) throws IOException {

		CounterSketch counterSketch = null;
		File file = new File("./resources/project3input.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		int n = 0;
		String[] flowIds = null;
		int lineNo = 0;
		int lineCount = 0;
		while ((line = br.readLine()) != null) {
			if (lineNo == 0) {
				// First line consists of no of flows
				n = Integer.parseInt(line);
				flowIds = new String[n + 1];
				lineNo++;
			} else {
				flowIds[lineCount++] = line;
			}
		}
		br.close();

		// Using noOfHashes=3 arrays/ hashes and 3k counters per each
		// countMin = new CountMin(n, 3, 3000, flowIds);

		try {
			if (args.length != 2) {
				throw new IllegalArgumentException("Enter valid number(2) of arguments");
			} else {
				counterSketch = new CounterSketch(n, Integer.parseInt(args[0]), Integer.parseInt(args[1]), flowIds);
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Please provide a valid Input");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		File fout = new File("NewOutputCounterSketch.txt");
		FileWriter fileWriter = new FileWriter(fout);
		PrintWriter printWriter = new PrintWriter(fileWriter);

		counterSketch.recordAll();
		counterSketch.queryAll();
		printWriter.println("Average Error among all flows = " + counterSketch.getAverage());
		List<Entry<String, Integer>> sortedList = counterSketch.sort();

		printWriter.println("FlowId" + "\t" + "Estimated_Flow_Size" + "\t" + "Actual_Flow_Size");
		for (int k = 0; k < 100; k++) {
			Map.Entry<String, Integer> e = sortedList.get(k);
			String flowId = e.getKey();
			int estimatedFlowSize = e.getValue();
			int actualFlowSize = counterSketch.actualSize.get(flowId);
			printWriter.println(flowId + "\t" + estimatedFlowSize + "\t" + actualFlowSize);
		}
		printWriter.println();
		printWriter.close();
	}
}