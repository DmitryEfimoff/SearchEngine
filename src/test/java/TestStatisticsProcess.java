import main.model.SiteList;
import main.responses.WebSearchAnswer;
import main.responses.WebSearchRequest;
import main.service.MainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("StatisticsProcess method check")
public class TestStatisticsProcess {

  @Test
  @DisplayName("Поиск по слову 'черный'")
  void statisticsProcess() throws ParseException {

    WebSearchAnswer expectedAnswer = new WebSearchAnswer();

    WebSearchRequest request = new WebSearchRequest();
    request.setQuery("черный");
    request.setOffset(0);
    request.setLimit(10);
    request.setSite("http://playback.ru/");

    List<String> urlList = new ArrayList<>();
    urlList.add("http://playback.ru/");

    MainService mainService = new MainService();

    WebSearchAnswer actualAnswer = mainService.searchProcess(request, urlList);

    assertEquals(expectedAnswer, actualAnswer);

  }


}
