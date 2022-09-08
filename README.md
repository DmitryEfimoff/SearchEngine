# SearchEngine

**Search Engine** is a Web Application allowing to perform parsing, indexing and text request searching through predefined list of internet sites.
_MySQL_ database should be preinstalled on your server (MySQL server adress and login/password info are defined in SearchEngine configuration file **"application.yaml"**).
http://localhost:8080/search_engine is used as default.
_The list of internet sites_ which should be parsed and indexed is stored with _configuration file_ too and may be changed as required.
NOTE: it is suggested to write "/" slash at the end of each internet site adress entered into the list.

The access to Web Application and its administration is performed through the base page **http://localhost:8080/admin**
It contains three Tabs **"DASHBOARD"** (Default), **"MANAGEMENT"** and **"SEARCH"**

## "DASHBOARD"
contains an information about number of internet site pages from configuration Search List, total number of defined lemmas and indexes already defined in MySQL database.
Before very first Indexation process run all values are predefined zero value, 0, as soon as there are no site adresses from configuration file loaded to MySQL database yet.
Below Totals there is a statistics for each site page already defined in MySQL database including information about its actual indexation status.

## "MANAGEMENT"
allows to Start/Stop indexation process for all sites listed in configuration file or for a particular site adress which should be preincluded in configuration file list.

## "SEARCH"
allows to perfrom a search of an entered _Query_ phrase through the all or a particular one site indexed in MySQL database.
Search results are shown as a list in according with their relevation index.

_NOTE: search process may take a significant time depends of your server capacilty and query request relevation._

## IMPORTANT NOTE
MySQL table **Field** should be created manually in _**search_engine**_ base before **first run!!!** as shown below

    CREATE TABLE `search_engine`.`field` (
            `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `selector` VARCHAR(255) NOT NULL,
            `weight` FLOAT NOT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;

HTML page fields and their relevation factors should be predefined (they may be changed if required).

    INSERT INTO `field` VALUES (1,'title','title',1.0)
    INSERT INTO `field` VALUES (2,'body','body',0.8)
    
All other MySQL database tables may be created by JAVA machine in auto mode.
Manual instructions for each MySQL table creation are shown below (if something is wrong with Auto mode in Java):

### Pages

    CREATE TABLE `search_engine`.`pages` (
            `id` INT NOT NULL AUTO_INCREMENT,
  `path` VARCHAR(255) NOT NULL,
    `site_id` INT NOT NULL,
  `code` INT NOT NULL,
            `content` MEDIUMTEXT NOT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;
    
### Lemma

    CREATE TABLE `search_engine`.`lemma` (
            `id` INT NOT NULL AUTO_INCREMENT,
    `site_id` INT NOT NULL,
  `lemma` VARCHAR(255) NOT NULL,
            `frequency` INT NOT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;
    
### Indexe
NOTE: more simple names _Index_ and _rank_ can't be created and used in MySQL here as soon as @SpringBoot in Java defaultly using names without apostrophs _`index`_ and _`rank`_
to transfer commands to MySQL, where _index_ and _rank_ without apostrophs here are predefined as a different commands.

    CREATE TABLE `search_engine`.`indexe` (
            `id` INT NOT NULL AUTO_INCREMENT,
    `site_id` INT NOT NULL,
  `page_id` INT NOT NULL,
            `lemma_id` INT NOT NULL,
    `ranke` FLOAT NOT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;
    
### Site

    CREATE TABLE `search_engine`.`site` (
            `id` INT NOT NULL AUTO_INCREMENT,
    `status` ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL,
  status_time DATETIME NOT NULL,
            last_error TEXT,
    url VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;

