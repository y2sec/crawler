package instagram;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Instagram_Crawling extends JFrame {
    private WebDriver driver;
    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:\\Users\\Rectworks\\IdeaProjects\\chromedriver\\chromedriver.exe";

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    private final ArrayList<String> links_check = new ArrayList<>();
    private final HashMap<String, Long> avail_links = new HashMap<>();

    private final ArrayList<HashMap<String, String>> m_list = new ArrayList<>();

    public static void main(String[] args){
        new Instagram_Crawling();
    }
    public Instagram_Crawling() {
        super();
        JPanel panal = new JPanel();
        JLabel label = new JLabel("keyword : ");
        panal.add(label);
        JTextField keyword = new JTextField(20);
        panal.add(keyword);
        JButton btn = new JButton("검색");
        panal.add(btn);

        btn.addActionListener(e -> {
            if(!keyword.getText().equals("")) {
                ChromeOptions chromeOptions = new ChromeOptions();
                //chromeOptions.addArguments("--headless");
                //chromeOptions.addArguments("--no-sandbox");
                //chromeOptions.addArguments("--disable-dev-shm-usage");
                System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
                driver = new ChromeDriver();
                try {
                    Date date = new Date(new Date().getTime() - 900000);
                    craw(keyword.getText(), date);
                } catch (IOException | InterruptedException | ParseException parseException) {
                    JOptionPane.showMessageDialog(null,"입력 값이 부정확합니다.");
                }
            }
        });
        add(panal);
        setSize(600, 600);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    private void craw(String keyword, Date date) throws IOException, InterruptedException, ParseException {
        link_crawling(keyword, date);
        data_crawling();
        mapping();
        driver.close();
    }

    private void link_crawling(String keyword, Date end_date) throws InterruptedException, IOException {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        driver.get("https://www.instagram.com/explore/tags/" + keyword);
        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);

        int before_link_list_size;
        do {
            jse.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep(2000);

            WebElement root = driver.findElement(By.xpath("//article[@class='KC1QD']"));
            List<WebElement> element = root.findElements(By.cssSelector(".Nnq7C.weEfm a"));

            before_link_list_size = avail_links.size();
            for (WebElement e : element) {
                String link = e.getAttribute("href");
                if (!links_check.contains(link)) {
                    links_check.add(link);

                    Document doc = Jsoup.connect(link).get();

                    //Date
                    String string_doc = doc.toString();
                    String timestamp = string_doc.substring(string_doc.indexOf("taken_at_timestamp") + 20, string_doc.indexOf("taken_at_timestamp") + 30);
                    long time = Integer.parseInt(timestamp) * 1000L;

                    if (time >= end_date.getTime())
                        avail_links.put(link, time);
                }
            }
        }
        while (before_link_list_size != avail_links.size());
    }

    private void data_crawling() {
        for (String link : avail_links.keySet()) {
            driver.get(link);
            driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);

            HashMap<String, String> hm = new HashMap<>();

            WebElement root = driver.findElement(By.className("ltEKP"));
/*
            try {
                WebElement id = root.findElement(By.className("e1e1d"));
                hm.put("ID", id.getText());
            } catch (org.openqa.selenium.NoSuchElementException e) {
                continue;
            }

            try {
                WebElement hash_root = root.findElement(By.className("C4VMK"));
                List<WebElement> tag = hash_root.findElements(By.className("xil3i"));
                ArrayList<String> hashtags = new ArrayList<>();
                String hashtag = "";
                for (WebElement e : tag) {
                    hashtags.add(e.getText());
                    hashtag += e.getText() + " ";
                    ht_num.put(e.getText(), ht_num.getOrDefault(e.getText(), 0) + 1);
                }
                hm.put("HashTags", hashtag);
            } catch (org.openqa.selenium.NoSuchElementException e) {
                hm.put("HashTags", "");
            }
            try {
                WebElement likes_root = root.findElement(By.cssSelector(".EDfFK.ygqzn"));
                WebElement likes = likes_root.findElement(By.className("Nm9Fw"));
                hm.put("Likes", likes.getText().substring(4, likes.getText().lastIndexOf("개")));
            } catch (org.openqa.selenium.NoSuchElementException e) {
                hm.put("Likes", "0");
            }
 */
            try {
                WebElement hash_root = root.findElement(By.xpath("//*[@class='C4VMK']/span"));
                hm.put("content",hash_root.getText());
            } catch (org.openqa.selenium.NoSuchElementException e) {
                hm.put("content","");
            }

            try {
                hm.put("img",root.findElements(By.cssSelector(".FFVAD")).get(0).getAttribute("src"));
            } catch (IndexOutOfBoundsException e ) {
                hm.put("img",root.findElements(By.cssSelector(".tWeCl")).get(0).getAttribute("src"));
            }

            Date date = new Date(avail_links.get(link));
            hm.put("Date", format.format(date));
            hm.put("Link", link);

            m_list.add(hm);
        }
    }

    private void mapping() throws IOException, ParseException {
        for(int i = 0; i < m_list.size()/50.0; i++) {
            File file;
            int idx = 0;
            while(true) {
                file = new File("C:\\Users\\Rectworks\\IdeaProjects\\SNS_Crawling\\src\\instagram\\output_selenium_rss"+ idx +".xml");
                if(file.exists())
                    idx++;
                else
                    break;
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

            bufferedWriter.write("<rss xmlns:media=\"http://www.w3.org/2005/Atom\" version=\"2.0\">\n");
            bufferedWriter.write("\t<channel>\n");
            bufferedWriter.write("\t\t<title>ListenChuncheon</title>\n");
            bufferedWriter.write("\t\t<link>https://www.facebook.com/listenchuncheon</link>\n");
            bufferedWriter.write("\t\t<description>PageFeed on 춘천관광요람 Facebook Page</description>\n");
            for (int j = i * 50; j < m_list.size(); j++) {
                HashMap<String, String> map = m_list.get(j);

                bufferedWriter.write("\t\t<item>\n");
                bufferedWriter.write("\t\t\t<title>춘천관광요람</title>\n");
                bufferedWriter.write("\t\t\t<link>" + map.get("Link") + "</link>\n");
                bufferedWriter.write("\t\t\t<description>" + map.get("content").replace("\n","<br />") + "</description>\n");
                bufferedWriter.write("\t\t\t<media:thumbnail url=\"" + map.get("img") + "\"/>\n");
                bufferedWriter.write("\t\t\t<pubDate>" + map.get("Date") + "</pubDate>\n");
                Date date = format.parse(map.get("Date"));
                bufferedWriter.write("\t\t\t<wr_datetime>" + date.getTime()/1000L + "</wr_datetime>\n");
                bufferedWriter.write("\t\t</item>\n");

                if(j % 50 == 49)
                    break;
            }
            bufferedWriter.write("\t</channel>\n");
            bufferedWriter.write("</rss>\n");
            bufferedWriter.close();
        }
    }
}