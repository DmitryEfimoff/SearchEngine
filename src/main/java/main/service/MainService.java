package main.service;
import main.engines.LemmatizationEngine;
import main.engines.SearchEngine;
import main.model.*;
import main.responses.WebSearchAnswer;
import main.responses.WebSearchRequest;
import main.responses.WebStatisticsAnswer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

@Service
public class MainService {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private SearchService searchService;


    private LemmatizationEngine lemmatization = new LemmatizationEngine();


    public WebSearchAnswer searchProcess(WebSearchRequest request, List<String> urlList){

        Logger.getLogger(MainService.class.getName()).info("Search method started for request: " + request.getQuery());

        WebSearchAnswer answer = new WebSearchAnswer();

        List<Site> sitesRep = (List<Site>) siteRepository.findAll();
        List<Lemma> lemmasRep = (List<Lemma>) lemmaRepository.findAll();
        List<Index> indexRep = (List<Index>) indexRepository.findAll();
        List<Page> pagesRep = (List<Page>) pageRepository.findAll();

        urlList = searchService.urlListCheck(urlList);
        request = searchService.requestDefaultsCheck(request);

        boolean isEmptyQuery = request.getQuery().isBlank();
        boolean existingSite = searchService.existingSiteCheck(urlList, request);
        boolean isIndexedSite = searchService.indexedSiteCheck(request, sitesRep);


        if (!isEmptyQuery && existingSite && isIndexedSite) {

            HashMap<String, Integer> requestWords = lemmatization.wordLemmas(request.getQuery().split("\\s"));

            List<String> urlSearchList = searchService.urlSearchListSet(request, urlList);

            int siteId = 0;
            for (String url : urlSearchList) {

                siteId++;
                if (!searchService.siteIsIndexed(url, sitesRep)) { continue; }

                TreeMap<Float, List<Integer>> sortedRequest = searchService.sortedRequest(requestWords, lemmasRep, siteId);
                HashMap<Integer, HashMap<Integer,Float>> foundPages = searchService.foundPagesSearch(sortedRequest, lemmasRep, indexRep, siteId);
                HashMap<Integer, Float> absRel = searchService.absRelCalculation(foundPages, pagesRep, siteId);
                TreeMap<Integer, Page> pages = searchService.pagesCollector(pagesRep, absRel, siteId);
                HashMap<Integer, Float> relRel = searchService.relRel(absRel);

                answer = searchService.searchThreadsStarter(answer, request, absRel,relRel, pages, url);

            }

        } else {

            answer.setResult(false);
            if (isEmptyQuery) { answer.setError("Задан пустой поисковый запрос"); };
            if (!existingSite) { answer.setError("Заданный сайт отсутствует в конфигурационном списке"); };
            if (!isIndexedSite) { answer.setError("Заданный сайт не проиндексирован"); };

        }

        Logger.getLogger(MainService.class.getName()).info("Search method finished process for request: " + request.getQuery());
        return answer;
    }

    public WebStatisticsAnswer statisticsProcess(boolean isIndexationWorks){

        Logger.getLogger(MainService.class.getName()).info("Statistics requested");

        WebStatisticsAnswer answer = new WebStatisticsAnswer();
        answer.setResult(true);
        WebStatisticsAnswer.WebStatistics statistics = new WebStatisticsAnswer.WebStatistics();
        WebStatisticsAnswer.WebStatistics.WebTotal total = new WebStatisticsAnswer.WebStatistics.WebTotal();
        List<WebStatisticsAnswer.WebStatistics.WebDetailed> detailedList = new ArrayList<>();

        Integer sitesCounter = 0;
        for (Site site : siteRepository.findAll()) {
            WebStatisticsAnswer.WebStatistics.WebDetailed detailed = new WebStatisticsAnswer.WebStatistics.WebDetailed();

            detailed.setUrl(site.getUrl());
            detailed.setName(site.getName());
            detailed.setStatus(site.getStatus());
            detailed.setStatusTime(site.getStatusTime());
            detailed.setError(site.getLastError());

            Integer pageCounter = 0;
            for (Page page: pageRepository.findAll()) {
                if (page.getSiteId() == site.getId()) { pageCounter++; }
            }
            detailed.setPages(pageCounter);

            Integer lemmaCounter = 0;
            for (Lemma lemma : lemmaRepository.findAll()) {
                if (lemma.getSiteId() == site.getId()) { lemmaCounter++; }
            }
            detailed.setLemmas(lemmaCounter);

            detailedList.add(detailed);
            sitesCounter++;
        }

        Integer pagesCounter = 0;
        for (Page page : pageRepository.findAll()) { pagesCounter++; }
        Integer lemmaCounter = 0;
        for (Lemma lemma : lemmaRepository.findAll()) { lemmaCounter++; }

        total.setSites(sitesCounter);
        total.setPages(pagesCounter);
        total.setLemmas(lemmaCounter);
        total.setIndexing(isIndexationWorks);
        statistics.setTotal(total);
        statistics.setDetailed(detailedList);
        answer.setStatistics(statistics);

        Logger.getLogger(MainService.class.getName()).info("Statistics provided");
        return answer;
    }

