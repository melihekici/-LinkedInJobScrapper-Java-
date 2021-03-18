import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class LinkedInScrapper {
    private final WebDriver driver;
    private String url;

    public LinkedInScrapper(String searchTerm, String location) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        this.driver = new ChromeDriver(options);
        this.url = "https://www.linkedin.com/jobs/search?keywords={0}&location={1}";
        String searchTermFormatted = searchTerm.replace(" ", "%20");
        url = java.text.MessageFormat.format(url, searchTermFormatted, location);
    }

    private void seeAllJobs() {
        driver.get(url);
        WebElement language = driver.findElement(By.xpath("//button[contains(@class,'language-selector__button')]"));
        language.click();
        language = driver.findElement(By.xpath("//button[contains(@data-tracking-control-name,'language-selector-en_US')]"));
        language.click();
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            ;
        }

        int screenPauseTime = 1;

        Object screenHeight = ((JavascriptExecutor) driver).executeScript("return window.screen.height;");

        int i = 1;

        while (true) {
            ((JavascriptExecutor) driver).executeScript(java.text.MessageFormat.format("window.scrollTo(0,{0}*{1})", screenHeight.toString(), Integer.toString(i)));
            try {
                Thread.sleep(screenPauseTime * 1000);
            } catch (Exception e) {
                ;
            }


            try {
                WebElement more_button = driver.findElement(By.xpath("(//button[contains(@data-tracking-control-name,'show-more')])[1]"));
            } catch (Exception e) {
                ;
            }
            i++;
            Object scrollHeight = ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight;");
            if ((long) screenHeight * i > (long) scrollHeight) {
                break;
            }
        }
    }

    public List<String> findJobUrls() {
        String response = driver.getPageSource();
        driver.quit();
        String[] jobsList = response.split("linkedin.com/jobs/view/");
        jobsList = Arrays.copyOfRange(jobsList, 1, jobsList.length);
        List<String> jobUrls = new ArrayList<>();
        int count = 0;
        for (String job : jobsList) {
            jobUrls.add("https://www.linkedin.com/jobs/view/" + job.split(";")[0]);
            count++;
        }
        return jobUrls;
    }

    public String readJobUrl(String jobUrl) {
        try {
            URL url = new URL(jobUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept-Language", "en-US,en;q=0.9");

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            int responseCode = connection.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String jobInfo = Jsoup.parse(response.toString()).text();
            return jobInfo;
        } catch (Exception e) {
            ;
        }
        return "";
    }

    public String modifyText(String jobInfo){
        try{
            jobInfo = jobInfo.split("Report this job",2)[1].split("Show more",2)[0];
        }catch (Exception e){
            ;
        }
        jobInfo = jobInfo.toLowerCase(Locale.ROOT);
        jobInfo = jobInfo.replace("\\xa0", " ");
        jobInfo = jobInfo.replace("\"", "");
        jobInfo = jobInfo.replace(".", " ");
        jobInfo = jobInfo.replace(",", " ");

        return jobInfo;
    }

    public void addToDict(String jobInfo, HashMap<String, ArrayList<Object>> dictionary, HashMap<String, Integer> newDict){
        for(String key: dictionary.keySet()){
            ArrayList<Object> objects = dictionary.get(key);
            for(Object key2: (ArrayList<Object>)objects.get(0)){
                if(jobInfo.contains((String) key2)){
                    ArrayList<Object> skills = new ArrayList<>();
                    skills = (ArrayList<Object>)objects.get(0);
                    ArrayList<Object> dictValue = new ArrayList<Object>();
                    dictValue.add(skills);
                    dictValue.add((int)objects.get(1)+1);
                    dictionary.put(key, dictValue);
                    newDict.put(key, (int)objects.get(1)+1);
                }
            }
        }
    }

    public HashMap<String, Integer> scrapSkills(HashMap<String, ArrayList<Object>> dictionary){
        HashMap<String, Integer> newDict = new HashMap<>();
        seeAllJobs();
        List<String> jobUrls = findJobUrls();
        int count = 0;
        for(String url: jobUrls){
            String text = readJobUrl(url);
            text = modifyText(text);
            addToDict(text, dictionary, newDict);
        }
        return newDict;
    }

}
