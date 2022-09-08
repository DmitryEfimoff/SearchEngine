package main;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Lemmatization {

    private String rusWordRegex = "[А-я]+";

    public HashMap<String, Integer> wordLemmas(String[] words) {

        HashMap<String,Integer> convertedWords = new HashMap<>();

        Arrays.stream(words).forEach(w -> {
            String word = w.toLowerCase();
            if (word.matches(rusWordRegex)) {

                LuceneMorphology luceneMorph = null;
                try {
                    luceneMorph = new RussianLuceneMorphology();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                boolean union = false;

                List<String> wordBaseForms = luceneMorph.getMorphInfo(word);
                for (String wbf : wordBaseForms){
                    String[] parts = wbf.split("\\s");
                    if (parts[parts.length-1].equals("МЕЖД")) { continue; }
                    if (parts[parts.length-1].equals("ПРЕДЛ")) { continue; }
                    if (parts[parts.length-1].equals("ЧАСТ")) { continue; }
                    String readyWord = parts[0].substring(0,parts[0].indexOf('|'));

                    if (convertedWords.containsKey(readyWord)) {
                        convertedWords.put(readyWord, convertedWords.get(readyWord) + 1);
                    } else {
                        convertedWords.put(readyWord, 1);
                    }
                }

            }
        });

        return convertedWords;
    }

    public String snippet(String lemma, String context) {

        String[] words = Jsoup.parse(context).text().split("\\s");

        int position = 0;

        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            if (word.matches(rusWordRegex)) {

                LuceneMorphology luceneMorph = null;
                try {
                    luceneMorph = new RussianLuceneMorphology();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                boolean union = false;

                List<String> wordBaseForms = luceneMorph.getMorphInfo(word);
                for (String wbf : wordBaseForms){
                    String[] parts = wbf.split("\\s");
                    if (parts[parts.length-1].equals("МЕЖД")) { continue; }
                    if (parts[parts.length-1].equals("ПРЕДЛ")) { continue; }
                    if (parts[parts.length-1].equals("ЧАСТ")) { continue; }

                    String readyWord = parts[0].substring(0,parts[0].indexOf('|'));

                    if (lemma.equals(readyWord)) {
                        position = i;
                        break;
                    }
                }

            }
        }

        String snippet = "<b> ";
        for (int i = ((position < 10) ? 0 : position - 10); i < ((position > words.length - 10) ? words.length : position + 10); i++) {
            snippet = snippet + words[i] + " ";
        }
        snippet = snippet + "</b>";

        return snippet;
    }

}
