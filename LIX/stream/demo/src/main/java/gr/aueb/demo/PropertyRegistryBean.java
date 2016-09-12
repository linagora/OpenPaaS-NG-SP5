/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aueb.demo;

/**
 *
 * @author Midas
 */
import gr.aueb.service.EmailService;
import gr.aueb.service.StackOverflowService;
import gr.aueb.service.WikipediaService;
import gr.aueb.structures.Email;
import gr.aueb.structures.StackOverflow;
import gr.aueb.structures.Wikipedia;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.ejb.ConcurrencyManagementType.BEAN;
import javax.ejb.Singleton;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.Startup;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.primefaces.context.RequestContext;

@Singleton
@ConcurrencyManagement(BEAN)
@Startup
public class PropertyRegistryBean {
    // Note the java.util.Properties object is a thread-safe
    // collections that uses synchronization.  If it didn't
    // you would have to use some form of synchronization
    // to ensure the PropertyRegistryBean is thread-safe.

    // The @Startup method ensures that this method is
    // called when the application starts up.
    private static Map keywords;
    private static Map techicalKeywords;
    private static List<Email> emails;
    private static List<StackOverflow> SOquestions;
    private static List<Wikipedia> wikiArticles;
    public static Boolean isInitialized;
    public static List<String> stop_Words;
    public static CharArraySet stopSet;

    @PostConstruct
    public void applicationStartup() {

        try {

            stop_Words = Files.readAllLines(Paths.get(System.getProperty("user.home")+"/stoplist.txt"));
            stopSet = new CharArraySet(Version.LUCENE_48, stop_Words, true);

        } catch (IOException ex) {
            Logger.getLogger(PropertyRegistryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        isInitialized = true;
        keywords = new ConcurrentHashMap();
        keywords.put("java", 1);
        keywords.put("python", 3);
        //keywords.put("java", 2);
        //keywords.put("demo", 3);
        keywords.put("programming", 1);
        keywords.put("language", 4);

        //emails = EmailService.getEmails();
        //SOquestions = StackOverflowService.getStackOverflowQuestions();
        //wikiArticles = WikipediaService.getWikipediaArticles();
        TimerTask task = new RunMeTask();

        Timer timer = new Timer();
        timer.schedule(task, 0, 30 * 1000);

        // this method performs the task
    }

    public class RunMeTask extends TimerTask {

        @Override
        public void run() {

            ConcurrentHashMap keywords = new ConcurrentHashMap();

            List<String> testWords = new ArrayList<String>();
            testWords.add("java");
            testWords.add("language");
            testWords.add("programming");
            testWords.add("computer");
            testWords.add("model");
            testWords.add("python");
            testWords.add("css");
            testWords.add("learning");
            testWords.add("unix");
            ArrayList<Integer> list = new ArrayList<Integer>();

            for (int i = 1; i < 8; i++) {
                list.add(new Integer(i));
            }
            Collections.shuffle(list);
            for (int i = 0; i < 4; i++) {
                keywords.put(testWords.get(list.get(i)), list.get(i));
            }
            boolean ommitServicesDebugFlag = true;
            //System.out.println("Scheduled update");
            PropertyRegistryBean.setKeywords(keywords);
            PropertyRegistryBean.setEmails(EmailService.getEmails(testWords));

            if (!ommitServicesDebugFlag) {
                PropertyRegistryBean.setWikiArticles(WikipediaService.getWikipediaArticles());
                PropertyRegistryBean.setSOquestions(StackOverflowService.getStackOverflowQuestions());
            }

        }
    }

    @PreDestroy
    public void applicationShutdown() {
    }

    public static Map getKeywords() {
        return keywords;
    }

    public static void setKeywords(Map keywords) {
        PropertyRegistryBean.keywords = keywords;
    }

    public static Map getTechicalKeywords() {
        return keywords;
    }

    public static void setTechicalKeywords(Map techicalKeywords) {
        PropertyRegistryBean.techicalKeywords = techicalKeywords;
    }

    public static List<Email> getEmails() {
        return emails;
    }

    public static void setEmails(List<Email> emails) {
        PropertyRegistryBean.emails = emails;
    }

    public static List<StackOverflow> getSOquestions() {
        return SOquestions;
    }

    public static void setSOquestions(List<StackOverflow> SOquestions) {
        PropertyRegistryBean.SOquestions = SOquestions;
    }

    public static List<Wikipedia> getWikiArticles() {
        return wikiArticles;
    }

    public static void setWikiArticles(List<Wikipedia> wikiArticles) {
        PropertyRegistryBean.wikiArticles = wikiArticles;
    }

    public static String removeStopWords(String textFile) {
        //CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
        CharArraySet stopWords = PropertyRegistryBean.stopSet;
        TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_48, new StringReader(textFile.trim()));
        tokenStream = new StopFilter(Version.LUCENE_48, tokenStream, stopWords);
        StringBuilder sb = new StringBuilder();
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        try {
            tokenStream.reset();
        } catch (IOException ex) {
            Logger.getLogger(PropertyRegistryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            while (tokenStream.incrementToken()) {
                String term = charTermAttribute.toString();
                sb.append(term + " ");
            }
        } catch (IOException ex) {
            Logger.getLogger(PropertyRegistryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

}
