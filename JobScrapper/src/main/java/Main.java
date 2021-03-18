import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.ColorPalette;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

public class Main {
    public static void main(String[] args){
        System.setProperty("webdriver.chrome.driver", "resources/chromedriver.exe");
        String searchTerm = "machine learning engineer";
        String location = "Ankara";
        LinkedInScrapper linkedInScrapper = new LinkedInScrapper(searchTerm, location);
        HashMap<String, ArrayList<Object>> dictionary = readSkills();
        HashMap<String, Integer> newDict = linkedInScrapper.scrapSkills(dictionary);
        createWordCloud(newDict,5,searchTerm);
    }

    public static void createWordCloud(HashMap<String, Integer> newDict, int numIteration, String saveLocation){
        HashMap<String,Integer> maxDict = new HashMap<>();
        for(int i = 0; i < numIteration; i++) {
            int max_value = 0;
            String max_key = "";
            for (String key : newDict.keySet()) {
                if (newDict.get(key) >= max_value) {
                    max_value = newDict.get(key);
                    max_key = key;
                }
            }
            maxDict.put(max_key, max_value);
            newDict.remove(max_key);
        }
        FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
        List<String> strs = new ArrayList<>();
        for(String key: maxDict.keySet()){
            for(int i = 0; i < maxDict.get(key); i++){
                strs.add(key);
            }
        }
        final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(strs);
        final Dimension dimension = new Dimension(600, 600);
        final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);

        wordCloud.setPadding(0);
        wordCloud.setBackground(new RectangleBackground(dimension));
        wordCloud.setColorPalette(new ColorPalette(Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.LIGHT_GRAY, Color.WHITE, Color.CYAN));
        wordCloud.setFontScalar(new LinearFontScalar(10, 100));
        wordCloud.build(wordFrequencies);
        wordCloud.writeToFile("resources/" + saveLocation + ".png");
    }

    public static HashMap<String, ArrayList<Object>> readSkills(){
        HashMap<String, ArrayList<Object>> dictionary = new HashMap<>();
        try{
            File myObj = new File("resources/skills.txt");
            Scanner reader = new Scanner(myObj);
            while (reader.hasNextLine()){
                String nextLine = reader.nextLine();
                String key = nextLine.split(",",2)[0];
                ArrayList<String> skills = new ArrayList<>();
                for(String same : nextLine.split(",",2)[1].split(",")){
                    skills.add(" " + same + " ");
                }
                dictionary.put(key, new ArrayList<>(Arrays.asList(skills,0)));
            }
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return dictionary;
    }

}

