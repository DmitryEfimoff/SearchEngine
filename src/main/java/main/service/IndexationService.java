package main.service;

import main.engines.LemmatizationEngine;
import main.model.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class IndexationService {

    public boolean pagesIndexed(List<Page> pages){

        for (Page page : pages) {
            return true;
        }
        return false;
    }

    public Site siteIsFound(List<Site> sites, String url, int siteId) {

        Site actualSite = null;

        boolean siteIsFound = false;
        for (Site site : sites) {
            if (site.getUrl().equals(url) && (site.getId().equals(siteId))) {
                siteIsFound = true;
                site.setStatus(Status.INDEXING.toString());
                site.setStatusTime(new java.util.Date(new java.util.Date().getTime()));

                actualSite = site;
            }
        }
        if (!siteIsFound) {
            Site newSite = new Site();
            newSite.setId(siteId);
            newSite.setUrl(url);
            newSite.setName(url);
            newSite.setStatus(Status.INDEXING.toString());
            newSite.setStatusTime(new java.util.Date(new java.util.Date().getTime()));

            actualSite = newSite;
        }

        return actualSite;
    }

    public boolean siteIsIndexed(List<Page> pages, int siteId){

        for (Page page : pages) {
            if (page.getSiteId().equals(siteId)) {
                return true;
            }
        }

        return false;
    }

    public List<Lemma> existingLemmas(List<Lemma> lemmaRep, int siteId){

        List<Lemma> lemmas = new ArrayList<>();
        for (Lemma lemma : lemmaRep) {
            if (lemma.getSiteId() == siteId) {
                lemma.setFrequency(0F);
                lemmas.add(lemma);
            }
        }
        return lemmas;
    }

    public List<Index> existingIndexes(List<Index> indexRep, int siteId){

        List<Index> indexes = new ArrayList<>();
        for (Index index : indexRep) {
            if (index.getSiteId() == siteId) {
                index.setRanke(0F);
                indexes.add(index);
            }
        }
        return indexes;
    }

    public HashMap<String,Float> convertedWords(Page resultPage, List<Field> fieldRep){

        HashMap<String,Float> convertedWords = new HashMap<>();
        LemmatizationEngine lemmatization = new LemmatizationEngine();

        for (Field field : fieldRep) {

            String query = field.getName();
            Elements dTable = Jsoup.parse(resultPage.getContent()).select(query);

            for (Element tbl: dTable) {
                String txt = tbl.text();
                String[] words = txt.split("\\s");
                HashMap<String,Integer> receivedWords = lemmatization.wordLemmas(words);

                for (String word : receivedWords.keySet()) {
                    if (convertedWords.containsKey(word)) {
                        convertedWords.put(word, convertedWords.get(word) + receivedWords.get(word)* field.getWeight());
                    } else {
                        convertedWords.put(word, receivedWords.get(word)* field.getWeight());
                    }
                }

            }

        }

        return convertedWords;
    }

    public Lemma lemmaDefined(String word, List<Lemma> lemmas, int siteId) {

        if (!lemmas.equals(null)){
            for (Lemma lemma : lemmas) {
                if (lemma.getLemma().equals(word)) {
                    lemma.setFrequency(lemma.getFrequency() + 1F);
                    return lemma;
                }
            }
        }

        Lemma lemma = new Lemma();
        lemma.setLemma(word);
        lemma.setFrequency(1F);
        lemma.setSiteId(siteId);

        return lemma;
    }

    public Index indexDefined(List<Index> indexes, Page resultPage, Integer lemmaId, Float ranke, int siteId){

        for (Index index : indexes) {
            if ((index.getPageId() == resultPage.getId()) && (index.getLemmaId() == lemmaId)) {
                index.setRanke(ranke);
                return index;
            }
        }

        Index index = new Index();
        index.setPageId(resultPage.getId());
        index.setSiteId(siteId);
        index.setLemmaId(lemmaId);
        index.setRanke(ranke);

        return index;
    }

    public Site siteStatusUpdate(Site site) {

        site.setStatus(Status.INDEXED.toString());
        site.setStatusTime(new java.util.Date(new java.util.Date().getTime()));

        return site;
    }

}
