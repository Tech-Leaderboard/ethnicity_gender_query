package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

//Multi threaded server call
//Producer consumer pattern

public class MultiThreadRestHandler {

	private static final int ID = 3;
	private static final String SOURCE = "/Users/pramod/Documents/GoogleDrive/EclipseWorkspace/DataScience/src/data/input.csv";
	private static final String DESTINATION = "/Users/pramod/Documents/GoogleDrive/EclipseWorkspace/DataScience/src/data/output.csv";

	private static final String INVALID = "/Users/pramod/Documents/GoogleDrive/EclipseWorkspace/DataScience/src/data/output_invalid.csv";
	private static final String URL_NAMEPRISM = "http://www.name-prism.com/api_token/eth/json/bc565ffdbb7740ac/Barack%20Obama";
	//http://www.name-prism.com/api_token/eth/json/bc565ffdbb7740ac/Barack%20Obama
	//http://www.name-prism.com/api_token/eth/json/bc565ffdbb7740ac
	static int count = 3;

	private static Map<String, String> duplicateMap = new HashMap<String, String>();

	private static Map<String, String> mapFailover = loadToMap(DESTINATION);

	static BufferedReader br = null;
	static BufferedWriter dest = null;
	static BufferedWriter nop = null;

	static {
		System.out.println(SOURCE);
		System.out.println(DESTINATION);
		try {
			br = new BufferedReader(new FileReader(SOURCE));
			dest = new BufferedWriter(new FileWriter(DESTINATION, true));
			nop = new BufferedWriter(new FileWriter(INVALID));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static BlockingQueue<Message> queue = new ArrayBlockingQueue<Message>(1000);

	public static void main(String[] args) {
		new Producer().start();

		Consumer[] consumes = new Consumer[count];
		for (int i = 0; i < count; i++) {
			Consumer consume = new Consumer(i);
			consume.start();
			consumes[i] = consume;
		}

		for (int i = 0; i < count; i++) {
			try {
				consumes[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			br.close();
			dest.close();
			nop.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static Map<String, String> loadToMap(String fileName) {
		Map<String, String> map = new HashMap<String, String>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				map.put(getLineKey(line), "1");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	static String getLineKey(String line) {
		String data[] = line.split(",");
		//String id = data[0].trim();
		String nameFirst = data[0].trim();
		String nameMiddle = data[1].trim();
		String nameLast = "";
		if (data.length > 2) {
			nameLast = data[2].trim();
		}

		return nameFirst + "_" + nameMiddle + "_" + nameLast;
	}

	static class Producer extends Thread {
		@Override
		public void run() {
			String line = null;
			try {
				br.readLine();
				while ((line = br.readLine()) != null) {
					try {
						// Skip already fetched lines.
						if (mapFailover.get(getLineKey(line)) != null) {
							continue;
						}
						String data[] = line.split(",");
						String nameFirst = data[0].trim();
						String nameMiddle = data[1].trim();
						String nameLast = "";
						if (data.length > 2) {
							nameLast = data[2].trim();
						}
						String name = getName(nameFirst, nameMiddle, nameLast);
						if (name != null) {
							queue.put(new Message(name, line));
							// System.out.println("Producing: " + count++);
						} else {
							System.out.println("ProdEro: " + line);
							nop.write(line + "\n");
						}
					} catch (Exception ex) {
						System.err.println(ex.getMessage() + " : " + line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		static String getName(String nameFirst, String nameMiddle, String nameLast) {
			if (nameFirst.length() < 2 && nameLast.length() < 2) {
				return null;
			}
			if (nameFirst.length() < 2) {
				return nameMiddle + " " + nameLast;
			}
			return nameFirst + " " + nameLast;
		}

	}

	static class Consumer extends Thread {

		int id;

		public Consumer(int id) {
			this.id = id;
		}

		@Override
		public void run() {
			int count = 0;
			while (true) {
				try {
					Message msg = queue.take();
					if (msg != null) {
						consume(msg);
						if (count++ % 20 == 0) {
							System.out.println("N" + ID + ":  C" + id + " : " + count++);
						}
					} else {
						System.out.println("Empty msg");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void consume(Message msg) {
			try {
				String url = msg.getUrl();
				String output = null;
				if ((output = duplicateMap.get(url)) != null) {
					//System.out.println("Duplicate url: " + url);
				} else {
					URL obj = new URL(url);
					HttpURLConnection con = (HttpURLConnection) obj.openConnection();
					con.setRequestMethod("GET");
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					StringBuffer response = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					output = response.toString();
					in.close();
				}
				if (output != null) {
					dest.write(msg.getLine() + "," + output + "\n");
					duplicateMap.put(url, output);
					// System.out.println(response.toString());
					dest.flush();
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println(msg.getLine() + ex.getMessage());
			}
		}

	}

	static class Message {
		String name;
		String line;

		Message(String name, String line) {
			this.name = name;
			this.line = line;
		}

		public String getName() {
			return name;
		}

		public String getUrl() throws Exception {
			if (name != null) {
				String encode = URLEncoder.encode(name, "UTF-8");
				encode = encode.replace("+", "%20");
				return URL_NAMEPRISM + encode;
			}
			return null;
		}

		public String getLine() {
			return line;
		}
	}
}
