package main;

import main.model.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.*;

@RestController
public class DefaultController {

    private static final Integer OFFSET_DEFAULT = 0;

    private static final Integer LIMIT_DEFAULT = 20;

    private static List<String> urlList = new ArrayList<>();

    private static boolean isIndexationWorks = false;

    private static boolean indexationStopper = false;

    private Long startTime;

    private Lemmatization lemmatization = new Lemmatization();

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

    @GetMapping("/search")
    public WebSearchAnswer request(WebSearchRequest request) {

        WebSearchAnswer answer = new WebSearchAnswer();
        List<WebSearchAnswer.WebSearchData> dataList = new ArrayList<>();

        int counter = 0;

        if (urlList.isEmpty()) {
            SiteList.getUrls().forEach(u -> urlList.add(u));

        }

        if (request.getOffset() == null) { request.setOffset(OFFSET_DEFAULT); }
        if (request.getLimit() == null) { request.setLimit(LIMIT_DEFAULT); }

        boolean isEmptyQuery = request.getQuery().isBlank();
        boolean existingSite = false;
        if (request.getSite() == null) {
            existingSite = true;
        } else {
            if (urlList.contains(request.getSite())) { existingSite = true; }
        }


        boolean isIndexedSite = false;
        if (request.getSite() == null) {
            for (Site site : siteRepository.findAll()) {
                if (site.getStatus().equals(Status.INDEXED.toString())) {
                    isIndexedSite = true;
                    break;
                }
            }
        } else {
            for (Site site : siteRepository.findAll()) {
                if (site.getUrl().equals(request.getSite()) && site.getStatus().equals(Status.INDEXED.toString())) {
                    isIndexedSite = true;
                    break;
                }
            }
        }


        if (!isEmptyQuery && existingSite && isIndexedSite) {

            String[] words = request.getQuery().split("\\s");

            HashMap<String, Integer> requestWords = lemmatization.wordLemmas(words);

            List<String[]> resultList = new ArrayList<>();

            List<String> urlSearchList = new ArrayList<>();
            if (request.getSite() == null) {
                urlSearchList = urlList;
            } else {
                urlSearchList.add(request.getSite());
            }

            int siteId = 1;
            for (String url : urlSearchList) {

                boolean siteIsIndexed = false;
                for (Site site : siteRepository.findAll()) {
                    if ((site.getStatus().equals(Status.INDEXED.toString())) && (site.getUrl().equals(url))) { siteIsIndexed = true; }
                }
                if (!siteIsIndexed) {
                    siteId++;
                    continue;
                }

                TreeMap<Float, List<Integer>> sortedRequest = new TreeMap<>();

                for (String word : requestWords.keySet()) {
                    for (Lemma lemma : lemmaRepository.findAll()) {
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


                List<Integer> resultPages = new ArrayList<>();
                HashMap<Integer, HashMap<Integer,Float>> foundPages = new HashMap<>();

                boolean isStartElement = true;
                String firstLemma = new String();
                for (Float frq : sortedRequest.keySet()) {
                    for (Integer lemmaId : sortedRequest.get(frq)) {

                        if (isStartElement) {

                            if (firstLemma.isEmpty()) {
                                for (Lemma lemma : lemmaRepository.findAll()) {
                                    if (lemmaId == lemma.getId()) {
                                        firstLemma = lemma.getLemma();
                                        break;
                                    }
                                }
                            }

                            for (Index index : indexRepository.findAll()) {
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
                            for (Index index : indexRepository.findAll()) {

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


                HashMap<Integer, Float> absRel = new HashMap<>();
                Float maxAbsRel = 0F;
                HashMap<Integer, Float> relRel = new HashMap<>();

                for (Integer pageId : foundPages.keySet()) {
                    absRel.put(pageId, 0F);
                    HashMap<Integer,Float> lemmas = foundPages.get(pageId);
                    for (Integer lemma : lemmas.keySet()) {
                        absRel.put(pageId, absRel.get(pageId) + lemmas.get(lemma));
                    }
                    if (maxAbsRel < absRel.get(pageId)) { maxAbsRel = absRel.get(pageId); }
                }



                for (Integer pageId : absRel.keySet()) {
                    relRel.put(pageId,absRel.get(pageId)/maxAbsRel);

                    for (Page page : pageRepository.findAll()) {
                        if (!page.getSiteId().equals(siteId)) { continue; }
                        if (page.getId().equals(pageId)) {

                            counter++;
                            String[] line = new String[5];
//                            line[0] = url + (url.charAt(url.length()-1) == '/' ? page.getPath().substring(1) : page.getPath());
                            line[0] = (url.charAt(url.length()-1) == '/' ? page.getPath().substring(1) : page.getPath());
                            line[1] = Jsoup.parse(page.getContent()).select("title").get(0).text();
                            line[3] = relRel.get(pageId).toString();
                            line[4] = url;
                            if ((counter > request.getOffset()) && (counter <= (request.getOffset()) + request.getLimit())) {
                                line[2] = "..." + lemmatization.snippet(firstLemma,page.getContent()) + "...";

                                WebSearchAnswer.WebSearchData newData = new WebSearchAnswer.WebSearchData();
                                newData.setSite(line[4]);
                                newData.setSiteName(line[4]);
                                newData.setUri(line[0]);
                                newData.setTitle(line[1]);
                                newData.setSnippet(line[2]);
                                newData.setRelevance(Float.parseFloat(line[3]));
                                dataList.add(newData);

                            } else {
                                line[2] = "N/A - out of offset and limit";
                            }
                            resultList.add(line);

                        }
                    }

                }

                siteId++;
            }

            answer.setResult(true);
            answer.setCount(counter);
            answer.setData(dataList);

        } else {

            answer.setResult(false);
            if (isEmptyQuery) { answer.setError("Задан пустой поисковый запрос"); };
            if (!existingSite) { answer.setError("Заданный сайт отсутствует в конфигурационном списке"); };
            if (!isIndexedSite) { answer.setError("Заданный сайт не проиндексирован"); };

        }

        return answer;
    }

    @GetMapping("/statistics")
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


    public void indexation(int siteId, String url) throws IOException {

        System.out.println("Indexation process started!");

//            Stopper
        if (indexationStopper) {
            isIndexationWorks = false;
            System.out.println("Indexation process Interrupted!");
            return;
        }

        boolean pagesIndexed = false;
        for (Page page : pageRepository.findAll()) {
            pagesIndexed = true;
            break;
        }
        if (!pagesIndexed) { return; }

        Site actualSite = null;

        boolean siteIsFound = false;
        for (Site site : siteRepository.findAll()) {
            if (site.getUrl().equals(url) && (site.getId().equals(siteId))) {
                siteIsFound = true;
                site.setStatus(Status.INDEXING.toString());
                site.setStatusTime(new java.util.Date(new java.util.Date().getTime()));
                siteRepository.save(site);
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
            siteRepository.save(newSite);
            actualSite = newSite;
        }


        boolean siteIndexed = false;
        for (Page page : pageRepository.findAll()) {
            if (page.getSiteId().equals(siteId)) {
                siteIndexed = true;
                break;
            }
        }
        if (!siteIndexed) {
            siteId++;
            return;
        }

//            Stopper
        if (indexationStopper) {
            isIndexationWorks = false;
            System.out.println("Indexation process Interrupted!");
            return;
        }

        List<Lemma> lemmas = new ArrayList<>();
        List<Index> indexes = new ArrayList<>();

        if (!lemmaRepository.equals(null)) {
            for (Lemma lemma : lemmaRepository.findAll()) {
                if (lemma.getSiteId() == siteId) {
                    lemma.setFrequency(0F);
                    lemmas.add(lemma);
                }
            }
        }
        if (!indexRepository.equals(null)) {
            for (Index index : indexRepository.findAll()) {
                if (index.getSiteId() == siteId) {
                    index.setRanke(0F);
                    indexes.add(index);
                }
            }
        }

//            Stopper
        if (indexationStopper) {
            isIndexationWorks = false;
            System.out.println("Indexation process Interrupted!");
            return;
        }

        for (Page resultPage : pageRepository.findAll()) {

            if (resultPage.getSiteId() != siteId) { continue; }

            HashMap<String,Float> convertedWords = new HashMap<>();

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

            for (String word : convertedWords.keySet()) {

                int lemmaID = 0;

                boolean lemmaExists = false;
                if (!lemmas.equals(null)){
                    for (Lemma lemma : lemmas) {
                        if (lemma.getLemma().equals(word)) {
                            lemma.setFrequency(lemma.getFrequency() + 1F);
                            lemmaRepository.save(lemma);
                            lemmaID = lemma.getId();
                            lemmaExists = true;
                            break;
                        }
                    }
                }

                if (!lemmaExists) {
                    Lemma lemma = new Lemma();
                    lemma.setLemma(word);
                    lemma.setFrequency(1F);
                    lemma.setSiteId(siteId);
                    lemmaRepository.save(lemma);
                    for (Lemma lemmaBase : lemmaRepository.findAll()) {
                        if (lemmaBase.getSiteId() != siteId) { continue; }
                        if (lemmaBase.getLemma().equals(word)) {
                            lemma.setId(lemmaBase.getId());
                            lemmaID = lemma.getId();
                            break;
                        }
                    }
                    lemmas.add(lemma);
                }

                boolean indexExists = false;
                if (!indexes.equals(null)) {
                    for (Index index : indexes) {
                        if ((index.getPageId() == resultPage.getId()) && (index.getLemmaId() == lemmaID)) {
                            index.setRanke(convertedWords.get(word));
                            indexRepository.save(index);
                            actualSite.setStatusTime(new java.util.Date(new java.util.Date().getTime()));
                            siteRepository.save(actualSite);
                            indexExists = true;
                            break;
                        }
                    }
                }

                if (!indexExists) {
                    Index index = new Index();
                    index.setPageId(resultPage.getId());
                    index.setSiteId(siteId);
                    index.setLemmaId(lemmaID);
                    index.setRanke(convertedWords.get(word));
                    indexRepository.save(index);
                    actualSite.setStatusTime(new java.util.Date(new java.util.Date().getTime()));
                    siteRepository.save(actualSite);
                }

            }

//            Stopper
            if (indexationStopper) {
                isIndexationWorks = false;
                System.out.println("Indexation process Interrupted!");
                return;
            }
        }

        for (Site site : siteRepository.findAll()) {
            if (site.getUrl().equals(url) && (site.getId().equals(siteId))) {
                site.setStatus(Status.INDEXED.toString());
                site.setStatusTime(new java.util.Date(new java.util.Date().getTime()));
                siteRepository.save(site);
            }
//            Stopper
            if (indexationStopper) {
                isIndexationWorks = false;
                System.out.println("Indexation process Interrupted!");
                return;
            }

        }

        System.out.println("Indexation process Complete!");
        isIndexationWorks = false;

    }


}