    public boolean somethingIsIndexing(){

        for (Site site : siteRepository.findAll()) {
            if (site.getStatus().equals(Status.INDEXING.toString())) {
                return true;
            }
        }
        return false;
    }

    public void indexationStopperProcess(){

        for (Site site : siteRepository.findAll()) {
            if (site.getStatus().equals(Status.INDEXING.toString())) {
                site.setStatus(Status.FAILED.toString());
                site.setStatusTime(new java.util.Date(new java.util.Date().getTime()));
                site.setLastError("Indexing process interrupted manually");
                siteRepository.save(site);
            }
        }
    }

    public boolean isSiteParsed(int siteId){

        for (Page page : pageRepository.findAll()) {
            if (page.getSiteId().equals(siteId)) {
                return true;
            }
        }
        return false;
    }

    public void siteParsePublisher(SearchEngine searchEngine) {

        int siteId = searchEngine.getSiteId();

        Vector<Page> searchResults = searchEngine.getPages();

        for (Page page : searchResults) {
            page.setSiteId(siteId);
        }

        HashMap<String, Integer> pagesInBase = new HashMap<>();

        for (Page page : pageRepository.findAll()) {
            if (page.getSiteId() != siteId) { continue; }
            pagesInBase.put(page.getPath(),page.getId());
        }


        for (Page resultPage : searchResults) {

            if (pagesInBase.containsKey(resultPage.getPath())) {
                resultPage.setId(pagesInBase.get(resultPage.getPath()));
                pageRepository.save(resultPage);
            } else {
                Page uploadPage = pageRepository.save(resultPage);
                pagesInBase.put(resultPage.getPath(), uploadPage.getId());
            }

        }

    }

    public boolean pagesIndexed(){

        for (Page page : pageRepository.findAll()) {
            return true;
        }
        return false;
    }

    public Site siteIsFound(String url, int siteId) {

        Site actualSite = null;

        boolean siteIsFound = false;
        for (Site site : siteRepository.findAll()) {
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

        siteRepository.save(actualSite);
        return actualSite;
    }

    public boolean siteIsIndexed(int siteId){

        for (Page page : pageRepository.findAll()) {
            if (page.getSiteId().equals(siteId)) {
                return true;
            }
        }

        return false;
    }

    public List<Lemma> existingLemmas(int siteId){

        List<Lemma> lemmas = new ArrayList<>();
        for (Lemma lemma : lemmaRepository.findAll()) {
            if (lemma.getSiteId() == siteId) {
                lemma.setFrequency(0F);
                lemmas.add(lemma);
            }
        }
        return lemmas;
    }

    public List<Index> existingIndexes(int siteId){

        List<Index> indexes = new ArrayList<>();
        for (Index index : indexRepository.findAll()) {
            if (index.getSiteId() == siteId) {
                index.setRanke(0F);
                indexes.add(index);
            }
        }
        return indexes;
    }

    public List<Page> pagesList(){
        return  (List<Page>) pageRepository.findAll();
    }

    public HashMap<String,Float> convertedWords(Page resultPage){

        HashMap<String,Float> convertedWords = new HashMap<>();
        LemmatizationEngine lemmatization = new LemmatizationEngine();

        for (Field field : fieldRepository.findAll()) {

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
                    lemmaRepository.save(lemma);
                    return lemma;
                }
            }
        }

        Lemma lemma = new Lemma();
        lemma.setLemma(word);
        lemma.setFrequency(1F);
        lemma.setSiteId(siteId);

        lemmaRepository.save(lemma);
        for (Lemma lemmaBase : lemmaRepository.findAll()) {
            if (lemmaBase.getSiteId() != siteId) { continue; }
            if (lemmaBase.getLemma().equals(word)) {
                lemma.setId(lemmaBase.getId());
                break;
            }
        }

        return lemma;
    }

    public Index indexDefined(Page resultPage, Integer lemmaId, Float ranke, int siteId){

        for (Index index : existingIndexes(siteId)) {
            if ((index.getPageId() == resultPage.getId()) && (index.getLemmaId() == lemmaId)) {
                index.setRanke(ranke);
                indexRepository.save(index);
                return index;
            }
        }

        Index index = new Index();
        index.setPageId(resultPage.getId());
        index.setSiteId(siteId);
        index.setLemmaId(lemmaId);
        index.setRanke(ranke);
        indexRepository.save(index);
        return index;
    }

    public void actualSiteSave(Site actualSite){

        actualSite.setStatusTime(new java.util.Date(new java.util.Date().getTime()));
        siteRepository.save(actualSite);
    }

    public void siteStatusUpdate(String url, int siteId) {

        for (Site site : siteRepository.findAll()) {
            if (site.getUrl().equals(url) && (site.getId().equals(siteId))) { siteRepository.save(updater(site)); }
        }


    }

    public Site updater(Site site) {

        site.setStatus(Status.INDEXED.toString());
        site.setStatusTime(new java.util.Date(new java.util.Date().getTime()));

        return site;
    }

}
