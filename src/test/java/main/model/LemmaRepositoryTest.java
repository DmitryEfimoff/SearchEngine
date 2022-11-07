package main.model;

import main.Main;
import main.model.Lemma;
import main.model.LemmaRepository;
import main.model.SiteList;
import main.responses.WebSearchAnswer;
import main.responses.WebSearchRequest;
import main.service.MainService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Java6Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase
@DisplayName("LemmaRepository check")
public class LemmaRepositoryTest {

  @Autowired
  private LemmaRepository lemmaRepository;

  @BeforeEach
  void initUseCase() {

    Lemma lemma = new Lemma();
    lemma.setId(10);
    lemma.setLemma("default_value");
    lemma.setSiteId(1);
    lemma.setFrequency(0.0F);

    lemmaRepository.save(lemma);

  }

  @AfterEach
  public void destroyAll(){
    lemmaRepository.deleteAll();
  }

  @Test
  @DisplayName("Lemma repository 'Save' check")
  void saveLemmasSuccess(){

    List<Lemma> lemmas = new ArrayList<>();

    for (int i = 0; i < 3; i++) {

      Lemma lemma = new Lemma();
      if (i == 0) { lemma.setLemma("черный"); }
      if (i == 1) {
        lemma.setLemma("белый");
      } else { lemma.setLemma("синий"); }
      lemma.setSiteId(1);
      lemma.setFrequency(1.0F);
      lemmas.add(lemma);
    }

    Iterable<Lemma> allLemmas = lemmaRepository.saveAll(lemmas);

    AtomicInteger validIdFound = new AtomicInteger();

    allLemmas.forEach(lemma -> {
      if (lemma.getId() > 0) { validIdFound.getAndIncrement(); }
    });

    assertThat(validIdFound.intValue()).isEqualTo(3);

  }

  @Test
  @DisplayName("Lemma repository 'FindAll' check")
  void findAllSuccess(){
    List<Lemma> lemmas = (List<Lemma>) lemmaRepository.findAll();
    assertThat(lemmas.size()).isGreaterThanOrEqualTo(1);
  }

  @Test
  @DisplayName("Lemma repository 'Find by Lemma' success check")
  void findByLemmaSuccess(){
    List<Lemma> lemmas = lemmaRepository.findAllByLemma("default_value");
    assertThat(lemmas.size()).isGreaterThanOrEqualTo(1);
  }

  @Test
  @DisplayName("Lemma repository 'Find by Lemma' failed check")
  void findByLemmaFailed(){
    List<Lemma> lemmas = lemmaRepository.findAllByLemma("default");
    assertThat(lemmas.size()).isEqualTo(0);
  }

}
