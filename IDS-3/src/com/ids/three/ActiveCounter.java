package com.ids.three;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ThreadLocalRandom;

public class ActiveCounter {
	int[] counter;
	// Numerical part max value - 16 bits
	char max; // 65535 -> (char) (Math.pow(2,16)-1);
	int exponent;
	char numberBits;
	char exponentBits;
	int n;

	public ActiveCounter(int numberPartBits, int exponentPartBits) {
		this.numberBits = 0;
		this.exponentBits = 0;
		this.n = 1000000;
		this.max = (char) (Math.pow(2, numberPartBits) - 1);
		counter = new int[numberPartBits + exponentPartBits];
	}

	public int add() {
		for (int i = 0; i < n; i++) {
			// increment number part bits with probability
			if (ThreadLocalRandom.current().nextDouble() < (1 / Math.pow(2, exponentBits))) {

				
				// if number bits overflows then increment exponent bits and right shift number
				// bits
				if (numberBits +1 == max) {
					exponentBits++;
					numberBits++;
					// Right shift of numberBits
					numberBits = (char) ((int) numberBits >> 1);
				}
				
				else {
					numberBits++;
				}
			}
		}
		return (int) (numberBits * Math.pow(2, exponentBits));
	}

	public static void main(String[] args) throws IOException {

		ActiveCounter activeCounter = null;

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

		try {
			if (args.length != 2) {
				throw new IllegalArgumentException("Enter valid number(2) of arguments");
			} else {
				activeCounter = new ActiveCounter(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Please provide a valid Input");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}

		File fout = new File("NewOutputActiveCounter.txt");
		FileWriter fileWriter = new FileWriter(fout);
		PrintWriter printWriter = new PrintWriter(fileWriter);

		int result = activeCounter.add();
		// int finalans = Integer.parseInt(result,10);
		printWriter.println("Final value of active counter in decimal " + result);

		printWriter.println();
		printWriter.close();
	}

}
