package com.ids.four;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class ProbabilisticBitmap {

	int bitMapSize;
	double probability;
	int[] bitMap;
	Map<String, Integer> estimatedSize;
	Map<String, Integer> actualSize;
	Map<String, List<Integer>> flowValues;

	public ProbabilisticBitmap(int bitMapSize, double probability) {
		this.bitMapSize = bitMapSize;
		this.probability = probability;
		// Initializing an array full of zeros of size m passed as argument
		this.bitMap = new int[bitMapSize];

		this.flowValues = new HashMap<String, List<Integer>>();
		this.actualSize = new HashMap<String, Integer>();
		this.estimatedSize = new HashMap<String, Integer>();
		for (int i = 1; i < 6; i++) {
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

	private int findMaxHash(List<Integer> spreadValues) {

		return Collections.max(spreadValues);
	}

	private void recordAndQuery() {

		int randomHashVal = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);

		// SecondHash
		int randomHashVal2 = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);

//		int maxHashVal = Integer.MAX_VALUE -1;

		for (String flowId : actualSize.keySet()) {

			List<Integer> spreadValues = this.flowValues.get(flowId);

			int maxHashVal = findMaxHash(spreadValues);

			for (int value : spreadValues) {

//				double randomProbability = ThreadLocalRandom.current().nextDouble();
//				
//				//Updating values in bitmap based on probability
//				if(randomProbability < this.probability) {
//				
//				int hashVal = (value ^ randomHashVal) % this.bitMapSize;

				int hashVal = (value ^ randomHashVal);

//				//Updating values in bitmap based on probability
				if (hashVal < (maxHashVal * this.probability)) {

				//	Using different hash function to record
					hashVal=((value ^ randomHashVal2))%this.bitMapSize;

					this.bitMap[hashVal] = 1;

				}
			}

			int zeros = 0;

			for (int i = 0; i < bitMap.length; i++) {
				if (bitMap[i] == 0)
					zeros++;
			}

			double fractionOfZeros = (double) zeros / this.bitMapSize;

			// The value will be equal to INT_MAX if all the bits in bitMap are 1s (because
			// ln(0) is undefined)
			int estimatedSizeVal = (int) ((-1) * (double) (1 / this.probability) * (this.bitMapSize)
					* (Math.log(fractionOfZeros)));

			this.estimatedSize.put(flowId, estimatedSizeVal);

			// resetting bits to 0s
			for (int i = 0; i < bitMap.length; i++) {
				bitMap[i] = 0;
			}

		}
	}

	public static void main(String[] args) throws IOException {

		ProbabilisticBitmap bitMap = null;
		try {
			if (args.length != 2) {
				throw new IllegalArgumentException("Enter valid number(2) of arguments");
			} else {
				bitMap = new ProbabilisticBitmap(Integer.parseInt(args[0]), Double.parseDouble(args[1]));
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Please provide a valid Input");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		File fout = new File("NewOutputProbabilisticBitmap.txt");
		FileWriter fileWriter = new FileWriter(fout);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		bitMap.recordAndQuery();

		for (String flowId : bitMap.estimatedSize.keySet()) {

			printWriter.println("True flow spread: " + Integer.toString(bitMap.actualSize.get(flowId))
					+ " Estimated flow spread: " + Integer.toString(bitMap.estimatedSize.get(flowId)));
		}
		printWriter.close();
	}
}