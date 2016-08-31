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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import static javax.ejb.ConcurrencyManagementType.BEAN;
import javax.ejb.Singleton;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.Startup;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

    @PostConstruct
    public void applicationStartup() {
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
        timer.schedule(task, 1000, 60000);

        // this method performs the task
    }

    public class RunMeTask extends TimerTask {

        long t0 = System.currentTimeMillis();

        @Override
        public void run() {
            if (System.currentTimeMillis() - t0 > 1000 * 1500) {
                System.out.println("cancelled");
                cancel();
            }
            ConcurrentHashMap keywords = new ConcurrentHashMap();

            List<String> testWords = new ArrayList<String>();
            testWords.add("java");
            testWords.add("language");
            testWords.add("programming");
            testWords.add("computer");
            testWords.add("model");
            testWords.add("algorithm");

            ArrayList<Integer> list = new ArrayList<Integer>();
            for (int i = 1; i < 6; i++) {
                list.add(new Integer(i));
            }
            Collections.shuffle(list);
            for (int i = 0; i < 4; i++) {
                keywords.put(testWords.get(list.get(i)), list.get(i));
            }
            System.out.println("Scheduled update");
            PropertyRegistryBean.setKeywords(keywords);
            PropertyRegistryBean.setWikiArticles(WikipediaService.getWikipediaArticles());
            PropertyRegistryBean.setEmails(EmailService.getEmails());
            PropertyRegistryBean.setSOquestions(StackOverflowService.getStackOverflowQuestions());
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

}
