package IR;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import opennlp.tools.stemmer.PorterStemmer;

public class IRSystem {
	private PorterStemmer stemmer = new PorterStemmer();

	// get list stopwords
	public static List<String> getStopWord(String path) {
		List<String> result = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
				result.add(line);
			}
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
		return result;
	}

	// get index by name file
	public static int getKeysByValue(HashMap<Integer, String> map, String value) {
		for (Entry<Integer, String> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return 0;
	}

	// get list index of file
	public static HashMap<Integer, String> indexDocuments() {
		File folder = new File("Cranfield");
		File[] listOfFiles = folder.listFiles();
		HashMap<Integer, String> indexDocuments = new HashMap<Integer, String>();
		for (int i = 0; i < listOfFiles.length; i++) {
			indexDocuments.put(
					Integer.parseInt(listOfFiles[i].getName().substring(0, listOfFiles[i].getName().length() - 4)),
					listOfFiles[i].getName());
		}
		return indexDocuments;
	}

	// idf all term
	public static HashMap<String, Float> idfDictionary(HashMap<String, HashMap<Integer, Integer>> dictionary) {
		HashMap<String, Float> idfDictionary = new HashMap<String, Float>();
		for (Entry<String, HashMap<Integer, Integer>> item : dictionary.entrySet()) {
			float idf = (float) (1.0 / (float) (item.getValue().size()));
			idfDictionary.put(item.getKey(), idf);
		}
		return idfDictionary;
	}

	// dictionary of all doc: term:{doc, tf}
	public static HashMap<String, HashMap<Integer, Integer>> buildDictionary(HashMap<Integer, String> indexDocuments,
			List<String> stopWords) {
		HashMap<String, HashMap<Integer, Integer>> dictionary = new HashMap<String, HashMap<Integer, Integer>>();
		File folder = new File("Cranfield");
		File[] listOfFiles = folder.listFiles();
		// dictionary
		for (int i = 0; i < listOfFiles.length; i++) {
			int indexOfFile = getKeysByValue(indexDocuments, listOfFiles[i].getName());
			try (BufferedReader br = Files.newBufferedReader(Paths.get("Cranfield\\" + listOfFiles[i].getName()))) {
				String line;
				while ((line = br.readLine()) != null) {
					line = line.replaceAll("[-|?|$|\\.|!|\"|,|(|)|/|_|\\'|`|*|+|@|#|%|^|&|[|]|{|}|;|:|<|>|]", " ");
					String[] words = line.split("\\s+");
					for (String word : words) {
						
						if (!stopWords.contains(word)) {
							if (dictionary.containsKey(word)) {
								if (dictionary.get(word).containsKey(indexOfFile)) {
									dictionary.get(word).replace(indexOfFile,
											dictionary.get(word).get(indexOfFile) + 1);
								} else {
									dictionary.get(word).put(indexOfFile, 1);
								}
							} else {
								HashMap<Integer, Integer> temp = new HashMap<Integer, Integer>();
								temp.put(indexOfFile, 1);
								dictionary.put(word, temp);
							}
						}
					}
				}
			} catch (IOException e) {
				System.err.format("IOException: %s%n", e);
			}
		}
		return dictionary;
	}

	// build indexing
	public static HashMap<String, Model> indexing(HashMap<String, HashMap<Integer, Integer>> dictionary,
			HashMap<String, Float> idfDictionary) {
		HashMap<String, Model> indexing = new HashMap<String, Model>();
		HashMap<Integer, Float> norm = new HashMap<Integer, Float>();
		for (Entry<String, HashMap<Integer, Integer>> item : dictionary.entrySet()) {
			for (Entry<Integer, Integer> lsFileF : item.getValue().entrySet()) {
				if (norm.containsKey(lsFileF.getKey())) {
					float temp = norm.get(lsFileF.getKey());
					norm.replace(lsFileF.getKey(),
							temp + (float) Math.pow(lsFileF.getValue() * idfDictionary.get(item.getKey()), 2));
				} else {
					norm.put(lsFileF.getKey(),
							(float) Math.pow(lsFileF.getValue() * idfDictionary.get(item.getKey()), 2));
				}
			}
		}
		for (Entry<String, HashMap<Integer, Integer>> item : dictionary.entrySet()) {
			Model m = new Model();
			int count = 0;
			for (Entry<Integer, Integer> subItem : item.getValue().entrySet()) {
				count += subItem.getValue();
			}
			m.setNumOfDocs(item.getValue().size());
			m.setIdf(idfDictionary.get(item.getKey()));
			m.setTf(count);
			HashMap<Integer, Posting> posting = new HashMap<Integer, Posting>();
			for (Entry<Integer, Integer> dic : dictionary.get(item.getKey()).entrySet()) {
				float weight = dic.getValue() * idfDictionary.get(item.getKey())
						/ (float) Math.sqrt(norm.get(dic.getKey()));
				posting.put(dic.getKey(), new Posting(dic.getValue(), weight));
			}
			m.setPostingList(posting);
			indexing.put(item.getKey(), m);
		}
		return indexing;

	}

	public static void calculateNorm(HashMap<String, Float> idfDictionary,
			HashMap<String, HashMap<Integer, Integer>> dictionary, HashMap<Integer, String> indexDocuments) {
		HashMap<Integer, Float> norm = new HashMap<Integer, Float>();
		for (Entry<String, HashMap<Integer, Integer>> item : dictionary.entrySet()) {
			for (Entry<Integer, Integer> lsFileF : item.getValue().entrySet()) {
				if (norm.containsKey(lsFileF.getKey())) {
					float temp = lsFileF.getValue();

					norm.replace(lsFileF.getKey(),
							temp + (float) Math.pow(lsFileF.getValue() * idfDictionary.get(item.getKey()), 2));
				} else {
					norm.put(lsFileF.getKey(),
							(float) Math.pow(lsFileF.getValue() * idfDictionary.get(item.getKey()), 2));
				}
			}
		}
	}

	// dictionary of query
	public static HashMap<String, Float> query(HashMap<String, Float> idfDictionary, String query,
			List<String> stopWords) {
		HashMap<String, Integer> queryDic = new HashMap<String, Integer>();
		HashMap<String, Float> result = new HashMap<String, Float>();
		query = query.replaceAll("[-|?|$|\\.|!|\"|,|(|)|/|_|\\'|`|*|+|@|#|%|^|&|[|]|{|}|;|:|<|>|]", " ");
		String[] words = query.split("\\s+");
		Float norm = (float) 0;
		for (String word : words) {
			if (!stopWords.contains(word)) {
				if (queryDic.containsKey(word)) {
					queryDic.replace(word, queryDic.get(word) + 1);
				} else {
					queryDic.put(word, 1);
				}
			}
		}
		for (Entry<String, Integer> item : queryDic.entrySet()) {
			if (idfDictionary.containsKey(item.getKey())) {
				norm += (float) Math.pow(idfDictionary.get(item.getKey()) * item.getValue(), 2);
			}
		}
		for (Entry<String, Integer> item : queryDic.entrySet()) {
			if (idfDictionary.containsKey(item.getKey())) {
				float w = (float) (queryDic.get(item.getKey()) * idfDictionary.get(item.getKey()) / Math.sqrt(norm));
				result.put(item.getKey(), w);
			}
		}
		return result;
	}

	public static HashMap<Integer, Float> similarities(HashMap<String, Model> indexing, HashMap<String, Float> query) {
		HashMap<Integer, Float> result = new HashMap<Integer, Float>();
		for (Entry<String, Float> item : query.entrySet()) {
			HashMap<Integer, Posting> postingList = indexing.get(item.getKey()).getPostingList();
			for (Entry<Integer, Posting> i : postingList.entrySet()) {
				if (result.containsKey(i.getKey())) {
					float temp = 0;
					temp = result.get(i.getKey());
					result.replace(i.getKey(),temp + item.getValue() * i.getValue().getW());
				} else {
					result.put(i.getKey(), item.getValue() * i.getValue().getW());
				}
			}
		}
		return result;
	}

	// sort similarities descending
	public static HashMap<Integer, Float> sort(HashMap<Integer, Float> simi) {
		HashMap<Integer, Float> result = new HashMap<Integer, Float>();
		List<Entry<Integer, Float>> listEntry = new ArrayList<Map.Entry<Integer, Float>>();
		for (Entry<Integer, Float> item : simi.entrySet()) {
			listEntry.add(item);
		}
		Set<Entry<Integer, Float>> set = simi.entrySet();
		List<Entry<Integer, Float>> list = new ArrayList<Entry<Integer, Float>>(set);
		Collections.sort(list, new Comparator<Map.Entry<Integer, Float>>() {
			@Override
			public int compare(Entry<Integer, Float> arg0, Entry<Integer, Float> arg1) {
				return (arg1.getValue()).compareTo(arg0.getValue());
			}
		});
		for (Map.Entry<Integer, Float> entry : list) {
			System.out.println(entry.getKey() + " =========== " + entry.getValue());
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static List<Integer> getRes(int idQuery, HashMap<Integer, String> indexDocuments) {
		File folder = new File("RES");
		File[] listOfFiles = folder.listFiles();
		List<Integer> result = new ArrayList<Integer>();
//		if(indexDocuments.get(idQuery) != null) {
//			System.out.println("null " + idQuery);
//		}
		try (BufferedReader br = Files.newBufferedReader(Paths.get("RES\\" + indexDocuments.get(idQuery)))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] words = line.split("\\s+");
				result.add(Integer.parseInt(words[1]));
			}
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
		return result;
	}

	public static int getQuery(String q) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = Files.newBufferedReader(Paths.get("query.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains(q)) {
					String[] words = line.split("\\s+");
					return Integer.parseInt(words[0]);
				}
			}
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
		return 0;
	}

	public static List<String> getListQuery() {
		List<String> result = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = Files.newBufferedReader(Paths.get("query.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] words = line.split("\\s+");
				if (words.length != 1) {
					int wordTemp = 0;
					wordTemp = line.indexOf(words[1].substring(0, 1));
					String temp = line.substring(wordTemp);
					result.add(temp);
				}
			}
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
		return result;
	}

	public static Evaluation evaluation(HashMap<Integer, Float> similarities, List<Integer> result) {
		Evaluation res = new Evaluation();
		int countRelevantDoc = 0;
		for (Entry<Integer, Float> item : similarities.entrySet()) {
			if (result.contains(item.getKey())) {
				countRelevantDoc++;
			}
		}
		float r = (float) countRelevantDoc / result.size();
		float p = (float) countRelevantDoc / similarities.size();
		res.setRecall(r);
		res.setPrecision(p);
		return res;
	}

	public static void main(String[] args) {
//		List<String> listQuery = new ArrayList<String>();
//		listQuery =	getListQuery();
//		List<String> stopWords = getStopWord("stopwords.txt");
//		HashMap<Integer, String> indexDocuments = indexDocuments();
//		HashMap<String, HashMap<Integer, Integer>> dictionary = buildDictionary(indexDocuments, stopWords);
//		HashMap<String, Float> idfDictionary = idfDictionary(dictionary);
//		HashMap<String, Model> indexing = indexing(dictionary, idfDictionary);
//		List<Evaluation> lsEvaluation = new ArrayList<Evaluation>();
//		for(String q:listQuery) {
//			String query = q;
//			int indexQuery = 0;
//			indexQuery = getQuery(query);
//			if(indexQuery != 0) {
//				List<Integer> result = getRes(indexQuery, indexDocuments);
//				HashMap<String, Float> qr = query(idfDictionary, query, stopWords);
//				HashMap<Integer, Float> similarities = similarities(indexing, qr);
//				HashMap<Integer, Float> sortedSimilarities = sort(similarities);
//				Evaluation evaluation = new Evaluation();
//				evaluation = evaluation(sortedSimilarities, result);
//				lsEvaluation.add(evaluation);
//			}
//			else {
//				System.out.println(query +"\n");
//			}
//		}
//		float totalPrecission = 0;
//		for(Evaluation item : lsEvaluation) {
//			totalPrecission+= item.getPrecision();
//		}
//		System.out.println(totalPrecission/lsEvaluation.size());

		String query = "what similarity laws must be obeyed when constructing aeroelastic models of heated high speed aircraft";
		int indexQuery = 0;
		List<String> stopWords = getStopWord("stopwords.txt");
		HashMap<Integer, String> indexDocuments = indexDocuments();
		indexQuery = getQuery(query);
		List<Integer> result = getRes(indexQuery, indexDocuments);
		HashMap<String, HashMap<Integer, Integer>> dictionary = buildDictionary(indexDocuments, stopWords);
		HashMap<String, Float> idfDictionary = idfDictionary(dictionary);
		HashMap<String, Model> indexing = indexing(dictionary, idfDictionary);
		HashMap<String, Float> qr = query(idfDictionary, query, stopWords);
		HashMap<Integer, Float> similarities = similarities(indexing, qr);
		HashMap<Integer, Float> sortedSimilarities = sort(similarities);
		Evaluation evaluation = evaluation(similarities, result);
		System.out.println(sortedSimilarities.size());
		System.out.println("List file relevant: ");
		System.out.println(sortedSimilarities.keySet());
		System.out.println("Recal = " + evaluation.getRecall());
		System.out.println("Precission = " + evaluation.getPrecision());
//		for( Entry<String, Model> item:indexing.entrySet()) {
//			System.out.println(item.getKey());
//			System.out.println(item.getValue().getIdf());
//			System.out.println(item.getValue().getIdf());
//			System.out.println(item.getValue().getNumOfDocs());
//			for( Entry<Integer, Posting> i: item.getValue().getPostingList().entrySet()) {
//				System.out.println("TF :" + i.getValue().getTf());
//				System.out.println("W :" + i.getValue().getW());
//			}
//			System.out.println("===========");
//		}
	}

}