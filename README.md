# web-data-extractor 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/im.nll.data/extractor/badge.svg)](https://maven-badges.herokuapp.com/maven-central/im.nll.data/extractor/)
[![Build Status](https://travis-ci.org/fivesmallq/web-data-extractor.svg)](https://travis-ci.org/fivesmallq/web-data-extractor)
[![codecov.io](http://codecov.io/github/fivesmallq/web-data-extractor/coverage.svg?branch=master)](http://codecov.io/github/fivesmallq/web-data-extractor?branch=master)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

Extracting and parsing structured data with Jquery Selector, XPath or JsonPath from common web format like HTML, XML and JSON.

Implements:

 * Jquery Selector - [Jsoup](https://github.com/jhy/jsoup) and [Jerry](http://jodd.org/doc/jerry/index.html)
 * XPath -  [Jdom2](https://github.com/hunterhacker/jdom/)
 * JsonPath - [JsonPath](https://github.com/jayway/JsonPath)


###Usage
To add a dependency on Web-Data-Extractor using Maven, use the following:

```xml
<dependency>
    <groupId>im.nll.data</groupId>
    <artifactId>extractor</artifactId>
    <version>0.9.6</version>
</dependency>
```

To add a dependency using Gradle:

```
dependencies {
  compile 'im.nll.data:extractor:0.9.6'
}
```


##Examples

###extract single data

````java
String followers = Extractors.on(baseHtml)
                   .extract(new SelectorExtractor("div.followers"))
                   .with(new RegexExtractor("\\d+"))
                   .asString();
````

or use static method

````java
String followers = Extractors.on(baseHtml)
                   .extract(selector("div.followers"))
                   .with(regex("\\d+"))
                   .asString();
````

or short string

````java
String followers = Extractors.on(baseHtml)
                   .extract("selector:div.followers"))
                   .with(regex("\\d+"))
                   .asString();
````

more method

````java
 String year = Extractors.on("<div> Talk is cheap. Show me the code. - Fri, 25 Aug 2000 </div>")
                .extract(selector("div")) // extract with selector
                .filter(value -> value.trim()) // trim result
                .with(regex("20\\d{2}")) // get year with regex
                .filter(value -> "from " + value) // append 'from' string
                .asString();
        Assert.assertEquals("from 2000", year);
````

###extract data to map

````java
    @Test
    public void testToMap() throws Exception {
        Map<String, String> dataMap = Extractors.on(baseHtml)
                .extract("title", selector("a.title"))
                .extract("followers", selector("div.followers")).with(regex("\\d+"))
                .extract("description", selector("div.description"))
                .asMap();
        Assert.assertEquals("fivesmallq", dataMap.get("title"));
        Assert.assertEquals("29671", dataMap.get("followers"));
        Assert.assertEquals("Talk is cheap. Show me the code.", dataMap.get("description"));
    }
  ````
  
###extract data to map list

````java

    @Test
    public void testToMapList() throws Exception {
        //split param must implements ListableExtractor
        List<Map<String, String>> languages = Extractors.on(listHtml)
            .split(selector("tr.item.html"))
                .extract("type", selector("td.type"))
                .extract("name", selector("td.name"))
                .extract("url", selector("td.url"))
                .asMapList();
        Assert.assertNotNull(languages);
        Map<String, String> second = languages.get(1);
        Assert.assertEquals(languages.size(), 3);
        Assert.assertEquals(second.get("type"), "dynamic");
        Assert.assertEquals(second.get("name"), "Ruby");
        Assert.assertEquals(second.get("url"), "https://www.ruby-lang.org");
    }
  ````
  
  
###extract data to bean

````java
    @Test
    public void testToBean() throws Exception {
        Base base = Extractors.on(baseHtml)
                .extract("title", selector("a.title"))
                .extract("followers", selector("div.followers")).with(regex("\\d+"))
                .extract("description", selector("div.description"))
                .asBean(Base.class);
        Assert.assertEquals("fivesmallq", base.getTitle());
        Assert.assertEquals("29671", base.getFollowers());
        Assert.assertEquals("Talk is cheap. Show me the code.", base.getDescription());
    }
````

###extract data to bean list

````java
    @Test
    public void testToBeanList() throws Exception {
        List<Language> languages = Extractors.on(listHtml)
            .split(selector("tr.item.html"))
                .extract("type", selector("td.type"))
                .extract("name", selector("td.name"))
                .extract("url", selector("td.url"))
                .asBeanList(Language.class);
        Assert.assertNotNull(languages);
        Language second = languages.get(1);
        Assert.assertEquals(languages.size(), 3);
        Assert.assertEquals(second.getType(), "dynamic");
        Assert.assertEquals(second.getName(), "Ruby");
        Assert.assertEquals(second.getUrl(), "https://www.ruby-lang.org");
    }
````

###filter
``before`` and ``after`` is the global filter.

```java
    @Test
    public void testToBeanListFilterBeforeAndAfter() throws Exception {
        List<Language> languages = Extractors.on(listHtml)
                //before and after just process the extract value, then execute the follow filter method.
                .before(value -> "|before|" + value)
                .after(value -> value + "|after|")
                .split(xpath("//tr[@class='item']"))
                .extract("type", xpath("//td[1]/text()")).filter(value -> "filter:" + value)
                .extract("name", xpath("//td[2]/text()")).filter(value -> "filter:" + value)
                .extract("url", xpath("//td[3]/text()")).filter(value -> "filter:" + value)
                .asBeanList(Language.class);
        Assert.assertNotNull(languages);
        Language second = languages.get(1);
        Assert.assertEquals(languages.size(), 3);
        Assert.assertEquals(second.getType(), "filter:|before|dynamic|after|");
        Assert.assertEquals(second.getName(), "filter:|before|Ruby|after|");
        Assert.assertEquals(second.getUrl(), "filter:|before|https://www.ruby-lang.org|after|");
    }
```

see [Example](https://github.com/fivesmallq/web-data-extractor/blob/master/src/test/java/im/nll/data/extractor/ExtractorsTest.java)
