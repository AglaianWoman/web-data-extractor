package im.nll.data.extractor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import im.nll.data.extractor.impl.*;
import im.nll.data.extractor.utils.Logs;
import im.nll.data.extractor.utils.Reflect;
import im.nll.data.extractor.utils.Validate;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:fivesmallq@gmail.com">fivesmallq</a>
 * @version Revision: 1.0
 * @date 15/12/28 下午4:43
 */
public class Extractors {
    private static final Logger LOGGER = Logs.get();
    private static final String DEFAULT_FIELD = "_default_field_";
    private String html;
    private List<String> htmlList;
    private Map<String, List<Extractor>> extractorsMap = Maps.newLinkedHashMap();
    private String prevField;

    public Extractors(String html) {
        this.html = html;
    }

    public static Extractors on(String html) {
        return new Extractors(html);
    }

    public Extractors extract(Extractor extractor) {
        extract(DEFAULT_FIELD, extractor);
        return this;
    }

    public Extractors extract(String field, Extractor extractor) {
        List<Extractor> extractors = extractorsMap.getOrDefault(field, Lists.newLinkedList());
        extractors.add(extractor);
        extractorsMap.put(field, extractors);
        this.prevField = field;
        return this;
    }

    public Extractors with(Extractor extractor) {
        Validate.notNull(prevField, "must call extract method first!");
        List<Extractor> extractors = extractorsMap.getOrDefault(prevField, Lists.newLinkedList());
        extractors.add(extractor);
        extractorsMap.put(prevField, extractors);
        return this;
    }


    /**
     * split html use listable extractor
     *
     * @param listableExtractor
     * @return
     */
    public Extractors split(Extractor listableExtractor) {
        Validate.isTrue(listableExtractor instanceof ListableExtractor, "split parameter must implement ListableExtractor." + listableExtractor.getClass().getSimpleName() + " can't be used.");
        this.htmlList = ((ListableExtractor) listableExtractor).extractList(html);
        return this;
    }

    public String asString() {
        List<Extractor> extractors = extractorsMap.get(DEFAULT_FIELD);
        String result = html;
        for (Extractor extractor : extractors) {
            result = extractor.extract(result);
        }
        return result;
    }

    public Map<String, String> asMap() {
        Map<String, String> map = Maps.newLinkedHashMap();
        for (Map.Entry<String, List<Extractor>> one : extractorsMap.entrySet()) {
            String name = one.getKey();
            List<Extractor> extractors = one.getValue();
            String result = html;
            for (Extractor extractor : extractors) {
                result = extractor.extract(result);
            }
            try {
                map.put(name, result);
            } catch (Exception e) {
                LOGGER.error("convert to map error! can't set '{}' with '{}'", name, result, e);
            }
        }
        return map;
    }

    public List<Map<String, String>> asMapList() {
        Validate.notNull(htmlList, "must split first!");
        List<Map<String, String>> mapList = Lists.newLinkedList();
        for (String input : htmlList) {
            Map<String, String> map = Maps.newLinkedHashMap();
            for (Map.Entry<String, List<Extractor>> one : extractorsMap.entrySet()) {
                String name = one.getKey();
                List<Extractor> extractors = one.getValue();
                String result = input;
                for (Extractor extractor : extractors) {
                    result = extractor.extract(result);
                }
                map.put(name, result);
            }
            mapList.add(map);
        }
        return mapList;
    }


    public <T> T asBean(Class<T> clazz) {
        T entity = Reflect.on(clazz).create().get();
        for (Map.Entry<String, List<Extractor>> one : extractorsMap.entrySet()) {
            String name = one.getKey();
            List<Extractor> extractors = one.getValue();
            String result = html;
            for (Extractor extractor : extractors) {
                result = extractor.extract(result);
            }
            try {
                Reflect.on(entity).set(name, result);
            } catch (Exception e) {
                LOGGER.error("convert to bean error! can't set '{}' with '{}'", name, result, e);
            }
        }
        return entity;
    }

    public <T> List<T> asBeanList(Class<T> clazz) {
        Validate.notNull(htmlList, "must split first!");
        List<T> entityList = Lists.newLinkedList();
        for (String input : htmlList) {
            T entity = Reflect.on(clazz).create().get();
            for (Map.Entry<String, List<Extractor>> one : extractorsMap.entrySet()) {
                String name = one.getKey();
                List<Extractor> extractors = one.getValue();
                String result = input;
                for (Extractor extractor : extractors) {
                    result = extractor.extract(result);
                }
                try {
                    Reflect.on(entity).set(name, result);
                } catch (Exception e) {
                    LOGGER.error("convert to bean error! can't set '{}' with '{}'", name, result, e);
                }
            }
            entityList.add(entity);
        }
        return entityList;
    }

    public static Extractor selector(String query) {
        return new SelectorExtractor(query);
    }

    public static Extractor json(String query) {
        return new JSONPathExtractor(query);
    }

    public static Extractor xpath(String query) {
        return new XPathExtractor(query);
    }

    public static Extractor regex(String query) {
        return new RegexExtractor(query);
    }

    public static Extractor stringRange(String query) {
        return new StringRangeExtractor(query);
    }
}