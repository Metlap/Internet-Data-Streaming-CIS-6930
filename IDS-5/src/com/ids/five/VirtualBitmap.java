package com.ids.five;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class VirtualBitmap {

	int l, m, n;
	// Parent bitMap
	int[] parentBitmap;
	int[] virtualBitmap;
	int[] randomArrayForHash;
	// Map to store flowId and flowSpread values read from input file
	HashMap<String, Integer> ipCountMap;
	// Map to store flowId and corresponding unique elements of input flow spread
	HashMap<String, int[]> ipValueMap;

	// To store the estimated spread value
	HashMap<String, Integer> estimatedSize;

	public VirtualBitmap(int m, int l, int n, HashMap<String, Integer> ipCountMap) {

		this.l = l;
		this.m = m;
		this.n = n;
		this.ipCountMap = ipCountMap;

		this.parentBitmap = new int[m];
		this.randomArrayForHash = new int[l];

		Set<Integer> uniqueVal = new HashSet<>();
		for (int i = 0; i < randomArrayForHash.length; i++) {
			while (true) {
				// Generate a random positive number
				int newHashGenerated = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
				if (!uniqueVal.contains(newHashGenerated)) {
					uniqueVal.add(newHashGenerated);
					randomArrayForHash[i] = newHashGenerated;
					break;
				}
			}
		}

		this.ipValueMap = new HashMap<>();

		// Fill values in the ipValueMap
		fillValuesInIPMap();

	}

	public void fillValuesInIPMap() {
		// Fill random unique values of spread size for each flow in the IPCountMap
		for (Map.Entry<String, Integer> flow : ipCountMap.entrySet()) {

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

			ipValueMap.put(ipAddr, uniqueArr);
		}

	}

	public void record() {

		int firstHash = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);

		for (Entry<String, int[]> entry : ipValueMap.entrySet()) {
			String ip = entry.getKey();
			int[] values = entry.getValue();
			for (int i : values) {

				// setting VBf[H(e)] to one
				int hashIndexInVirtualBitMap = (i ^ firstHash) % this.l;
				// setting B[H(f⊗R[H(e) mod 𝑙]) mod m]=1
				int hashIndexInParentBitMap = (Math.abs(ip.hashCode()) ^ randomArrayForHash[hashIndexInVirtualBitMap])
						% this.m;

				this.parentBitmap[hashIndexInParentBitMap] = 1;
			}
		}
	}

	public void estimate() {

		this.estimatedSize = new HashMap<>();

		int zeroes = 0;
		for (int i = 0; i < parentBitmap.length; i++) {
			if (parentBitmap[i] == 0) {
				zeroes++;
			}
		}

		for (Map.Entry<String, int[]> entry : ipValueMap.entrySet()) {

			String ip = entry.getKey();

			// l * ln(VB)
			double estimateInParent = this.l * Math.log((double) zeroes / this.m);

			double numerator = 0;

			for (int i = 0; i < l; i++) {

				int ipHash = Math.abs(ip.hashCode());
				int elementHash = i;
				int index = (ipHash ^ randomArrayForHash[elementHash]) % m;

				if (parentBitmap[index] == 0) {
					numerator++;
				}
			}

			// l * ln(Vf)
			double estimateInVirtual = this.l * Math.log(numerator / this.l);

			// to prevent insertion of negative values (instead insert 0)
			this.estimatedSize.put(ip, (int) (estimateInParent - estimateInVirtual) < 0 ? 0
					: (int) (estimateInParent - estimateInVirtual));

		}
	}

	public static void main(String[] args) throws IOException {

		VirtualBitmap virtualBitmap = null;

		try {
			if (args.length != 2) {
				throw new IllegalArgumentException("Enter valid number(2) of arguments");
			} else {

				File file = new File("./resources/project5input.txt");
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = null;
				int lineNo = 0;
				int n = 0;
				Set<String> ipSet = new HashSet<>();
				HashMap<String, Integer> ipCountMap = new HashMap<>();
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
								ipCountMap.put(ipUniqueFlow, Integer.parseInt(ipVal[1]));
								break;
							}
						}
					}
				}
				br.close();

				virtualBitmap = new VirtualBitmap(Integer.parseInt(args[0]), Integer.parseInt(args[1]), n, ipCountMap);
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Please provide a valid Input");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		File fout = new File("NewOutputVirtualBitmap.txt");
		FileWriter fileWriter = new FileWriter(fout);
		PrintWriter printWriter = new PrintWriter(fileWriter);

		virtualBitmap.record();
		virtualBitmap.estimate();

		printWriter.println("FlowId" + "\t" + "Estimated_Flow_Spread" + "\t" + "Actual_Flow_Spread");
		for (Map.Entry<String, Integer> entry : virtualBitmap.ipCountMap.entrySet()) {
			String flowId = entry.getKey();
			Integer actualFlowSpread = entry.getValue();
			Integer estimatedSpread = virtualBitmap.estimatedSize.get(entry.getKey());
			printWriter.println(flowId + "\t" + estimatedSpread + "\t" + actualFlowSpread);
		}
		printWriter.println();
		printWriter.close();

		// second file output containing parsed input for graph.
		File foutTwo = new File("NewOutputVirtualBitmapGraph.txt");
		FileWriter fileWriterTwo = new FileWriter(foutTwo);
		PrintWriter printWriterTwo = new PrintWriter(fileWriterTwo);


		for (Map.Entry<String, Integer> entry : virtualBitmap.ipCountMap.entrySet()) {
			Integer actualFlowSpread = entry.getValue();
			Integer estimatedSpread = virtualBitmap.estimatedSize.get(entry.getKey());
			if (actualFlowSpread < 500 && estimatedSpread < 500)
				printWriterTwo.print(estimatedSpread + ",");
		}

		printWriterTwo.println();

		for (Map.Entry<String, Integer> entry : virtualBitmap.ipCountMap.entrySet()) {
			Integer actualFlowSpread = entry.getValue();
			Integer estimatedSpread = virtualBitmap.estimatedSize.get(entry.getKey());
			if (actualFlowSpread < 500 && estimatedSpread < 500)
				printWriterTwo.print(actualFlowSpread + ",");
		}

		printWriterTwo.println();
		printWriterTwo.close();

	}

}
