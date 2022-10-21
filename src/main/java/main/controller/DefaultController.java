package main.controller;

import main.engines.IndexationEngine;
import main.engines.SearchEngine;
import main.model.*;
import main.responses.WebAnswer;
import main.responses.WebSearchAnswer;
import main.responses.WebSearchRequest;
import main.responses.WebStatisticsAnswer;
import main.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

@Controller
public class DefaultController {

    private static final Integer OFFSET_DEFAULT = 0;

    private static final Integer LIMIT_DEFAULT = 20;

    private static List<String> urlList = new ArrayList<>();

    private static boolean isIndexationWorks = false;

    private static boolean indexationStopper = false;

    private Long startTime;

    @Autowired
    private MainService mainService;


    public static Integer getOffsetDefault() { return OFFSET_DEFAULT; }

    public static Integer getLimitDefault() { return  LIMIT_DEFAULT; }

    @RequestMapping("/admin")
    public String index(){
        return "index.html";
    }

    @GetMapping("/search")
    @ResponseBody
    public WebSearchAnswer request(WebSearchRequest request) {

        return mainService.searchProcess(request, urlList);
    }

    @GetMapping("/statistics")
    @ResponseBody
    public WebStatisticsAnswer statistics(){

        return mainService.statisticsProcess(isIndexationWorks);
    }


    @GetMapping("/startIndexing")
    @ResponseBody
    public WebAnswer startIndexing() throws IOException {

        indexationStopper = false;

        WebAnswer webAnswer = new WebAnswer();

        if (!mainService.somethingIsIndexing()) {
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

        Logger.getLogger(DefaultController.class.getName()).info("Stop indexation request is performing ...");

        WebAnswer webAnswer = new WebAnswer();

        if (mainService.somethingIsIndexing()) {
            indexationStopper = true;
            mainService.indexationStopperProcess();
            webAnswer.setResult(true);
        } else {
            webAnswer.setResult(false);
            webAnswer.setError("Индексация не запущена");
            Logger.getLogger(DefaultController.class.getName()).info("There is no indexation started. Stop indexation cancelled.");
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

            boolean siteParsed = mainService.isSiteParsed(siteId);
            if (!siteParsed) {

                SearchEngine searchEngine = new SearchEngine(url,siteId);
                searchEngine.start();

                while (!siteParsed) {

                    Thread.sleep(10000);
                    if (!searchEngine.getPages().isEmpty()) {
                        siteParsed = true;
                        mainService.siteParsePublisher(searchEngine);
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



    public boolean stopper(int siteId, String url){
        if (indexationStopper) {
            isIndexationWorks = false;
            Logger.getLogger(DefaultController.class.getName()).info("Indexation process Interrupted for URL: " + url + " with siteId# " + siteId);
//            System.out.println("Indexation process Interrupted!");
            mainService.indexationStopperProcess();
            return true;
        }
        return false;
    }

    public void indexation(int siteId, String url) throws IOException {

        Logger.getLogger(DefaultController.class.getName()).info("Indexation process Started for URL: " + url + " with siteId# " + siteId);
//        System.out.println("Indexation process started!");

        if (stopper(siteId, url)) { return; }

        if (!mainService.pagesIndexed()) { return; }

        Site actualSite = mainService.siteIsFound(url, siteId);

        if (!mainService.siteIsIndexed(siteId)) { return; }

        if (stopper(siteId, url)) { return; }

        List<Lemma> lemmas = mainService.existingLemmas(siteId);

        if (stopper(siteId, url)) { return; }

        for (Page resultPage : mainService.pagesList()) {

            if (resultPage.getSiteId() != siteId) { continue; }

            HashMap<String,Float> convertedWords = mainService.convertedWords(resultPage);

            for (String word : convertedWords.keySet()) {

                Lemma lemmaDefined = mainService.lemmaDefined(word, lemmas, siteId);

                if (!lemmas.contains(lemmaDefined)) { lemmas.add(lemmaDefined); }

                Index indexDefined = mainService.indexDefined(resultPage, lemmaDefined.getId(), convertedWords.get(word), siteId);

                mainService.actualSiteSave(actualSite);

            }

            if (stopper(siteId, url)) { return; }
        }

        mainService.siteStatusUpdate(url, siteId);
        if (stopper(siteId, url)) { return; }

        Logger.getLogger(DefaultController.class.getName()).info("Indexation process Complete for URL: " + url + " with siteId# " + siteId);
//        System.out.println("Indexation process Complete!");
        isIndexationWorks = false;

    }


}
