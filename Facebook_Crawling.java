package facebook;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class Facebook_Crawling {
    private static final String fb_id = "01026221334";
    private static final String fb_pw = "zxczxc.12";

    public static void main(String[] args) throws InterruptedException {
        Facebook_Crawling craw = new Facebook_Crawling();
        craw.craw(fb_id, fb_pw, "#춘천");
    }

    private final WebDriver driver;
    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:\\Users\\Rectworks\\IdeaProjects\\chromedriver\\chromedriver.exe";

    public Facebook_Crawling() {
        super();
        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
        driver = new ChromeDriver();
    }

    private void craw(String id, String pw, String keyword) throws InterruptedException {
        login(id, pw);
        search(keyword);
        crawling();
    }

    private void login(String id, String pw) throws InterruptedException {
        driver.get("https://www.facebook.com/");
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        driver.findElement(By.name("email")).sendKeys(id);
        WebElement passwd = driver.findElement(By.name("pass"));
        passwd.sendKeys(pw);
        passwd.submit();
        Thread.sleep(5000);
        Actions action = new Actions(driver);
        action.sendKeys(Keys.ESCAPE).build().perform();
    }

    private void search(String keyword) throws InterruptedException {
        WebElement fb_search = driver.findElement(By.xpath("//input[@type='search']"));
        fb_search.click();
        fb_search.sendKeys(keyword);

        Thread.sleep(1000);
        driver.findElements(By.cssSelector(".buofh1pr.cbu4d94t.j83agx80")).get(0).findElement(By.cssSelector(".k4urcfbm")).click();
        Thread.sleep(2000);

        driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL, Keys.END);
        Thread.sleep(2000);
        //driver.findElement(By.cssSelector("._42ft._4jy0._4w98._4jy3._517h._51sy._4w97")).click();

        List<WebElement> see_more = driver.findElements(By.cssSelector(".oajrlxb2.g5ia77u1.qu0x051f.esr5mh6w.e9989ue4.r7d6kgcz.rq0escxv.nhd2j8a9.nc684nl6.p7hjln8o." +
                "kvgmc6g5.cxmmr5t8.oygrvhab.hcukyx3x.jb3vyjys.rz4wbd8a.qt6c0cv9.a8nywdso.i1ao9s8h.esuyzwwr.f1sip0of.lzcic4wl.oo9gr5id.gpro0wi8.lrazzd5p"));

        for (WebElement e : see_more) {
            if(e.getText().equals("더 보기"))
                e.sendKeys(Keys.ENTER);
        }
    }

    private void crawling() {
        List<WebElement> post= driver.findElements(By.cssSelector(".qzhwtbm6.knvmm38d"));
        ArrayList<String> post_list = new ArrayList<>();

        String hashtag_url = ".oajrlxb2.g5ia77u1.qu0x051f.esr5mh6w.e9989ue4.r7d6kgcz.rq0escxv.nhd2j8a9.nc684nl6.p7hjln8o.kvgmc6g5." +
                "cxmmr5t8.oygrvhab.hcukyx3x.jb3vyjys.rz4wbd8a.qt6c0cv9.a8nywdso.i1ao9s8h.esuyzwwr.f1sip0of.lzcic4wl.q66pz984.gpro0wi8.b1v8xokw";

        for(WebElement e : post) {
            if(!e.getText().equals(""))
                post_list.add(e.getText());
        }

        for(int i=1; i<post_list.size();i++) {
            if(i % 3 == 1) {
                System.out.println("--------------------------------------------------");
                System.out.println("page : " + post_list.get(i));
            } else if(i % 3 == 2)
                System.out.println("date : " + post_list.get(i).substring(0, post_list.get(i).indexOf("\n")));
            else
                System.out.println("content : " + post_list.get(i));

        }



    }
}
