package com.ids.five;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class BSkt {

	int n, k;
	int registerSize;
	int numberOfRegisters;
	int numberOfEstimators;
	int numberOfFlows;
	int[] bitMap;
	int[][] A;
	Map<String, Integer> estimatedSpread;
	Map<String, Integer> actualSpread;
	Map<String, int[]> flowValues;

	public BSkt(int numberOfEstimators, int numberOfRegisters, int registerSize, int k, int numberOfFlows,
			HashMap<String, Integer> actualSpread) {

		// this.registerSize = 5;
		this.numberOfEstimators = numberOfEstimators;
		this.registerSize = registerSize;
		this.numberOfRegisters = numberOfRegisters;
		this.numberOfFlows = numberOfFlows;
		this.actualSpread = actualSpread;
		this.k = k;
		// Initializing an array full of zeros of size m passed as argument
		// this.bitMap = new int[this.numberOfRegisters];
		this.A = new int[this.numberOfEstimators][this.numberOfRegisters];
		this.flowValues = new HashMap<>();
		this.estimatedSpread = new HashMap<>();

		fillValuesInIPMap();

	}

	public void fillValuesInIPMap() {
		// Fill random unique values of spread size for each flow in the IPCountMap
		for (Map.Entry<String, Integer> flow : actualSpread.entrySet()) {

			String ipAddr = flow.getKey();
			int spreadVal = flow.getValue();

			Set<Integer> uniqueVal = new HashSet<>();
			for (int i = 0; i < spreadVal; i++) {
				while (true) {
					// Generate a random positive number
					int newHashGenerated = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
					if (!uniqueVal.contains(newHashGenerated)) {
						uniqueVal.add(newHashGenerated);
						break;
					}
				}
			}

			Integer[] uniqueValArray = uniqueVal.stream().toArray(Integer[]::new);

			int[] uniqueArr = new int[uniqueValArray.length];

			for (int p = 0; p < uniqueValArray.length; p++) {

				uniqueArr[p] = uniqueValArray[p].intValue();
			}

			flowValues.put(ipAddr, uniqueArr);
		}

	}

	private void recordAndQuery() {

		int[] randomHashValues = new int[this.k];

		int[] indices = new int[this.k];

		for (int i = 0; i < this.k; i++) {

			randomHashValues[i] = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
		}

		int randomHashVal = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);

		for (String flowId : actualSpread.keySet()) {

			int[] spreadValues = this.flowValues.get(flowId);

			for (int value : spreadValues) {

				for (int i = 0; i < this.k; i++) {

					// Hi(f)
					int index = (Math.abs(flowId.hashCode()) ^ randomHashValues[i]) % this.numberOfEstimators;

					indices[i] = index;

					// H(e) is calculated below
					int hashVal = (value ^ randomHashVal);

					// Geometric hash G'(e)is calculated below -> no of leading zeros in range
					// [1,32] inclusive
					int gDash = Integer.numberOfLeadingZeros(value);

					// B[H(e)]
					hashVal %= this.numberOfRegisters;

					// RECORDING A[Hi(f)][H(e)]
					this.A[index][hashVal] = Math.max(this.A[index][hashVal], gDash);
				}
			}
		}

		for (String flowId : actualSpread.keySet()) {

			// QUERYING
			double alpha = 0.7213 / (1 + (1.079 / this.numberOfRegisters));
			double estimatedValue = Integer.MAX_VALUE;

			// HLL not accurate for small flow spread
			for (int i = 0; i < indices.length; i++) {

				int index = (Math.abs(flowId.hashCode()) ^ randomHashValues[i]) % this.numberOfEstimators;
				double estimatedVal = 0;

				for (int j = 0; j < A[0].length; j++) {

					double sumForOneBitMap = Math.pow(2, this.A[index][j]);
					estimatedVal += (1 / sumForOneBitMap);
				}
				estimatedValue = Math.min(estimatedValue,
						(alpha * Math.pow(this.numberOfRegisters, 2)) * (1 / estimatedVal));
			}

			this.estimatedSpread.put(flowId, (int) estimatedValue);

		}
	}

	public static void main(String[] args) throws IOException {

		BSkt bSkt = null;
		try {
			if (args.length != 4) {
				throw new IllegalArgumentException("Enter valid number(4) of arguments");
			} else {

				File file = new File("./resources/project5input.txt");
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = null;
				int lineNo = 0;
				int n = 0;
				Set<String> ipSet = new HashSet<>();
				HashMap<String, Integer> actualSpread = new HashMap<>();
				while ((line = br.readLine()) != null) {
					if (lineNo == 0) {
						// First line consists of no of flows
						n = Integer.parseInt(line);
						lineNo++;
					} else {

						String[] ipVal = line.split("\\s+");
						while (true) {
							String ipUniqueFlow = ipVal[0];
							if (!ipSet.contains(ipUniqueFlow)) {
								ipSet.add(ipUniqueFlow);
								actualSpread.put(ipUniqueFlow, Integer.parseInt(ipVal[1]));
								break;
							}
						}
					}
				}
				br.close();

				bSkt = new BSkt(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]),
						Integer.parseInt(args[3]), n, actualSpread);
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Please provide a valid Input");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		File fout = new File("NewBSkt2.txt");
		FileWriter fileWriter = new FileWriter(fout);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		bSkt.recordAndQuery();

		// LinkedHashMap preserve the ordering of elements in which they are inserted
		LinkedHashMap<String, Integer> reverseSortedMap = new LinkedHashMap<>();

		// Use Comparator.reverseOrder() for reverse ordering
		bSkt.estimatedSpread.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

		int count = 0;

		for (String flowId : reverseSortedMap.keySet()) {
			count++;
			if (count > 25)
				break;
			printWriter.println(
					"FlowID is: " + flowId + " True flow spread: " + Integer.toString(bSkt.actualSpread.get(flowId))
							+ " Estimated flow spread: " + Integer.toString(reverseSortedMap.get(flowId)));

		}

		printWriter.close();
	}
}
