package main.controller;

import main.engines.IndexationEngine;
import main.engines.LemmatizationEngine;
import main.engines.SearchEngine;
import main.model.*;
import main.responses.WebAnswer;
import main.responses.WebSearchAnswer;
import main.responses.WebSearchRequest;
import main.responses.WebStatisticsAnswer;
import main.service.IndexationService;
import main.service.SearchService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.*;

@Controller
public class DefaultController {

    private static final Integer OFFSET_DEFAULT = 0;

    private static final Integer LIMIT_DEFAULT = 20;

    private static List<String> urlList = new ArrayList<>();

    private static boolean isIndexationWorks = false;

    private static boolean indexationStopper = false;

    private Long startTime;

    private LemmatizationEngine lemmatization = new LemmatizationEngine();

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

    @Autowired
    private IndexationService indexationService;

    public static Integer getOffsetDefault() { return OFFSET_DEFAULT; }

    public static Integer getLimitDefault() { return  LIMIT_DEFAULT; }

    @RequestMapping("/admin")
    public String index(){
        return "index.html";
    }

    @GetMapping("/search")
    @ResponseBody
    public WebSearchAnswer request(WebSearchRequest request) {

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

        return answer;
    }

    @GetMapping("/statistics")
    @ResponseBody
    public WebStatisticsAnswer statistics(){

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

        return answer;
    }


    @GetMapping("/startIndexing")
    @ResponseBody
    public WebAnswer startIndexing() throws IOException {

        indexationStopper = false;

        WebAnswer webAnswer = new WebAnswer();

        boolean somethingIsIndexing = false;
        for (Site site : siteRepository.findAll()) {
            if (site.getStatus().equals(Status.INDEXING.toString())) {
                somethingIsIndexing = true;
                break;
            }
        }

        indexationStopper = false;

        if (!somethingIsIndexing) {
            if (urlList.isEmpty()) { SiteList.getUrls().forEach(u -> urlList.add(u)); }

            isIndexationWorks = true;
            webAnswer.setResult(true);

            int siteId = 1;
            for (String url : urlList) {

                IndexationEngine indexationEngine = new IndexationEngine(url, siteId);
                indexationEngine.start();

                siteId++;
            }


        } else {
            webAnswer.setResult(false);
            webAnswer.setError("Индексация уже запущена");
        }

        return webAnswer;
    }

    @GetMapping("/stopIndexing")
    @ResponseBody
    public WebAnswer stopIndexing() {

        WebAnswer webAnswer = new WebAnswer();

        boolean somethingIsIndexing = false;
        for (Site site : siteRepository.findAll()) {
            if (site.getStatus().equals(Status.INDEXING.toString())) {
                somethingIsIndexing = true;
                break;
            }
        }

        if (somethingIsIndexing) {
            indexationStopper = true;
            for (Site site : siteRepository.findAll()) {
                if (site.getStatus().equals(Status.INDEXING.toString())) {
                    site.setStatus(Status.FAILED.toString());
                    site.setStatusTime(new java.util.Date(new java.util.Date().getTime()));
                    site.setLastError("Indexing process interrupted manually");
                    siteRepository.save(site);
                }
            }
            webAnswer.setResult(true);
        } else {
            webAnswer.setResult(false);
            webAnswer.setError("Индексация не запущена");
        }

        return webAnswer;
    }

    @PostMapping("/indexPage")
    @ResponseBody
    public WebAnswer indexPage(String url) throws IOException, InterruptedException {

        indexationStopper = false;

        WebAnswer webAnswer = new WebAnswer();

        if (SiteList.getUrls().contains(url)) {

            int siteId = 1;
            for (String u : SiteList.getUrls()) {
                if (u.equals(url)) {
                    break;
                } else {
                    siteId++;
                }
            }

            webAnswer.setResult(true);
            isIndexationWorks = true;

            IndexationEngine indexationEngine = new IndexationEngine(url, siteId);
            indexationEngine.start();


        } else {
            webAnswer.setResult(false);
            webAnswer.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }

        return webAnswer;
    }

