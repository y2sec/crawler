package tripAdvisor;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class TripAdvisor_Crawling {
    private final WebDriver driver;
    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:\\Users\\Rectworks\\IdeaProjects\\chromedriver\\chromedriver.exe";

    private final ArrayList<String> activity_list = new ArrayList<>();
    private final ArrayList<String> hotel_list = new ArrayList<>();
    private final ArrayList<String> restaurant_list = new ArrayList<>();

    private final ArrayList<HashMap<String,Object>> activity_all = new ArrayList<>();
    private final ArrayList<HashMap<String,Object>> hotel_all = new ArrayList<>();
    private final ArrayList<HashMap<String,Object>> restaurant_all = new ArrayList<>();

    public static void main(String[] arg) throws InterruptedException {
        Scanner input = new Scanner(System.in);

        System.out.print("keyword를 입력하시오 : ");
        String keyword = input.nextLine();

        TripAdvisor_Crawling craw = new TripAdvisor_Crawling();
        craw.craw(keyword);
    }

    public TripAdvisor_Crawling() {
        super();
        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
        driver = new ChromeDriver();
    }

    private void craw(String keyword) throws InterruptedException {
        search(keyword);
        crawling();
        print();
    }

    private void search(String keyword) throws InterruptedException {
        driver.get("https://www.tripadvisor.co.kr/");
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        WebElement search = driver.findElements(By.xpath("//input[@type='search']")).get(1);
        search.sendKeys(keyword);
        Thread.sleep(2000);

        driver.findElements(By.cssSelector("._27pk-lCQ a")).get(0).click();
        Thread.sleep(2000);
    }

    private void crawling() throws InterruptedException {
        List<WebElement> list = driver.findElements(By.cssSelector("._2aDyanzw"));

        WebElement activity = list.get(0);
        WebElement hotel = list.get(1);
        WebElement restaurant = list.get(2);

        for(WebElement e : activity.findElements(By.cssSelector("a")))
            activity_list.add(e.getAttribute("href"));

        for (WebElement e : hotel.findElements(By.cssSelector("a")))
            hotel_list.add(e.getAttribute("href"));

        for (WebElement e : restaurant.findElements(By.cssSelector("a")))
            restaurant_list.add(e.getAttribute("href"));

        for(String link : activity_list) {
            driver.get(link);
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

            HashMap<String, Object> hm = new HashMap<>();

            hm.put("name",driver.findElement(By.id("HEADING")).getText()); //관광지 이름

            String trip_rating = driver.findElement(By.cssSelector(".ui_bubble_rating")).getAttribute("class"); //별점
            trip_rating = trip_rating.substring(trip_rating.length()-2);
            hm.put("rating", Integer.parseInt(trip_rating)/ 10.0);

            //driver.findElement(By.cssSelector("._3WF_jKL7._1uXQPaAr")).getText(); //리뷰 건수

            ArrayList<HashMap<String,Object>> reviews = new ArrayList<>();
            for (WebElement e : driver.findElements(By.className("oETBfkHU"))) {
                HashMap<String, Object> review = new HashMap<>();

                String rating = e.findElement(By.cssSelector(".ui_bubble_rating")).getAttribute("class");
                rating = rating.substring(rating.length()-2);
                review.put("rating", Integer.parseInt(rating) / 10.0);
                review.put("title",e.findElement(By.className("glasR4aX")).getText());
                review.put("content",e.findElement(By.className("IRsGHoPm")).getText());

                reviews.add(review);
            }
            hm.put("reviews",reviews);
            activity_all.add(hm);
        }

        for(String link : hotel_list) {
            driver.get(link);
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

            HashMap<String, Object> hm = new HashMap<>();

            hm.put("name",driver.findElement(By.id("HEADING")).getText()); //관광지 이름


            String trip_rating = driver.findElement(By.cssSelector(".ui_bubble_rating")).getAttribute("class"); //별점
            trip_rating = trip_rating.substring(trip_rating.length()-2);
            hm.put("rating", Integer.parseInt(trip_rating)/ 10.0);

            //driver.findElement(By.cssSelector("._33O9dg0j")).getText(); //리뷰 건수

            ArrayList<HashMap<String,Object>> reviews = new ArrayList<>();
            for (WebElement e : driver.findElements(By.className("oETBfkHU"))) {
                HashMap<String, Object> review = new HashMap<>();

                String rating = e.findElement(By.cssSelector(".ui_bubble_rating")).getAttribute("class");
                rating = rating.substring(rating.length()-2);
                review.put("rating", Integer.parseInt(rating)/ 10.0);
                review.put("title",e.findElement(By.className("glasR4aX")).getText());
                review.put("content",e.findElement(By.className("IRsGHoPm")).getText());

                reviews.add(review);
            }
            hm.put("reviews",reviews);
            hotel_all.add(hm);
        }

        for(String link : restaurant_list) {
            driver.get(link);
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

            HashMap<String, Object> hm = new HashMap<>();

            hm.put("name",driver.findElement(By.className("_3a1XQ88S")).getText()); //관광지 이름

            String trip_rating = driver.findElement(By.cssSelector("._3KcXyP0F")).getAttribute("title"); //별점
            trip_rating = trip_rating.substring(trip_rating.length()-3);
            hm.put("rating",Double.parseDouble(trip_rating));

            //driver.findElement(By.cssSelector("._3Wub8auF")).getText(); //리뷰 건수

            ArrayList<HashMap<String,Object>> reviews = new ArrayList<>();

            driver.findElement(By.cssSelector(".taLnk.ulBlueLinks")).click();
            Thread.sleep(2000);

            for (WebElement e : driver.findElements(By.cssSelector(".review-container"))) {
                HashMap<String, Object> review = new HashMap<>();

                String rating = e.findElement(By.cssSelector(".ui_bubble_rating")).getAttribute("class");
                rating = rating.substring(rating.length()-2);
                review.put("rating",Integer.parseInt(rating)/ 10.0);
                review.put("title",e.findElement(By.className("noQuotes")).getText());
                review.put("content", e.findElement(By.className("partial_entry")).getText().replace("\n", " "));

                reviews.add(review);
            }
            hm.put("reviews", reviews);
            restaurant_all.add(hm);
        }
    }

    private void print() {
        for(HashMap<String,Object> hm : activity_all) {
            for (String key: hm.keySet()) {
                System.out.println(key + " : " + hm.get(key));
            }
        }
        for(HashMap<String,Object> hm : hotel_all) {
            for (String key: hm.keySet()) {
                System.out.println(key + " : " + hm.get(key));
            }
        }
        for(HashMap<String,Object> hm : restaurant_all) {
            for (String key: hm.keySet()) {
                System.out.println(key + " : " + hm.get(key));
            }
        }
    }
}

/*
관광지 이름
전체평점
리뷰 리스트 - 평점, 제목, 내용

식당 리뷰의 경우 더보기를 클릭하여 처리시 오류가 발생
*/