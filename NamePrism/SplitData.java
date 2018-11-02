package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;

public class SplitData {

	static String input = "/Users/pramod/Documents/GoogleDrive/AI/Lexus/inputurls.txt";

	public static void main(String[] args) throws FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(new File(input)))) {
			String line = null;
			int count = 0;
			while ((line = br.readLine()) != null) {
				String url = line.split("url=")[1];
				String decode = URLDecoder.decode(url);
				if(decode.contains("ptab")) {
					System.out.println(decode);
				}
				count++;
			}
			System.out.println(count);
		}
	}
}
