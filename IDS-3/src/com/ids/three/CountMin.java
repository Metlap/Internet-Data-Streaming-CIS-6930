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

public class CountMin {

	String[] flowIds;
	int[][] counterArray;
	// To store random numbers of length noOfHashes
	int[] s;
	public Map<String, Integer> estimatedSize;
	public Map<String, Integer> actualSize;
	public Map<String, Integer> error;
	int noOfHashes;
	int counterArraySize;
	int numberOfFlows;

	public CountMin(int numberOfFlows, int noOfHashes, int counterArraySize, String[] flowIds) {
		this.noOfHashes = noOfHashes;
		this.numberOfFlows = numberOfFlows;
		this.counterArraySize = counterArraySize;
		// 2D counter array
		counterArray = new int[noOfHashes][counterArraySize];
		this.estimatedSize = new HashMap<>();
		this.actualSize = new HashMap<>();
		this.error = new HashMap<>();
		s = new int[noOfHashes];
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
		this.flowIds = flowIds;
	}

	public void recordAll() {
		for (int i = 0; i < flowIds.length; i++) {
			String s = flowIds[i];
			if (s == null)
				continue;
			// Breaking each line of input into flowID and no of elements of flowID
			// break at 1 or more spaces
			String[] arr = s.split("\\s+");
			String flowId = arr[0];
			String flowSize = arr[1];
			actualSize.put(flowId, Integer.parseInt(flowSize));
			record(flowId);
		}
	}

	public void record(String flowId) {

		// get a hash for each of the noOfHashes arrays by XORing with the string
		// hashcode and record 1

		int value = actualSize.get(flowId);

		int[] xorResult = new int[s.length];
		for (int j = 0; j < s.length; j++) {
			xorResult[j] = flowId.hashCode() ^ s[j];
		}

		// for each element of flowID, record it in numberOfHashes arrays
		for (int i = 0; i < noOfHashes; i++) {
			for (int j = 0; j < value; j++) {
				int hash = Math.abs(xorResult[i]) % counterArraySize;
				counterArray[i][hash]++;
			}
		}

	}

	public void query() {
		for (int i = 0; i < flowIds.length; i++) {
			String s = flowIds[i];
			if (s == null)
				continue;
			String flowId = s.split("\\s+")[0];
			// Querying each flow ID
			query(flowId);
		}
	}

	public void query(String flowId) {
		int estimatedFlowSize = Integer.MAX_VALUE;
		for (int i = 0; i < noOfHashes; i++) {
			int queryHash = Math.abs(flowId.hashCode() ^ s[i]) % counterArraySize;
			estimatedFlowSize = Math.min(estimatedFlowSize, counterArray[i][queryHash]);
		}
		estimatedSize.put(flowId, estimatedFlowSize);
		error.put(flowId, estimatedFlowSize - actualSize.get(flowId));
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

		CountMin countMin = null;

		File file = new File("C:/Users/karth/OneDrive/Documents/masters/sem-1/IDS/project3input.txt");
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
				countMin = new CountMin(n, Integer.parseInt(args[0]), Integer.parseInt(args[1]), flowIds);
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Please provide a valid Input");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		File fout = new File("NewOutputCountMin.txt");
		FileWriter fileWriter = new FileWriter(fout);
		PrintWriter printWriter = new PrintWriter(fileWriter);

		countMin.recordAll();
		countMin.query();
		printWriter.println("Average Error among all flows = " + countMin.getAverage());
		List<Entry<String, Integer>> sortedList = countMin.sort();

		printWriter.println("FlowId" + "\t" + "Estimated_Flow_Size" + "\t" + "Actual_Flow_Size");
		for (int k = 0; k < 100; k++) {
			Map.Entry<String, Integer> e = sortedList.get(k);
			String flowId = e.getKey();
			int estimatedFlowSize = e.getValue();
			int actualFlowSize = countMin.actualSize.get(flowId);
			printWriter.println(flowId + "\t" + estimatedFlowSize + "\t" + actualFlowSize);
		}
		printWriter.println();
		printWriter.close();
	}

}
