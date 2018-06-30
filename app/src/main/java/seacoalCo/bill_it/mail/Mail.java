package seacoalCo.bill_it.mail;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by honneur on 19/03/18.
 */

public class Mail extends Activity{
    private String recievers[];
    private String subject;
    private String content;

    public Mail(String recievers[], String subject, String content){
        this.recievers = recievers;
        this.subject = subject;
        this.content = content;
    }

    public String[] getRecievers(){
        return recievers;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public Intent getIntentForSending(){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, recievers);
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        return Intent.createChooser(emailIntent,"Sending email...");
    }
}
