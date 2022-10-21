package main.service;

import main.controller.DefaultController;
import main.engines.LemmatizationEngine;
import main.model.*;
import main.responses.WebSearchAnswer;
import main.responses.WebSearchRequest;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

@Service
public class SearchService {

    private static String firstLemma = new String();

    public String getFirstLemma() { return firstLemma;}

    public List<String> urlListCheck(List<String> urlList) {

        if (urlList.isEmpty()) {
            SiteList.getUrls().forEach(u -> urlList.add(u));
        }
        return urlList;
    }

    public WebSearchRequest requestDefaultsCheck(WebSearchRequest request) {

        if (request.getOffset() == null) { request.setOffset(DefaultController.getOffsetDefault()); }
        if (request.getLimit() == null) { request.setLimit(DefaultController.getLimitDefault()); }

        return request;
    }

    public boolean existingSiteCheck(List<String> urlList, WebSearchRequest request) {

        if ((request.getSite() == null) || urlList.contains(request.getSite())) { return true; }

        return false;

    }

    public boolean indexedSiteCheck(WebSearchRequest request, List<Site> sites) {

        // Method returns TRUE if at least one site from the request list is indexed
//         NOTE: request.getSite() = null means ALL sites are requested for search.

        if (request.getSite() == null) {
            for (Site site : sites) {
                if (site.getStatus().equals(Status.INDEXED.toString())) {
                    return true;
                }
            }
        } else {
            for (Site site : sites) {
                if (site.getUrl().equals(request.getSite()) && site.getStatus().equals(Status.INDEXED.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<String> urlSearchListSet(WebSearchRequest request, List<String> urlList) {

        List<String> urlSearchList = new ArrayList<>();
        if (request.getSite() == null) {
            urlSearchList = urlList;
        } else {
            urlSearchList.add(request.getSite());
        }

        return  urlSearchList;
    }

    public boolean siteIsIndexed(String url, List<Site> sites) {

        for (Site site : sites) {
            if ((site.getStatus().equals(Status.INDEXED.toString())) && (site.getUrl().equals(url))) { return true; }
        }
        return false;
    }

    public TreeMap<Float, List<Integer>> sortedRequest(HashMap<String, Integer> requestWords, List<Lemma> lemmas, int siteId){

        TreeMap<Float, List<Integer>> sortedRequest = new TreeMap<>();

        for (String word : requestWords.keySet()) {
            for (Lemma lemma : lemmas) {
                if (lemma.getSiteId() != siteId) { continue; }
                if (lemma.getLemma().equals(word)) {
                    if (sortedRequest.containsKey(lemma.getFrequency())) {
                        List<Integer> newList = sortedRequest.get(lemma.getFrequency());
                        newList.add(lemma.getId());
                        sortedRequest.put(lemma.getFrequency(), newList);
                    } else {
                        List<Integer> newList = new ArrayList<>();
                        newList.add(lemma.getId());
                        sortedRequest.put(lemma.getFrequency(), newList);
                    }
                }
            }
        }

        return sortedRequest;

    }

//    public String firstLemma(TreeMap<Float, List<Integer>> sortedRequest, List<Lemma> lemmaRep){
//
//        String firstLemma = new String();
//
//        for (Float frq : sortedRequest.keySet()) {
//            for (Integer lemmaId : sortedRequest.get(frq)) {
//                if (firstLemma.isEmpty()) {
//                    for (Lemma lemma : lemmaRep) {
//                        if (lemmaId == lemma.getId()) {
//                            firstLemma = lemma.getLemma();
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//
//        return  firstLemma;
//    }


    public HashMap<Integer, HashMap<Integer,Float>> foundPagesSearch(TreeMap<Float, List<Integer>> sortedRequest, List<Lemma> lemmaRep, List<Index> indexRep, int siteId){

        List<Integer> resultPages = new ArrayList<>();
        HashMap<Integer, HashMap<Integer,Float>> foundPages = new HashMap<>();

        boolean isStartElement = true;

        for (Float frq : sortedRequest.keySet()) {
            for (Integer lemmaId : sortedRequest.get(frq)) {

                if (isStartElement) {

                    if (firstLemma.isEmpty()) {
                        for (Lemma lemma : lemmaRep) {
                            if (lemmaId == lemma.getId()) {
                                firstLemma = lemma.getLemma();
                                break;
                            }
                        }
                    }

                    for (Index index : indexRep) {
                        if (!index.getSiteId().equals(siteId)) { continue; }
                        if (index.getLemmaId().equals(lemmaId)) {
                            HashMap<Integer,Float> lemmaWeight = new HashMap<>();
                            lemmaWeight.put(lemmaId, index.getRanke());
                            foundPages.put(index.getPageId(), lemmaWeight);
                            resultPages.add(index.getPageId());
                        }
                    }

                } else {
                    List<Integer> reducedResultPages = new ArrayList<>();
                    for (Index index : indexRep) {

                        if ((index.getLemmaId().equals(lemmaId)) && (resultPages.contains(index.getPageId()))) {
                            HashMap<Integer,Float> lemmaWeight = foundPages.get(index.getPageId());
                            lemmaWeight.put(lemmaId, index.getRanke());
                            foundPages.put(index.getPageId(), lemmaWeight);
                            reducedResultPages.add(index.getPageId());
                        }
                    }
                    for (Integer pageId : resultPages) {
                        if (!reducedResultPages.contains(pageId)) {
                            foundPages.remove(pageId);
                        }
                    }
                    resultPages = reducedResultPages;
                }

            }
            isStartElement = false;

        }

        return foundPages;

    }


    public HashMap<Integer, Float> absRelCalculation(HashMap<Integer, HashMap<Integer,Float>> foundPages, List<Page> pagesRep, int siteId) {


        HashMap<Integer, Float> absRel = new HashMap<>();

        for (Integer pageId : foundPages.keySet()) {
            absRel.put(pageId, 0F);
            HashMap<Integer,Float> lemmas = foundPages.get(pageId);
            for (Integer lemma : lemmas.keySet()) {
                absRel.put(pageId, absRel.get(pageId) + lemmas.get(lemma));
            }
        }

        return absRel;

    }

    public TreeMap<Integer, Page> pagesCollector(List<Page> pagesRep, HashMap<Integer, Float> absRel, int siteId) {

        TreeMap<Integer, Page> pages = new TreeMap<>();
        for (Page page : pagesRep) {
            if (!page.getSiteId().equals(siteId) && (absRel.containsKey(page.getId()))) { continue; }
            pages.put(page.getId(), page);
        }

        return pages;
    }

    public Float maxAbsRel(HashMap<Integer, Float> absRel){
        Float maxAbsRel = 0F;

        for (Integer pageId : absRel.keySet()) {
            if (maxAbsRel < absRel.get(pageId)) { maxAbsRel = absRel.get(pageId); }
        }

        return maxAbsRel;
    }

    public HashMap<Integer, Float> relRel(HashMap<Integer, Float> absRel){

        HashMap<Integer, Float> relRel = new HashMap<>();

        Float maxVal = maxAbsRel(absRel);

        for (Integer pageId : absRel.keySet()) {
            relRel.put(pageId,absRel.get(pageId)/maxVal);
        }

        return relRel;
    }

    public WebSearchAnswer searchThreadsStarter(WebSearchAnswer answer, WebSearchRequest request, HashMap<Integer, Float> absRel, HashMap<Integer, Float> relRel, TreeMap<Integer, Page> pages, String url){

        int counter = (answer.getCount() == null) ? 0 : answer.getCount();
        List<WebSearchAnswer.WebSearchData> dataList = (answer.getData() == null) ? new ArrayList<>() : answer.getData();

        List<LemmatizationEngine> lems = new ArrayList<>();

        for (Integer pageId : absRel.keySet()) {

            counter++;

            Page page = pages.get(pageId);

            String[] line = new String[5];
            line[0] = (url.charAt(url.length()-1) == '/' ? page.getPath().substring(1) : page.getPath());

            line[3] = relRel.get(pageId).toString();
            line[4] = url;
            if ((counter > request.getOffset()) && (counter <= (request.getOffset()) + request.getLimit())) {

//                Logger.getLogger(SearchService.class.getName()).info(line[0]);
//                System.out.println(line[0]);

                LemmatizationEngine lmt = new LemmatizationEngine(getFirstLemma(), page.getContent(), line[0], counter);
                lmt.start();
                Logger.getLogger(SearchService.class.getName()).info("Thread #" + counter + " is started for page " + line[0]);
//                System.out.println("Thread " + counter + " is started!");
                lems.add(lmt);

                WebSearchAnswer.WebSearchData newData = new WebSearchAnswer.WebSearchData();
                newData.setSite(line[4]);
                newData.setSiteName(line[4]);
                newData.setUri(line[0]);
                newData.setRelevance(Float.parseFloat(line[3]));
                dataList.add(newData);

            } else {
                line[1] = "N/A - out of offset and limit";
                line[2] = "N/A - out of offset and limit";
            }

        }

        boolean threadsDone = false;
        while (!threadsDone) {
            int cnt = 0;
            int done = 0;
            for (LemmatizationEngine lem : lems) {
                cnt++;
                if ((lem.getTitle() != null) && (lem.getSnip() != null)) { done++; }
            }
            if (cnt == done) {
                threadsDone = true;

                Logger.getLogger(SearchService.class.getName()).info("Threads are done!!!");
//                System.out.println("Threads are done!!!");
            }
        }

        for (WebSearchAnswer.WebSearchData data : dataList) {

            for (LemmatizationEngine lm : lems) {
                if (lm.getUri().equals(data.getUri())) {
                    data.setTitle(lm.getTitle());
                    data.setSnippet(lm.getSnip());
                }
            }
            Logger.getLogger(SearchService.class.getName()).info(data.getTitle() + " - " + data.getUri());
//            System.out.println(data.getTitle() + " - " + data.getUri());
        }

        answer.setResult(true);
        answer.setCount(counter);
        answer.setData(dataList);

        return answer;
    }

}
