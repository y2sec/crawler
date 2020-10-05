package naverNewsRss;

import java.net.MalformedURLException;

/**
 * 입력받은 키워드로 검색한 네이버 뉴스 RSS를 수집
 */
public class News_RSS {
    public static void main(String[] args) throws MalformedURLException {
        RSSFeedParser parser = new RSSFeedParser("http://newssearch.naver.com/search.naver?where=rss&query=" +
                "춘천&field=0&nx_search_query=&nx_and_query=&nx_sub_query=&nx_search_hlquery=&is_dts=0");
        Feed feed = parser.readFeed();

        for (FeedMessage message : feed.getMessages())
            System.out.println(message);
    }
}
