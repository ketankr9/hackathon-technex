package custom_utils;

import java.util.*;

import custom_utils.LevenshteinDistance;

import java.lang.*;
import java.io.*;

class FileSystemUtility {
	public ArrayList<String> generateFileList(String path) {
		ArrayList<String> files = new ArrayList<String>();
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles != null) {
			for (File file : listOfFiles) {
				if (file.isFile()) {
					files.add(file.getName());
				}
			}
		}
		return files;
	}
}

class GrammarEncoder {
	HashMap<String, HashMap<String, Double>> grammarWeightList_tf;
	HashMap<String, HashMap<String, String>> grammarSymbolList_tf;
	HashMap<String, Double> grammarWeightList_idf;

	GrammarEncoder() {
		try {
			GrammarWeight gw = new GrammarWeight();
			this.grammarWeightList_tf = gw.getTF();
			this.grammarSymbolList_tf = gw.getSymbol();
			this.grammarWeightList_idf = gw.getIDF();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public String encode(String grammar, String[] tokenArray) {
		StringBuilder code = new StringBuilder();
		for (String token : tokenArray) {
			if (grammarWeightList_tf.get(grammar).get(token) != null) {
				Double tf = grammarWeightList_tf.get(grammar).get(token);
				Double idf = grammarWeightList_idf.get(token);
				String symbol = grammarSymbolList_tf.get(grammar).get(token);

				for (int i = 0; i < Math.ceil(tf * idf); i++) {
					code.append(symbol);
				}
			}
		}

		return code.toString();
	}
}

public class GrammarAnalyser {
	private String __RESOUCSE_PATH__ = "./resources/";
	private FileSystemUtility fileSystemUtility = new FileSystemUtility();
	private ArrayList<String> fileList;
	HashMap<String, ArrayList<String>> encodedGrammarList;
	GrammarEncoder grammarEncoder;

	public GrammarAnalyser() {
		// list all files in concept folder
		this.fileList = fileSystemUtility.generateFileList(__RESOUCSE_PATH__ + "Grammar");
		this.encodedGrammarList = new HashMap<>();

		// initializing the encoder object
		this.grammarEncoder = new GrammarEncoder();

		for (String file : this.fileList) {
			// grammar filename vs list of encoded grammar
			String grammar = file.split("\\.")[0];

			ArrayList<String> tempList = new ArrayList<>();

			File fileOject = new File(__RESOUCSE_PATH__ + "Grammar/" + file);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(fileOject));
				String line = reader.readLine();
				while (line != null) {
					String[] tokenArray = line.split("\\s+");
					tempList.add(this.grammarEncoder.encode(grammar, tokenArray));
					line = reader.readLine();
				}
				reader.close();
			} catch (Exception e) {
				System.out.println("Error reported" + e);
			}

			this.encodedGrammarList.put(grammar, tempList);
		}
	}

	public String findGrammer(String sentense) {
		String[] tokenArray = sentense.split("\\s+");

		// the code per grammar for the sentence provided by the user
		HashMap<String, String> sentenseEncodedGrammar = new HashMap<>();
		for (String file : this.fileList) {
			String grammar = file.split("\\.")[0];
			sentenseEncodedGrammar.put(grammar, this.grammarEncoder.encode(grammar, tokenArray));
		}

		// find optimum match
		int globalMin = Integer.MAX_VALUE;
		String globalMinFile = "";
		for (String grammar : this.encodedGrammarList.keySet()) {
			String token_code = sentenseEncodedGrammar.get(grammar);
			int localMin = Integer.MAX_VALUE;
			for (String code : this.encodedGrammarList.get(grammar)) {
				int dist = LevenshteinDistance.find_distance(token_code, code);
				if (localMin > dist) {
					localMin = dist;
				}
			}

			if (localMin < globalMin) {
				globalMin = localMin;
				globalMinFile = grammar;
			}
			localMin = Integer.MAX_VALUE;
		}

		// System.out.println(sentenseEncodedGrammar);
		return globalMinFile;
	}
}