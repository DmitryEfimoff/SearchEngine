package main;

import org.hibernate.SessionFactory;
import org.hibernate.boot.model.source.spi.PluralAttributeElementSourceManyToMany;

import org.hibernate.cfg.Configuration;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main {


//        pages for MULTISITE mode
//
//
//    CREATE TABLE `search_engine`.`pages` (
//            `id` INT NOT NULL AUTO_INCREMENT,
//  `path` VARCHAR(255) NOT NULL,
//    `site_id` INT NOT NULL,
//  `code` INT NOT NULL,
//            `content` MEDIUMTEXT NOT NULL,
//    PRIMARY KEY (`id`))
//    ENGINE = InnoDB
//    DEFAULT CHARACTER SET = utf8;


//        field
//
//
//    CREATE TABLE `search_engine`.`field` (
//            `id` INT NOT NULL AUTO_INCREMENT,
//  `name` VARCHAR(255) NOT NULL,
//  `selector` VARCHAR(255) NOT NULL,
//            `weight` FLOAT NOT NULL,
//    PRIMARY KEY (`id`))
//    ENGINE = InnoDB
//    DEFAULT CHARACTER SET = utf8;


//    INSERT INTO `field` VALUES (1,'title','title',1.0)
//    INSERT INTO `field` VALUES (2,'body','body',0.8)



//    lemma
//
//
//    CREATE TABLE `search_engine`.`lemma` (
//            `id` INT NOT NULL AUTO_INCREMENT,
//    `site_id` INT NOT NULL,
//  `lemma` VARCHAR(255) NOT NULL,
//            `frequency` INT NOT NULL,
//    PRIMARY KEY (`id`))
//    ENGINE = InnoDB
//    DEFAULT CHARACTER SET = utf8;


//        indexe
//
//
//    CREATE TABLE `search_engine`.`indexe` (
//            `id` INT NOT NULL AUTO_INCREMENT,
//    `site_id` INT NOT NULL,
//  `page_id` INT NOT NULL,
//            `lemma_id` INT NOT NULL,
//    `ranke` FLOAT NOT NULL,
//    PRIMARY KEY (`id`))
//    ENGINE = InnoDB
//    DEFAULT CHARACTER SET = utf8;


//        site
//
//
//    CREATE TABLE `search_engine`.`site` (
//            `id` INT NOT NULL AUTO_INCREMENT,
//    `status` ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL,
//  status_time DATETIME NOT NULL,
//            last_error TEXT,
//    url VARCHAR(255) NOT NULL,
//    name VARCHAR(255) NOT NULL,
//    PRIMARY KEY (`id`))
//    ENGINE = InnoDB
//    DEFAULT CHARACTER SET = utf8;



    public static void main(String[] args) {

        SpringApplication.run(Main.class, args);

    }

}
