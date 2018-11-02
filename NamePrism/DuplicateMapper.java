package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DuplicateMapper {

	private static final String SOURCE = "/Users/pramod/Documents/GoogleDrive/RA/AI/census_data.csv";
	private static final String DESTINATION = "/Users/pramod/Documents/GoogleDrive/RA/Scrap/clean/decades_cleaned.csv";
	private static final String OUTPUT = "/Users/pramod/Documents/GoogleDrive/RA/Scrap/clean/eth/decades_cleaned_us";
	private static final String NOOUTPUT = "/Users/pramod/Documents/GoogleDrive/RA/Scrap/clean/eth/decades_cleaned.csv_census_nop.csv";

	private static final Map<String, String> nameMap = new HashMap<String, String>();

	public static void main(String[] args) throws IOException {

		BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT));
		BufferedWriter nbw = new BufferedWriter(new FileWriter(NOOUTPUT));

		/*
		 * try (BufferedReader br = new BufferedReader(new FileReader(SOURCE))) { String
		 * line = null; while ((line = br.readLine()) != null) { String data[] =
		 * line.split(","); String name = data[0].toLowerCase(); String val =
		 * line.substring(name.length() + 1); nameMap.put(name, val); } } catch
		 * (Exception ex) { ex.printStackTrace(); }
		 */

		// bw.write(
		// "inventor_first_name,inventor_last_name,inventor_country,inventor_id,rank,count,prop100k,cum_prop100k,pctwhite,pctblack,pctapi,pctaian,pct2prace,pcthispanic
		// \n");
		bw.write(
				"patent_id,patent_issue_date,inventor_name_first,inventor_name_middle,inventor_name_last,city,state,country");
		// "application_number,filing_date,uspc_class,cpc4,inventor_name_first,inventor_name_middle,inventor_name_last,inventor_rank,inventor_region_code,inventor_country_code,"
		// +
		// "rank,count,prop100k,cum_prop100k,pctwhite,pctblack,pctapi,pctaian,pct2prace,pcthispanic
		// \n");

		int lineCount = 0;
		int outCount = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(DESTINATION))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				String data[] = line.split(",");
				if (data.length >= 7) {
					try {
						// System.out.println(line);
						if (data[7].equals("US")) { // Filter to take only US data.
							String name = data[2].toLowerCase() + data[4].toLowerCase();
							String result = nameMap.get(name);
							if (result == null) {
								nameMap.put(name, "1");
								bw.write(line + "\n");
							}
						}
					} catch (Exception ex) {
						System.err.println(line);
					}

				}
				// System.out.println("Line: " + count++);
			}
			System.out.println("Done...");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		bw.close();
		nbw.close();
		// System.out.println("Donere : " + nameMap.size() + " " + resultMap.size() + "
		// ::" + list.size());

	}

}
