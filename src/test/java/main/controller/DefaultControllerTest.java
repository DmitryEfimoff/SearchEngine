package main.controller;

import main.responses.WebAnswer;
import main.responses.WebSearchAnswer;
import main.responses.WebSearchRequest;
import main.responses.WebStatisticsAnswer;
import main.service.MainService;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest({DefaultController.class})
public class DefaultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MainService mainService;

    @Test
    @DisplayName("Statistics request check")
    public void statisticsRequestSuccess() throws Exception {

        WebStatisticsAnswer answer = new WebStatisticsAnswer();
        answer.setResult(true);
        WebStatisticsAnswer.WebStatistics statistics = new WebStatisticsAnswer.WebStatistics();
        WebStatisticsAnswer.WebStatistics.WebTotal total = new WebStatisticsAnswer.WebStatistics.WebTotal();
        answer.setStatistics(statistics);
        statistics.setTotal(total);
        total.setSites(0);
        total.setPages(3664);
        total.setLemmas(0);
        total.setIndexing(false);

        boolean isIndexationWorks = false;

        when(mainService.statisticsProcess(false)).thenReturn(answer);

        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:8080/statistics")).andExpect(status().isOk())
                .andExpect(content().json("{\"result\":true,\"statistics\":{\"total\":{\"sites\":0,\"pages\":3664,\"lemmas\":0,\"indexing\":false},\"detailed\":null}}"));

    }

}
