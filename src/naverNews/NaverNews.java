package naverNews;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * 입력받은 키워드 기준 네이버 뉴스들을 수집
 */
public class NaverNews {
    private static final String url = "https://search.naver.com/search.naver?where=news";
    private static final LinkedHashSet<String> NewsLinks = new LinkedHashSet<>();
    private static final ArrayList<HashMap<String, String>> NewsList = new ArrayList<>();
    private final Clustering clustering = new Clustering();
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH.mm");
    private static final SimpleDateFormat NaverFormat = new SimpleDateFormat("yyyy.MM.dd. aa h:mm");
    private static final SimpleDateFormat RssFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public static void main(String[] args) throws IOException, ParseException {
        new NaverNews();
    }

    public NaverNews() throws IOException, ParseException {
        super();
        String de = format.format(new Date());
        String ds = format.format(new Date().getTime() - 86400000L);
        String query = "";
        int sort = 1;
        int pd = 4;
        search(query, sort, pd, ds, de);
        crawling();
    }

    /**
     * @param query = 검색어
     * @param sort  = (0:관련도순, 1:최신순, 2:오래된순)
     * @param ds    = 기준시간
     * @param de    = 기준시간
     * ds, de를 적지 않아도 pd값과 sort값에 따라 크롤링 가능
     * @param pd    = (0:전체, 1:1주, 2:1개월, 3:선택입력, 4:1일, 5:1년, 6:6개월, 7:1시간, 8:2시간, 9:3시간, 10:4시간, 11:5시간, 12:6시간)
     * @throws IOException = Jsoup exception
     */
    private void search(String query, int sort, int pd, String ds, String de) throws IOException {
        for (int i = 1; i < 300; i += 10) {
            String searchURL = url + "&query=" + query + "&sm=tab_pge" + "&sort=" + sort + "&photo=0" + "&field=0" + "&reporter_article=" +
                    "&pd=" + pd + "&ds=" + ds + "&de=" + de + "&docid=" + "&nso=so:r,p:1d,a:all" + "&mynew=0" + "&start=" + i + "&refresh_start=0";

            Document doc = Jsoup.connect(searchURL).get();
            for (Element e : doc.select("a[href]")) {
                if (e.attr("href").contains("news.naver.com/main/read.nhn?mode=LSD&mid=sec&"))
                    NewsLinks.add(e.attr("href"));
            }
        }
    }

    private void crawling() throws IOException, ParseException {
        for (String url : NewsLinks) {
            Document doc = Jsoup.connect(url).get();
            String title = doc.title().replace(" : 네이버 뉴스", "");
            if (ClusteringTitle(title))
                continue;

            HashMap<String, String> news = new HashMap<>();

            try {
                Element timestamp = doc.select("span.t11").first();
                Date date = NaverFormat.parse(timestamp.text());
                news.put("date",RssFormat.format(date));
            } catch (Exception e) {
                Element timestamp = doc.select("span.author em").first();
                Date date = NaverFormat.parse(timestamp.text());
                news.put("date",RssFormat.format(date));
            }

            try {
                Element body = doc.select("div#articleBodyContents").first();
                String content = body.text();

                if (content.contains("▶"))
                    content = content.substring(0, content.indexOf("▶"));
                //이미지 설명 제거
                for (Element del : doc.select("em.img_desc"))
                    content = content.replace(del.text(), "");
                news.put("content", content);
            } catch (Exception e) {
                Element body = doc.select("div#articeBody").first();
                String content = body.text();
                if (content.contains("▶"))
                    content = content.substring(0, content.indexOf("▶"));
                //이미지 설명 제거
                for (Element del : doc.select("em.img_desc"))
                    content = content.replace(del.text(), "");
                news.put("content", content);
            }

            try {
                Element img = doc.select("span.end_photo_org img").first();
                news.put("img",img.attr("src"));
            } catch (Exception ignored) { }

            news.put("title", title);
            news.put("link", url);

            NewsList.add(news);
        }
    }

    private boolean ClusteringTitle(String s) {
        for (HashMap<String, String> news : NewsList) {
            double similarity = clustering.jaccard(news.get("title"), s);
            if (similarity > 0.1)
                return true;
        }
        return false;
    }

    private boolean ClusteringContent(String s) {
        for (HashMap<String, String> news : NewsList) {
            double similarity = clustering.jaccard(news.get("content"), s);
            if (similarity > 0.1)
                return true;
        }
        return false;
    }
}