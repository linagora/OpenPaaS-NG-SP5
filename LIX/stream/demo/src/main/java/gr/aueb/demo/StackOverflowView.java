package gr.aueb.demo;
 
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import gr.aueb.structures.StackOverflow;
import gr.aueb.service.StackOverflowService;
 
@ManagedBean
@ViewScoped
public class StackOverflowView implements Serializable {
     
    private List<StackOverflow> questions;
    private int size=0; 
    private StackOverflow selectedStackOverflow;
     
     
    @PostConstruct
    public void init() {
        questions = StackOverflowService.createStackOverflowQuestions();
    }
 
    public List<StackOverflow> getQuestions() {
        if (PropertyRegistryBean.isInitialized)
            return PropertyRegistryBean.getSOquestions();
        else
            return StackOverflowService.createStackOverflowQuestions();
    }

    public StackOverflow getSelectedQuestion() {
        return selectedStackOverflow;
    }
 
    public void setSelectedQuestion(StackOverflow selectedStackOverflow) {
        this.selectedStackOverflow = selectedStackOverflow;
    }
    public int getSize(){
        return size;
    }
}