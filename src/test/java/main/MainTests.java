package main;

import main.Main;
import main.service.MainService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
public class MainTests {

    @Test
    public void contextLoads() {
//        main.model.LemmaRepositoryTest lemmaRepositoryTest = new main.model.LemmaRepositoryTest();
//        lemmaRepositoryTest.saveLemmasSuccess();
    }

}
