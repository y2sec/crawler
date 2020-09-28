package youtube;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;


import java.util.*;
import java.util.concurrent.TimeUnit;

public class Youtube_Crawling {
    private final WebDriver driver;
    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:\\Users\\Rectworks\\IdeaProjects\\chromedriver\\chromedriver.exe";

    private final ArrayList<HashMap<String, String>> renderers = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {

        Scanner input = new Scanner(System.in);

        System.out.print("Keyword를 입력하시오 : ");
        String keyword = input.nextLine();

        System.out.print("{ 0: 지난 1시간, 1: 오늘, 2: 이번 주, 3: 이번 달, 4: 올해 } :");
        int upload = input.nextInt();

        System.out.print("{ 0: 관련성(defalut), 1: 업로드 날짜, 2: 조회수, 3: 평점 } :");
        int sort = input.nextInt();

        Youtube_Crawling craw = new Youtube_Crawling();
        craw.craw(keyword, upload, sort);
    }

    public Youtube_Crawling() {
        super();
        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
        driver = new ChromeDriver();
    }

    private void craw(String keyword, int upload, int sort) throws InterruptedException {
        filter(keyword, upload, sort);
        scroll();
        crawling();
        print();
    }

    private void filter(String keyword, int upload, int sort) throws InterruptedException {

        driver.get("https://www.youtube.com/results?search_query=" + keyword);
        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        WebElement filter = driver.findElement(new By.ByXPath("//*[@id=\"container\"]/ytd-toggle-button-renderer/a"));

        filter.click();
        WebElement upload_root = driver.findElement(new By.ByXPath("//*[@id=\"collapse-content\"]/ytd-search-filter-group-renderer[1]"));
        upload_root.findElements(By.id("endpoint")).get(upload).click();

        Thread.sleep(2000);
        if (sort != 0) {
            filter.click();
            WebElement sort_root = driver.findElement(new By.ByXPath("//*[@id=\"collapse-content\"]/ytd-search-filter-group-renderer[5]"));
            sort_root.findElements(By.id("endpoint")).get(sort).click();

            Thread.sleep(2000);
        }
    }

    private void scroll() throws InterruptedException {
        JavascriptExecutor jse = (JavascriptExecutor) driver;

        long last_page, new_page;
        do {
            last_page = (long) jse.executeScript("return document.documentElement.scrollHeight");

            driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL, Keys.END);
            Thread.sleep(2000);

            new_page = (long) jse.executeScript("return document.documentElement.scrollHeight");
        } while (last_page != new_page);
    }

    private void crawling() {
        List<WebElement> video_list = driver.findElements(By.xpath("/html/body/ytd-app/div/ytd-page-manager/ytd-search/div[1]/ytd-two-column-search-results-renderer/div/ytd-section-list-renderer/div[2]/ytd-item-section-renderer"));

        for (WebElement e : video_list) {
            List<WebElement> renderer_list = e.findElements(By.tagName("ytd-video-renderer"));

            for (WebElement w : renderer_list) {
                if(w.findElement(By.id("video-title")).getText().contains("교회"))
                    continue;
                HashMap<String, String> video = new HashMap<>();
                video.put("title", w.findElement(By.id("video-title")).getText());
                video.put("channel", w.findElement(By.id("channel-name")).getText());

                String meta_data = w.findElement(By.id("metadata-line")).getText();
                try {
                    video.put("views", meta_data.substring(0, meta_data.indexOf("\n")));
                    if (meta_data.contains("스트리밍 시간"))
                        video.put("time", meta_data.substring(meta_data.indexOf("스트리밍 시간: ") + 1));
                    else
                        video.put("time", meta_data.substring(meta_data.indexOf("\n") + 1));
                } catch (StringIndexOutOfBoundsException ex) {
                    video.put("views", "조회수 없음");
                    video.put("time", "미공개");
                }
                renderers.add(video);
            }
        }
    }

    private void print() {
        int idx = 1;
        for (HashMap<String, String> hm : renderers) {
            System.out.println("index : " + idx++);
            for (String key : hm.keySet()) {
                System.out.println(key + " : " + hm.get(key));
            }
            System.out.println("------------------------------");
        }
    }
}

