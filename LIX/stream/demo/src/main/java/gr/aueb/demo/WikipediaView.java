package gr.aueb.demo;
 
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import gr.aueb.structures.Wikipedia;
import gr.aueb.service.WikipediaService;
 
@ManagedBean
@ViewScoped
public class WikipediaView implements Serializable {
     
    private List<Wikipedia> emails;
    private int size=0; 
    private Wikipedia selectedWikipedia;
     
     
    @PostConstruct
    public void init() {
        emails = WikipediaService.createWikipediaArticles();
    }
 
    public List<Wikipedia> getWikipediaArticles() {
        if (PropertyRegistryBean.isInitialized)
            return PropertyRegistryBean.getWikiArticles();
        else
            return WikipediaService.createWikipediaArticles();
    }

 
    public Wikipedia getSelectedWikipedia() {
        return selectedWikipedia;
    }
 
    public void setSelectedWikipedia(Wikipedia selectedWikipedia) {
        this.selectedWikipedia = selectedWikipedia;
    }
    public int getSize(){
        return size;
    }
}