    @PostMapping("/indexPages")
    @ResponseBody
    public WebAnswer indexPages(String url, String siteIdI) throws IOException, InterruptedException {

        indexationStopper = false;

        WebAnswer webAnswer = new WebAnswer();

        int siteId = Integer.parseInt(siteIdI);

        if (SiteList.getUrls().contains(url)) {

            boolean siteParsed = false;
            for (Page page : pageRepository.findAll()) {
                if (page.getSiteId().equals(siteId)) {
                    siteParsed = true;
                    break;
                }
            }
            if (!siteParsed) {

                SearchEngine searchEngine = new SearchEngine(url,siteId);
                searchEngine.start();

                while (!siteParsed) {

                    Thread.sleep(10000);
                    if (!searchEngine.getPages().isEmpty()) {
                        siteParsed = true;
                        siteParsePublisher(searchEngine);
                    }
                }

            }

            isIndexationWorks = true;
            indexation(siteId, url);
            webAnswer.setResult(true);


        } else {
            webAnswer.setResult(false);
            webAnswer.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }

        return webAnswer;
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

    public boolean stopper(){
        if (indexationStopper) {
            isIndexationWorks = false;
            System.out.println("Indexation process Interrupted!");
            return true;
        }
        return false;
    }

    public void indexation(int siteId, String url) throws IOException {

        List<Site> sitesRep = (List<Site>) siteRepository.findAll();
        List<Lemma> lemmasRep = (List<Lemma>) lemmaRepository.findAll();
        List<Index> indexRep = (List<Index>) indexRepository.findAll();
        List<Page> pagesRep = (List<Page>) pageRepository.findAll();
        List<Field> fieldRep = (List<Field>) fieldRepository.findAll();

        System.out.println("Indexation process started!");

        if (stopper()) { return; }

        if (!indexationService.pagesIndexed(pagesRep)) { return; }

        Site actualSite = indexationService.siteIsFound(sitesRep, url, siteId);
        siteRepository.save(actualSite);

        if (!indexationService.siteIsIndexed(pagesRep, siteId)) { return; }

        if (stopper()) { return; }

        List<Lemma> lemmas = indexationService.existingLemmas(lemmasRep, siteId);
        List<Index> indexes = indexationService.existingIndexes(indexRep, siteId);

        if (stopper()) { return; }

        for (Page resultPage : pageRepository.findAll()) {

            if (resultPage.getSiteId() != siteId) { continue; }

            HashMap<String,Float> convertedWords = indexationService.convertedWords(resultPage, fieldRep);

            for (String word : convertedWords.keySet()) {

                Lemma lemmaDefined = indexationService.lemmaDefined(word, lemmas, siteId);

                if (lemmaDefined.getId() == null) {
                    lemmaRepository.save(lemmaDefined);
                    for (Lemma lemmaBase : lemmaRepository.findAll()) {
                        if (lemmaBase.getSiteId() != siteId) { continue; }
                        if (lemmaBase.getLemma().equals(word)) {
                            lemmaDefined.setId(lemmaBase.getId());
                            break;
                        }
                    }
                    lemmas.add(lemmaDefined);
                } else {
                    lemmaRepository.save(lemmaDefined);
                }

                Index indexDefined = indexationService.indexDefined(indexes, resultPage, lemmaDefined.getId(), convertedWords.get(word), siteId);

                indexRepository.save(indexDefined);

                actualSite.setStatusTime(new java.util.Date(new java.util.Date().getTime()));
                siteRepository.save(actualSite);

            }

            if (stopper()) { return; }
        }

        for (Site site : siteRepository.findAll()) {
            
            if (site.getUrl().equals(url) && (site.getId().equals(siteId))) { siteRepository.save(indexationService.siteStatusUpdate(site)); }
            if (stopper()) { return; }
        }

        System.out.println("Indexation process Complete!");
        isIndexationWorks = false;

    }


}
