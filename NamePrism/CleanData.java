package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class CleanData {

	private static final String SOURCE = "/Users/pramod/Documents/RA/inventor0000000000010.csv";
	private static final String DESTINATION = "/Users/pramod/Documents/RA/cleaned/cleaned_inventor0000000000010.csv";

	private static String TOCLEAN = "3,4,";

	public static void main(String[] args) {
		clean(SOURCE, DESTINATION);
	}

	public static void clean(String source, String dest) {
		try (BufferedReader br = new BufferedReader(new FileReader(new File(SOURCE)))) {
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(DESTINATION)))) {
				String line = null;
				int count = 0;
				while ((line = br.readLine()) != null) {
					count++;
					StringBuilder output = new StringBuilder();
					String[] tokens = line.split(",");
					for (int i = 0; i < tokens.length; i++) {
						output.append(applyRule(tokens[i], i));
						if (i != tokens.length - 1) {
							output.append(",");
						}
					}
					//					System.out.println("Line : " + count);
					bw.write(output.toString() + "\n");
				}
			}
			System.out.println("Done");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String applyRule(String token, int index) {
		if (token == null) {
			return "";
		}

		if (!TOCLEAN.contains(index + ",")) {
			return token;
		}

		String out = token;
		out = out.trim();
		out = out.toLowerCase();
		out = out.replace(".", "");
		out = out.replace("'", "");
		out = out.replace("\"", "");
		out = out.replace("$", "s");
		out = out.trim();
		return out;
	}

}
