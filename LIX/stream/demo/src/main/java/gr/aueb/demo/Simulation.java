/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aueb.demo;

import gr.aueb.kcore.KCore;
import gr.aueb.service.EmailService;
import gr.aueb.structures.Email;
import gr.aueb.structures.SimulatedMeeting;
import gr.aueb.structures.StackOverflow;
import gr.aueb.structures.Wikipedia;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudModel;
import gr.aueb.wordgraph.GraphOfWords;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.jgrapht.WeightedGraph;
import org.primefaces.context.RequestContext;
import org.primefaces.model.tagcloud.TagCloudItem;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 *
 * @author pmeladianos
 */
@ManagedBean
@SessionScoped
public class Simulation implements Serializable {

    private TagCloudModel model;
    private List<Email> emails;
    private Email selectedEmail;
    private List<StackOverflow> questions;
    private StackOverflow selectedStackOverflow;
    private List<Wikipedia> wikipediaArticles;
    private Wikipedia selectedWikipedia;
    private String meeting;
    private List<String> meetings;
    private SimulatedMeeting simMeeting;
    private File dataDir = new File(System.getProperty("user.home") + "/ami");
    private String rawText;
    private String groundTruth;
    private Integer intervalDuration = 120;
    private Integer nkeys = 10;

    @PostConstruct
    public void init() {
        this.emails=new ArrayList();
        File dir = dataDir;
        File[] filesList = dir.listFiles();
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".da-asr");
            }
        });
        meetings = new ArrayList<String>();
        for (File rawFile : files) {
            meetings.add(rawFile.getName());
        }
        model = new DefaultTagCloudModel();
        //model.addTag(new DefaultTagCloudItem("Select a meeting", 1));
        //model.addTag(new DefaultTagCloudItem("and press play", 1));

    }

    public TagCloudModel getModel() {
        return model;
    }

    public void simulate() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        int id = Integer.valueOf(params.get("interval"));
        //int id = progress;

        String text = this.simMeeting.getTextAtInterval(id);
        rawText = text;

        String[] sentences = text.split("\n");
        String aggregateText = "";
        for (String s : sentences) {
            aggregateText += s.split("\t")[1] + " ";
        }
        aggregateText = aggregateText.replaceAll("[^a-zA-Z ]", " ");
        String cleanText = PropertyRegistryBean.removeStopWords(aggregateText.toLowerCase());
        String[] tokens = cleanText.split(" ");
        aggregateText = "";
        for (String token : tokens) {
            aggregateText += stemTerm(token) + " ";
        }
        cleanText = aggregateText;
        Map<String, Double> degreeMap = new HashMap();
        //Map<String, Double> map = kcore.kcoreDecomposition(cleanText, 1);
        WeightedGraph g = new GraphOfWords(cleanText).getGraph();
        for (Object v : g.vertexSet()) {
            double deg = 0.0;
            for (Object e : g.edgesOf(v)) {
                deg += g.getEdgeWeight(e);
            }
            degreeMap.put(v.toString(), deg);
        }
        //map = KCore.sortByValue(map);
        double totalSumScore = 0.0;

        KCore kcore = new KCore();
        Map<String, Double> coreMap = kcore.kcoreDecomposition(g, 1, 0);
        for (Entry<String, Double> e : coreMap.entrySet()) {
            totalSumScore += e.getValue();
        }
        Map<String, Double> scores = sum(coreMap, degreeMap);
        scores = KCore.sortByValue(scores);
        this.groundTruth = this.simMeeting.getGroundAtInterval(id);

        model = new DefaultTagCloudModel();
        int kCounter = 0;
        List<TagCloudItem> tags = new ArrayList();
        List<String> keywords=new ArrayList();
        for (Entry<String, Double> e : scores.entrySet()) {
            if (e.getKey().length() < 4) {
                continue;
            }
            String original = this.simMeeting.getStem2original().getOrDefault(e.getKey(), null);
            if (original != null) {
                this.groundTruth = this.groundTruth.replaceAll(original, "<b>" + original + "</b>");
            }
            keywords.add(e.getKey());
            Double weight = e.getValue();
            int tagScore = 0;
            if(kCounter<=5)
                tagScore=5-kCounter;
            else
                tagScore=1;
            tags.add(new DefaultTagCloudItem(e.getKey(),tagScore));
            //model.addTag(new DefaultTagCloudItem(e.getKey(),kCounter+1));
            if (kCounter > this.nkeys) {
                break;
            }
            kCounter++;
        }
        long seed = System.nanoTime();
        Collections.shuffle(tags, new Random(seed));
        tags.forEach(t -> model.addTag(t));
        this.emails=EmailService.getEmails(keywords);
      
        this.groundTruth = this.groundTruth.replaceAll("\\n", "<br></br>");
        this.groundTruth = this.groundTruth.replaceAll("\\t", "&nbsp;");
        this.rawText = this.rawText.replaceAll("\\t", "&nbsp;");
        this.rawText = this.rawText.replaceAll("\\n", "<br></br>");
        RequestContext.getCurrentInstance().update("raw");
        RequestContext.getCurrentInstance().update("keywords");
        RequestContext.getCurrentInstance().update("grnd");
        RequestContext.getCurrentInstance().update("mails");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Simulation.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    public Email getSelectedEmail() {
        return selectedEmail;
    }

    public void setSelectedEmail(Email selectedEmail) {
        this.selectedEmail = selectedEmail;
    }

    public List<StackOverflow> getQuestions() {
        return questions;
    }

    public void setQuestions(List<StackOverflow> questions) {
        this.questions = questions;
    }

    public StackOverflow getSelectedStackOverflow() {
        return selectedStackOverflow;
    }

    public void setSelectedStackOverflow(StackOverflow selectedStackOverflow) {
        this.selectedStackOverflow = selectedStackOverflow;
    }

    public List<Wikipedia> getWikipediaArticles() {
        return wikipediaArticles;
    }

    public void setWikipediaArticles(List<Wikipedia> WikipediaArticles) {
        this.wikipediaArticles = WikipediaArticles;
    }

    public Wikipedia getSelectedWikipedia() {
        return selectedWikipedia;
    }

    public void setSelectedWikipedia(Wikipedia selectedWikipedia) {
        this.selectedWikipedia = selectedWikipedia;
    }

    public String getMeeting() {
        return meeting;
    }

    public Integer getMaxid() {
        if (simMeeting != null) {
            return simMeeting.getMaxId();
        } else {
            return 0;
        }
    }

    public void setMeeting(String meeting) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(this.dataDir + "/" + meeting));
        SimulatedMeeting m = new SimulatedMeeting(lines, this.intervalDuration);
        String grdFile = meeting.split("\\.")[0] + ".abstract_raw";
        lines = Files.readAllLines(Paths.get(this.dataDir + "/" + grdFile));
        m.setGroundTruth(lines);
        this.simMeeting = m;

        this.meeting = meeting;
    }

    public List<String> getMeetings() {
        return meetings;
    }

    public void setMeetings(List<String> meetings) {
        this.meetings = meetings;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public static String stemTerm(String term) {
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(term);
        stemmer.stem();
        return stemmer.getCurrent();
    }

    public void onMeetingChange() {
        System.out.println(meeting);
    }

    public void onKeysChange() {
        System.out.println(this.nkeys);
    }

    private Integer progress;
    private Boolean simStarted;

    public Integer getProgress() {
        Double perc = 0.0;
        if (progress == null) {
            progress = -1;
        } else {
            progress = progress + 1;
            if (progress >= 1) {
                simulate();
                perc = progress.doubleValue() / this.simMeeting.getMaxId().doubleValue() * 100;
                if (progress > this.simMeeting.getMaxId()) {
                    return 100;
                }
            }

        }

        return perc.intValue();
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public void onComplete() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Progress Completed"));
    }

    public void cancel() {
        progress = null;
    }

    public Boolean getSimStarted() {
        return simStarted;
    }

    public void setSimStarted(Boolean simStarted) {
        this.simStarted = simStarted;
    }

    public String getGroundTruth() {
        return groundTruth;
    }

    public void setGroundTruth(String groundTruth) {
        this.groundTruth = groundTruth;
    }

    public Integer getIntervalDuration() {
        return intervalDuration;
    }

    public void setIntervalDuration(Integer intervalDuration) {
        this.intervalDuration = intervalDuration;
    }

    public Integer getNkeys() {
        return nkeys;
    }

    public void setNkeys(Integer nkeys) {
        this.nkeys = nkeys;
    }

    private static Map<String, Double> sum(Map<String, Double> map1, Map<String, Double> map2) {
        Map<String, Double> result = new HashMap<String, Double>();
        result.putAll(map1);
        for (String key : map2.keySet()) {
            Double value = result.get(key);
            if (value != null) {
                Double newValue = value + map2.get(key);
                result.put(key, newValue);
            } else {
                result.put(key, map2.get(key));
            }
        }
        return result;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }
    
}
