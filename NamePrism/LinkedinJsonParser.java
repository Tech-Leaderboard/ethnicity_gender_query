package data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LinkedinJsonParser {
	static String JOB = "job";
	static String PROFILE = "profile";
	static String EDUCATION = "education";
	static String COMPANY = "company";
	static String NAME = "name";
	static String TITLE = "title";
	static String LOCATION = "location";
	static String TBD = "TBD";
	static String YES = "Yes";
	static String No = "No";
	static String dates = "dates";

	static String INPUTCOMPANY = "Checkr".toLowerCase();

	public static void main(String[] args) {

		String fileName = "/Users/pramod/Documents/AI/Checkr_old";
		String DESTINATION = "/Users/pramod/Documents/AI/Checkr.csv";
		String FILEHEADER = "Name,Gender,Ethnicity,Current Title,Current Job,Current Job = Checkr,Previous Job = Checr,Time at Checkr ,Universities Attended,First Degree Outside US,Degree Type";
		try {

			try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(DESTINATION)))) {
				String fileJson = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
				String json = "[" + fileJson + "]";
				JSONArray jarray = new JSONArray(json);
				bw.write(FILEHEADER.toString() + "\n");
				for (int i = 0; i < jarray.length(); i++) {
					JSONObject jobj = jarray.getJSONObject(i);
					StringBuilder builder = new StringBuilder(jobj.getJSONObject(PROFILE).getString((NAME)));
					builder.append(",").append(TBD); // Gender
					builder.append(",").append(TBD); // Ethnicity
					//builder.append(",").append(jobj.getJSONObject(PROFILE).getString(LOCATION));
					builder.append(getJobDetails(jobj));
					System.out.println(builder);
					bw.write(builder.toString() + "\n");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getJobDetails(JSONObject jobj) throws JSONException {
		StringBuilder output = new StringBuilder();
		JSONArray jobs = jobj.getJSONArray(JOB);
		//Current Job	Current Job = Checkr	Previous Job = Checr	Time at Checkr 	Universities Attended
		boolean isCurrentJobInputCompany = false;
		boolean isPreviousJobInputCompany = false;
		String timeAtInputCompany = null;
		String currentCompany = "";
		String currentTitle = "";

		for (int i = 0; i < jobs.length(); i++) {
			JSONObject job = jobs.getJSONObject(i);
			String company = job.getString(COMPANY);
			String title = job.getString(TITLE);
			if (company != null && !company.isEmpty()) {
				if (company.toLowerCase().contains(INPUTCOMPANY)) {
					String time = job.getString(dates);
					if (time.toLowerCase().contains("present")) {
						isCurrentJobInputCompany = true;
						currentCompany = company;
						currentTitle = title;
					} else {
						isPreviousJobInputCompany = true;
					}
					timeAtInputCompany = time;
				} else {
					String time = job.getString(dates);
					if (time.toLowerCase().contains("present")) {
						currentCompany = company;
						currentTitle = title;
					}
				}
			}
		}

		output.append(",").append(currentTitle.replaceAll(",", " "));
		output.append(",").append(currentCompany.replaceAll(",", " "));
		output.append(",").append(isCurrentJobInputCompany + "");
		output.append(",").append(isPreviousJobInputCompany + "");
		output.append(",").append(timeAtInputCompany);
		return output.toString();

	}

	public static String getEducationDetails(JSONObject jobj) throws Exception {
		StringBuilder output = new StringBuilder();
		JSONArray jobs = jobj.getJSONArray(JOB);
		//Current Job	Current Job = Checkr	Previous Job = Checr	Time at Checkr 	Universities Attended
		boolean isCurrentJobInputCompany = false;
		boolean isPreviousJobInputCompany = false;
		String timeAtInputCompany = null;
		String currentCompany = "";
		String currentTitle = "";

		return output.toString();
	}

}
