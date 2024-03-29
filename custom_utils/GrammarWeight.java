package custom_utils;

import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;

import custom_utils.FileRead.*;

public class GrammarWeight {
  void Parameters() {
    idfConceptFact = new Double(2);
    idfFreeWords = new Double(1);

    tfConceptFact = new Double(2);
    tfFreeWords = new Double(1);

  }

  Double idfConceptFact = new Double(100);
  Double idfFreeWords = new Double(1);
  Double tfConceptFact = new Double(10);
  Double tfFreeWords = new Double(1);
  HashMap<String, HashMap<String, HashMap<String, Double>>> datadict = new HashMap<String, HashMap<String, HashMap<String, Double>>>();
  HashMap<String, HashMap<String, Double>> tf = new HashMap<String, HashMap<String, Double>>();
  HashMap<String, HashMap<String, String>> symbol = new HashMap<String, HashMap<String, String>>();
  HashMap<String, Double> idf = new HashMap<String, Double>();
  ArrayList<String> fileList = new ArrayList<>();

  HashMap<String, Double> calculateTF(ArrayList<String> words) {
    HashMap<String, Double> map = new HashMap<>();
    for (String w : words) {
      Double n = map.get(w);
      n = (n == null) ? new Double(1) : ++n;
      map.put(w.toLowerCase(), n);
    }
    DateTimePlaceholder dt = new DateTimePlaceholder();
    for (String w : words) {

      Double n = map.get(w) / new Double(words.size());
      String pattern = ".*\\{\\w+\\}|\\<\\w+\\>.*";
      if (dt.isPattern(pattern, w)) {
        n *= tfConceptFact;
      } else {
        n *= tfFreeWords;
      }
      map.put(w, n);
    }
    return map;
  }

  HashMap<String, String> calculateSymbol(ArrayList<String> words) {
    Set<String> unqWords = new HashSet<String>(words);
    HashMap<String, String> sym = new HashMap<>();
    int count = new Integer(65);
    for (String w : unqWords) {
      sym.put(w.toLowerCase(), String.valueOf(Character.toChars(count)));
      count++;
    }
    return sym;
  }

  void calculateIDF(ArrayList<String> al) {
    Set<String> wordSet = new HashSet<String>(al);
    for (String w : wordSet) {
      Double n = idf.get(w);
      n = (n == null) ? new Double(1) : ++n;
      idf.put(w.toLowerCase(), n);
    }
  }

  public GrammarWeight() throws Exception {
    Parameters();
    // System.out.println("Grammar Constructor");
    String DIR = "./resources/";
    // File file = new File("./resources/Concept/what_concept.txt");
    FileRead fr = new FileRead();
    fileList = fr.getFileList(new File(DIR + "Grammar/"));

    // System.out.println("Calculating TF");
    // Calculate TF
    for (String filename : fileList) {
      ArrayList<String> al = fr.readFileAsWord(new File(DIR + "Grammar/" + filename));
      tf.put(filename.split("\\.")[0], calculateTF(al));
    }

    // Calculate IDF
    for (String filename : fileList) {
      ArrayList<String> al = fr.readFileAsWord(new File(DIR + "Grammar/" + filename));
      calculateIDF(al);
    }
    // log(N/count)
    DateTimePlaceholder dt = new DateTimePlaceholder();
    for (String key : idf.keySet()) {
      String pattern = ".*\\{\\w+\\}|\\<\\w+\\>.*";
      Double x = new Double(0);
      if (dt.isPattern(pattern, key)) {
        // System.out.println(key);
        x = -Math.log(idf.get(key) / new Double(fileList.size()));
        x *= idfConceptFact;
      } else {
        x = -Math.log(idf.get(key) / new Double(fileList.size()));
        x *= idfFreeWords;
        // x = new Double(0.00000001);
      }
      // x = -Math.log(idf.get(key) / new Double(fileList.size()));
      idf.put(key, x);
    }

    // generate unique symbol for each word of every document
    for (String key : fileList) {
      ArrayList<String> al = fr.readFileAsWord(new File(DIR + "Grammar/" + key));
      symbol.put(key.split("\\.")[0], calculateSymbol(al));
    }
  }

  public HashMap<String, HashMap<String, Double>> getTF() {
    return tf;
  }

  public HashMap<String, Double> getIDF() {
    return idf;
  }

  public HashMap<String, HashMap<String, String>> getSymbol() {
    return symbol;
  }

}
