package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class SplitMerge {

	private static final String COMPARE = "/Users/pramod/Documents/GoogleDrive/EclipseWorkspace/DataScience/src/data/output.csv";
	private static final String MAIN_SOURCE = "/Users/pramod/Documents/GoogleDrive/EclipseWorkspace/DataScience/src/data/input.csv";
	private static final String SOURCE = "/Users/pramod/Documents/GoogleDrive/EclipseWorkspace/DataScience/src/data/output.csv";
	private static final String DESTINATION = "/Users/pramod/Documents/GoogleDrive/EclipseWorkspace/DataScience/src/data/output_nameprism.csv";
	static int LINES = 1000;
	static int COUNTER = 0;
	private static Map<String, String> map = new HashMap<String, String>();

	public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
		//split(SOURCE, DESTINATION);

		// load();
		// leftOver(SOURCE, DESTINATION);

		/*
		joinDocs(
				new String[] { "/Users/pramod/Documents/GoogleDrive/RA/Scrap/clean/M/nips_cleaned_namep_4_1_parsed.csv",
						"/Users/pramod/Documents/GoogleDrive/RA/Scrap/clean/M/nips_cleaned_namep_4_2_parsed.csv",
						"/Users/pramod/Documents/GoogleDrive/RA/Scrap/clean/M/nips_cleaned_namep_4_3_parsed.csv",
						"/Users/pramod/Documents/GoogleDrive/RA/Scrap/clean/M/nips_cleaned_nameprism.csv" },
				"/Users/pramod/Documents/GoogleDrive/RA/Scrap/clean/M/nips_cleaned_nameprism_final.csv");
		*/

		nameprism(SOURCE, DESTINATION);

	}

	static String getOutputName() {
		return DESTINATION + (++COUNTER) + ".csv";
	}

	static int getStart() {
		return LINES * COUNTER;
	}

	static int getEnd() {
		return getStart() + LINES;
	}

	public static void split(String source, String dest) {
		try (BufferedReader br = new BufferedReader(new FileReader(new File(SOURCE)))) {
			String line = null;
			boolean done = false;
			while (!done || COUNTER == 500) {
				int start = getStart();
				int end = getEnd();
				System.out.println("Reading lines : " + start + " and " + end);
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(getOutputName())))) {
					for (int i = start; i < end; i++) {
						line = br.readLine();
						if (line != null) {
							bw.write(line + "\n");
						} else {
							done = true;
							break;
						}
					}
				}
			}
			System.out.println("Done");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void load() throws FileNotFoundException, IOException {
		try (BufferedReader comp = new BufferedReader(new FileReader(new File(COMPARE)))) {
			String line = null;
			while ((line = comp.readLine()) != null) {
				int index = line.lastIndexOf("US,");
				String sub = line.substring(0, index + 2);
				map.put(sub, 1 + "");
			}
		}
	}

	public static void leftOver(String source, String dest) {
		try (BufferedReader br = new BufferedReader(new FileReader(new File(SOURCE)))) {
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(getOutputName())))) {
				String line = null;
				while ((line = br.readLine()) != null) {
					if (map.get(line) == null) {
						bw.write(line + "\n");
					}
				}
			}
			System.out.println("Done");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void joinDocs(String[] inputFiles, String output) throws FileNotFoundException, IOException {
		List<String> data = new ArrayList<String>();
		String line = null;
		for (String input : inputFiles) {
			int count = 0;
			try (BufferedReader br = new BufferedReader(new FileReader(new File(input)))) {
				while ((line = br.readLine()) != null) {
					data.add(line);
					System.out.println("Line: " + count++);
				}
			}
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)))) {
			for (String element : data) {
				bw.write(element + "\n");
			}
		}

	}

	static Map<String, String> nameMap = new HashMap<>();

	public static void nameprism(String input, String output) throws FileNotFoundException, IOException, JSONException {
		List<String> data = new ArrayList<String>();
		String line = null;
		int oute = 1;
		int count = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(new File(input)))) {
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",\\{");
				//System.out.println(line);
				//System.out.println(tokens[1]);
				JSONObject json = new JSONObject("{" + tokens[1]);
				String a = json.get("2PRACE").toString();
				String b = json.get("Hispanic").toString();
				String c = json.get("API").toString();
				String d = json.get("Black").toString();
				String e = json.get("AIAN").toString();
				String g = json.get("White").toString();
				String join = a + "," + b + "," + c + "," + d + "," + e + "," + g;
				data.add(tokens[0] + "US," + join);
				nameMap.put(tokens[0], join);
				System.out.println("File::Line" + oute + " :: " + count++);
			}
		}
		oute++;

		//		try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)))) {
		//			for (String element : data) {
		//				//bw.write(element + "\n");
		//			}
		//		}

		List<String> lines = Files.readAllLines(Paths.get(MAIN_SOURCE));
		for (String l : lines) {
			String res = nameMap.get(l);
			if (res == null) {
				Files.write(Paths.get(DESTINATION), "\n".getBytes(), StandardOpenOption.APPEND);
			} else {
				Files.write(Paths.get(DESTINATION), (l + "," + res + "\n").getBytes(), StandardOpenOption.APPEND);
			}

		}

	}
}
