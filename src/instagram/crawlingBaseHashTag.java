package instagram;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 인스타그램 해시태그 값을 입력받아 해당 해시태그로 작성된 게시물들을 수집
 * 현재시간부터 1시간 전 게시물만 크롤링
 */

public class crawlingBaseHashTag {
    private WebDriver driver;
    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "";

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private final ArrayList<HashMap<String, Object>> docs = new ArrayList<>();
    private final int timeout = 3;
    private static final String keyword = "";
    private static final Date endDate = new Date(new Date().getTime() - 3600000L);
    private static final Date startDate = new Date();

    public static void main(String[] args) throws InterruptedException{
        crawlingBaseHashTag instagram = new crawlingBaseHashTag();
        instagram.Crawling();
    }

    private void Crawling() throws InterruptedException {
        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        login();
        search();
        crawlingData();

        driver.close();
    }

    /**
     * 로그인
     * ID, PW는 변경 요망
     */
    private void login() {
        driver.get("https://www.instagram.com");
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);

        driver.findElement(By.name("username")).sendKeys("");
        WebElement pw = driver.findElement(By.name("password"));
        pw.sendKeys("");
        pw.submit();
    }

    /**
     * 검색
     * 리눅스환경에서는 필요
     *
     * @throws InterruptedException = Thread.sleep
     */
    private void search() throws InterruptedException {
        driver.findElement(By.cssSelector("input.XTCLo.x3qfX")).sendKeys("#"+keyword);
        driver.findElement(By.cssSelector("div.z556c")).click();
        Thread.sleep(1000);
    }

    /**
     * 데이터 크롤링
     * idx = 크롤링할 게시물 수
     * HashMap<String, Object> 형태로 저장하기 때문에 나중에 확인할 때 까다로울 수 있음
     * 필요한 부분만 저장해 사용할 예정
     * 주석처리된 부분은 게시물의 모든 사진이나 영상을 가져오기 위한 코드
     */
    private void crawlingData() throws InterruptedException {
        int search = 0;
        driver.findElements(By.cssSelector("div.v1Nh3.kIKUG._bz0w")).get(0).click(); //첫번째 게시물 클릭
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
        while (docs.size() < 100 && search < 150){
            search++;
            try {
                WebElement post = driver.findElement(By.cssSelector("div.PdwC2.fXiEu.s2MYR"));
                String date = post.findElement(By.cssSelector("time._1o9PC.Nzb55")).getAttribute("datetime");
                date = date.substring(0, date.indexOf(".000Z")) + "+0000";

                if (startDate.getTime() >= format.parse(date).getTime() && format.parse(date).getTime() >= endDate.getTime()) {
                    HashMap<String, Object> doc = new HashMap<>();
                    doc.put("date", date);
                    doc.put("link", post.findElement(By.cssSelector("a.c-Yi7")).getAttribute("href"));
                    if (findElementXPath(post, "//div[@class='C4VMK']/span"))
                        doc.put("content", post.findElement(new By.ByXPath("//div[@class='C4VMK']/span")).getText()); //content
                    if (findElementCSS(post, "button.sqdOP.yWX7d._8A5w5"))
                        doc.put("like", post.findElement(By.cssSelector("button.sqdOP.yWX7d._8A5w5")).getText()); // likes
                    /*
                     * like this = 좋아요는 없고 버튼만 있을 때
                     * 1 likes = 좋아요가 있을 때
                     * null = 요소가 없을 때
                     * 없는 경우 시간을 많이 잡아 먹음
                     * doc.put("hashtag", post.findElements(By.cssSelector("a.xil3i"))); //hashtag
                     * doc.put("comment", post.findElements(new By.ByXPath("//ul[@class='Mr508']/div/li/div/div[1]/div[2]/span"))); // comment
                     */

                    if (findElementCSS(post, "img.FFVAD"))
                        doc.put("img", post.findElement(By.cssSelector("img.FFVAD")).getAttribute("src"));
                    else if (findElementCSS(post, "video.tWeCl"))
                        doc.put("video", post.findElement(By.cssSelector("video.tWeCl")).getAttribute("src"));
                    docs.add(doc);
                }
            } catch (Exception ignored) { }

            if (findElementCSS("a._65Bje.coreSpriteRightPaginationArrow"))
                driver.findElement(By.cssSelector("a._65Bje.coreSpriteRightPaginationArrow")).click(); // next
            else
                break;
            Thread.sleep(1000);
        }
    }

    /**
     * 요소가 존재하는지 확인하기 위한 메소드
     *
     * @param css = 확인할 요소 형식
     * @return = bool 형태로 있다면 true 없으면 false 반환
     */
    private boolean findElementCSS(String css) {
        try {
            driver.findElement(By.cssSelector(css));
        } catch (NoSuchElementException nse) {
            return false;
        }
        return true;
    }

    /**
     * 요소가 존재하는지 확인하기 위한 메소드
     *
     * @param e   = driver가 아닌 WebElement에서 찾아야 할때 사용
     * @param css = 확인할 요소 형식
     * @return = bool 형태로 있다면 true 없으면 false 반환
     */
    private boolean findElementCSS(WebElement e, String css) {
        try {
            e.findElement(By.cssSelector(css));
        } catch (NoSuchElementException nse) {
            return false;
        }
        return true;
    }

    /**
     * 요소가 존재하는지 확인하기 위한 메소드
     *
     * @param e     = driver가 아닌 WebElement에서 찾아야 할때 사용
     * @param xpath = 확인할 요소 형식
     * @return = bool 형태로 있다면 true 없으면 false 반환
     */
    private boolean findElementXPath(WebElement e, String xpath) {
        try {
            e.findElement(new By.ByXPath(xpath));
        } catch (NoSuchElementException nse) {
            return false;
        }
        return true;
    }
}