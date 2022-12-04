package com.ids.four;

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

public class HyperLogLogSketch {

	int registerSize;
	int numberOfRegisters;
	int[] bitMap;
	Map<String, Integer> estimatedSize;
	Map<String, Integer> actualSize;
	Map<String, List<Integer>> flowValues;

	public HyperLogLogSketch(int numberOfRegisters) {
		this.registerSize = 5;
		this.numberOfRegisters = numberOfRegisters;
		// Initializing an array full of zeros of size m passed as argument
		this.bitMap = new int[this.numberOfRegisters];

		this.flowValues = new HashMap<String, List<Integer>>();
		this.actualSize = new HashMap<String, Integer>();
		this.estimatedSize = new HashMap<String, Integer>();
		for (int i = 2; i < 6; i++) {
			this.actualSize.put(String.valueOf(i), (int) Math.pow(10, i + 1));

			List<Integer> s = generateUniqueValues((int) Math.pow(10, i + 1));

			this.flowValues.put(String.valueOf(i), s);

		}

	}

	private List<Integer> generateUniqueValues(int range) {
		// generating random values
		List<Integer> s = new ArrayList<Integer>(range);
		Set<Integer> uniqueHashVal = new HashSet<>();
		for (int j = 0; j < range; j++) {
			while (true) {
				// Generate a random positive number
				int newHashGenerated = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
				if (!uniqueHashVal.contains(newHashGenerated)) {
					s.add(newHashGenerated);
					uniqueHashVal.add(newHashGenerated);
					break;
				}
			}
		}
		return s;
	}

//Integer.numberOfLeadingZeros(n)

	private void recordAndQuery() {

		int randomHashVal = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);

		for (String flowId : actualSize.keySet()) {

			List<Integer> spreadValues = this.flowValues.get(flowId);

			for (int value : spreadValues) {

				// H(e) is calculated below
				int hashVal = (value ^ randomHashVal);

				// Geometric hash G'(e)is calculated below -> no of leading zeros in range [1,32] inclusive
				int gDash = Integer.numberOfLeadingZeros(value);

				// B[H(e)]
				hashVal %= this.numberOfRegisters;

				// RECORDING
				this.bitMap[hashVal] = Math.max(this.bitMap[hashVal], gDash);

			}

			// QUERYING
			double alpha = 0.7213/ (1 + (1.079 / this.numberOfRegisters));
			double estimatedVal = 0;

			// HLL not accurate for small flow spread
			for (int i = 0; i < bitMap.length; i++) {
				double sumForOneBitMap = Math.pow(2, this.bitMap[i]);
				estimatedVal += (1 / sumForOneBitMap);
			}

			estimatedVal = (alpha * Math.pow(this.numberOfRegisters, 2)) * (1 / estimatedVal);

			this.estimatedSize.put(flowId, (int) estimatedVal);

			// resetting bits to 0s
			for (int i = 0; i < bitMap.length; i++) {
				bitMap[i] = 0;
			}

		}
	}

	public static void main(String[] args) throws IOException {

		HyperLogLogSketch hyperLogLogSketch = null;
		try {
			if (args.length != 1) {
				throw new IllegalArgumentException("Enter valid number(1) of arguments");
			} else {
				hyperLogLogSketch = new HyperLogLogSketch(Integer.parseInt(args[0]));
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Please provide a valid Input");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		File fout = new File("NewHyperLogLogSketch.txt");
		FileWriter fileWriter = new FileWriter(fout);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		hyperLogLogSketch.recordAndQuery();

		for (String flowId : hyperLogLogSketch.estimatedSize.keySet()) {

			printWriter.println("True flow spread: " + Integer.toString(hyperLogLogSketch.actualSize.get(flowId))
					+ " Estimated flow spread: " + Integer.toString(hyperLogLogSketch.estimatedSize.get(flowId)));
		}
		printWriter.close();
	}
}
