package gr.aueb.demo;
 
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import gr.aueb.structures.Email;
import gr.aueb.service.EmailService;
import javax.faces.bean.SessionScoped;
import org.primefaces.context.RequestContext;
 
@ManagedBean
@SessionScoped
public class EmailView implements Serializable {
     
    private List<Email> emails;
    private int size=0; 
    private Email selectedEmail;
     
     
    @PostConstruct
    public void init() {
        emails = EmailService.createEmails();
    }
 
    public List<Email> getEmails() {
        if (PropertyRegistryBean.isInitialized)
            return PropertyRegistryBean.getEmails();
        else
            return EmailService.createEmails();
    }

 
    public Email getSelectedEmail() {
        return selectedEmail;
    }
 
    public void setSelectedEmail(Email selectedEmail) {
        this.selectedEmail = selectedEmail;
    }
    public int getSize(){
        return size;
    }
    
    
}