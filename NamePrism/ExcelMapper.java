package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExcelMapper {

	private static final String SOURCE = "/Users/pramod/Documents/GoogleDrive/RA/AI/census_data.csv";
	private static final String DESTINATION = "/Users/pramod/Documents/GoogleDrive/RA/Scrap/clean/M/nips_cleaned.csv";
	private static final String OUTPUT = "/Users/pramod/Documents/GoogleDrive/RA/Scrap/clean/M/nips_cleaned_census.csv";
	private static final String NOOUTPUT = "/Users/pramod/Documents/GoogleDrive/RA/Scrap/clean/M/nips_cleaned_nop.csv";
	static int LAST_NAME_INDEX = 3;
	private static final Map<String, String> nameMap = new HashMap<String, String>();

	public static void main(String[] args) throws IOException {

		BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT));
		BufferedWriter nbw = new BufferedWriter(new FileWriter(NOOUTPUT));

		try (BufferedReader br = new BufferedReader(new FileReader(SOURCE))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				String data[] = line.split(",");
				String name = data[0].toLowerCase();
				String val = line.substring(name.length() + 1);
				nameMap.put(name, val);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// bw.write(
		// "inventor_first_name,inventor_last_name,inventor_country,inventor_id,rank,count,prop100k,cum_prop100k,pctwhite,pctblack,pctapi,pctaian,pct2prace,pcthispanic
		// \n");
		bw.write(
				"author_name_full,author_name_first,author_name_middle,author_name_last,author_url	,paper_name,paper_url,year_of_conference,rank,count,prop100k,cum_prop100k,pctwhite,pctblack,pctapi,pctaian,pct2prace,pcthispanic\n");
		// "application_number,filing_date,uspc_class,cpc4,inventor_name_first,inventor_name_middle,inventor_name_last,inventor_rank,inventor_region_code,inventor_country_code,"
		// +
		// "rank,count,prop100k,cum_prop100k,pctwhite,pctblack,pctapi,pctaian,pct2prace,pcthispanic
		// \n");

		try (BufferedReader br = new BufferedReader(new FileReader(DESTINATION))) {
			String line = null;
			int count = 0;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				String data[] = line.split(",");
				// if(data[7].equals("US")) { //Filter to take only US data.
				String name = data[LAST_NAME_INDEX].toLowerCase();
				String result = nameMap.get(name);
				if (result == null) {
					nbw.write(line + "," + result + "\n");
				} else {
					bw.write(line + "," + result + "\n");
				}
				// }
				System.out.println("Line: " + count++);

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
