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

public class Bitmap {

	int bitMapSize;
	int[] bitMap;
	Map<String, Integer> estimatedSize;
	Map<String, Integer> actualSize;
	Map<String, List<Integer>> flowValues;

	public Bitmap(int bitMapSize) {
		this.bitMapSize = bitMapSize;
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

	private void recordAndQuery() {

		int randomHashVal = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);

		for (String flowId : actualSize.keySet()) {

			List<Integer> spreadValues = this.flowValues.get(flowId);

			for (int value : spreadValues) {

				int hashVal = (value ^ randomHashVal) % this.bitMapSize;

				this.bitMap[hashVal] = 1;
			}

			int zeros = 0;

			for (int i = 0; i < bitMap.length; i++) {
				if (bitMap[i] == 0)
					zeros++;
			}

			double fractionOfZeros = (double) zeros / this.bitMapSize;
			
			// The value will be equal to INT_MAX if all the bits in bitMap are 1s (because ln(0) is undefined)
			int estimatedSizeVal = (int) ((-1) * (this.bitMapSize) * (Math.log(fractionOfZeros)));

			this.estimatedSize.put(flowId, estimatedSizeVal);

			// resetting bits to 0s
			for (int i = 0; i < bitMap.length; i++) {
				bitMap[i] = 0;
			}

		}
	}

	public static void main(String[] args) throws IOException {

		Bitmap bitMap = null;
		try {
			if (args.length != 1) {
				throw new IllegalArgumentException("Enter valid number(1) of arguments");
			} else {
				bitMap = new Bitmap(Integer.parseInt(args[0]));
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Please provide a valid Input");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		File fout = new File("NewOutputBitmap.txt");
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
