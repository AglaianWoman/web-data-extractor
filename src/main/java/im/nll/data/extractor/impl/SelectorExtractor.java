package im.nll.data.extractor.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import im.nll.data.extractor.ListableExtractor;
import im.nll.data.extractor.utils.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * * 一个使用Jquery选择器的抽取器
 * <p>
 * 关于jquery选择器语法请参考 <a
 * href="http://www.w3school.com.cn/jquery/jquery_ref_selectors.asp"
 * >http://www.w3school.com.cn/jquery/jquery_ref_selectors.asp</a>
 * <p>
 * 因为是基于jsoup实现，可参考<a
 * href="http://jsoup.org/cookbook/extracting-data/selector-syntax"
 * >http://jsoup.org/cookbook/extracting-data/selector-syntax</a>
 *
 * @author <a href="mailto:fivesmallq@gmail.com">fivesmallq</a>
 * @version Revision: 1.0
 * @date 15/12/25 下午9:25
 */
public class SelectorExtractor implements ListableExtractor {
    private final static String TYPE_TEXT = "text";
    private final static String TYPE_HTML = "html";
    /**
     * css selector
     */
    private String query;
    /**
     * 元素序号,0代表第一个(默认第一个元素)
     */
    private int eq = 0;
    /**
     * 输出类型 <li>text 只输出文本.</li> <li>html 输出带有html格式.</li><li>
     * 如果想获取其他属性,直接写属性名,比如'href'则输出元素的href属性值</li>
     */
    private String outType = "text";

    public SelectorExtractor(String query) {
        List<String> stringList = Splitter.on(",")
                .splitToList(query);
        this.query = stringList.get(0);
        if (stringList.size() > 1 && StringUtils.isNotNullOrEmpty(stringList.get(1))) {
            this.eq = StringUtils.tryParseInt(stringList.get(1), 0);
        }
        if (stringList.size() > 2 && StringUtils.isNotNullOrEmpty(stringList.get(2))) {
            this.outType = stringList.get(2);
        }
    }

    @Override
    public String extract(String data) {
        Document document = Jsoup.parse(data, "", Parser.xmlParser());
        String result = "";
        switch (outType) {
            case TYPE_TEXT:
                result = document.select(query).eq(eq).text();
                break;
            case TYPE_HTML:
                result = document.select(query).eq(eq).html();
                break;
            default:
                result = document.select(query).eq(eq).attr(outType);
                break;
        }
        return result;
    }

    @Override
    public List<String> extractList(String content) {
        List<String> strings = Lists.newArrayList();
        Document document = Jsoup.parse(content, "", Parser.xmlParser());
        Elements elements = document.select(query);
        for (Element element : elements) {
            switch (outType) {
                case TYPE_TEXT:
                    strings.add(element.text());
                    break;
                case TYPE_HTML:
                    strings.add(element.html());
                    break;
                default:
                    strings.add(element.attr(outType));
                    break;
            }
        }
        return strings;
    }
}
