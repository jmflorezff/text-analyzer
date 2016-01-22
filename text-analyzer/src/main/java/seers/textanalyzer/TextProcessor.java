package seers.textanalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import seers.textanalyzer.entity.Sentence;
import seers.textanalyzer.entity.Token;

public class TextProcessor {

	private static StanfordCoreNLP pipeline;

	static {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
		pipeline = new StanfordCoreNLP(props);
	}

	private static final String[] PARENTHESIS = { "-LCB-", "-RCB-", "-LRB-", "-RRB-", "-LSB-", "-RSB-" };
	private static HashMap<String, String> POS_TAGS = new HashMap<String, String>();

	static {

		POS_TAGS.put("JJ", "JJ");
		POS_TAGS.put("JJR", "JJ");
		POS_TAGS.put("JJS", "JJ");

		POS_TAGS.put("NN", "NN");
		POS_TAGS.put("NNS", "NN");
		POS_TAGS.put("NNP", "NN");
		POS_TAGS.put("NNPS", "NN");

		POS_TAGS.put("PRP", "PRP");
		POS_TAGS.put("PRP$", "PRP");

		POS_TAGS.put("RB", "RB");
		POS_TAGS.put("RBR", "RB");
		POS_TAGS.put("RBS", "RB");

		POS_TAGS.put("VB", "VB");
		POS_TAGS.put("VBD", "VB");
		POS_TAGS.put("VBG", "VB");
		POS_TAGS.put("VBN", "VB");
		POS_TAGS.put("VBP", "VB");
		POS_TAGS.put("VBZ", "VB");

		POS_TAGS.put("WDT", "WH");
		POS_TAGS.put("WP", "WH");
		POS_TAGS.put("WP$", "WH");
		POS_TAGS.put("WRB", "WH");
	}

	private static String getGeneralPos(String pos) {
		String tag = POS_TAGS.get(pos);
		if (tag != null) {
			return tag;
		}
		return pos;
	}

	public static List<Sentence> processText(String text) {

		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		List<Sentence> parsedSentences = new ArrayList<>();
		Integer id = 0;

		for (CoreMap sentence : sentences) {
			Sentence parsedSentence = new Sentence(id.toString());

			List<CoreLabel> list = sentence.get(TokensAnnotation.class);

			for (CoreLabel token : list) {
				String word = token.get(TextAnnotation.class);

				if (isPunctuation(word)) {
					continue;
				}

				// if (isInteger(word)) {
				// continue;
				// }

				String lemma = token.get(LemmaAnnotation.class);
				String pos = token.get(PartOfSpeechAnnotation.class);
				String generalPos = getGeneralPos(pos);
				String stem = GeneralStemmer.stemmingPorter(word);

				Token parsedToken = new Token(word, generalPos, pos, lemma, stem);
				parsedSentence.addToken(parsedToken);
			}

			if (parsedSentence.isEmpty()) {
				continue;
			}

			parsedSentences.add(parsedSentence);
			id++;
		}

		return parsedSentences;

	}

	private static boolean isInteger(String token) {
		return token.matches("\\d+");
	}

	private static boolean isPunctuation(String token) {
		return token.matches("[\\p{P}\\p{S}]") || isParenthesis(token);
	}

	private static boolean isParenthesis(String token) {
		for (String parenthesis : PARENTHESIS) {
			if (token.contains(parenthesis)) {
				return true;
			}
		}
		return false;
	}
}
