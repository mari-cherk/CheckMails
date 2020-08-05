import javax.mail.Message;

import emailCode.EmailUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.annotations.Test;

public class EmailTests {

    @Test
    public void emailTest(){

        final EmailUtils email = new EmailUtils("testerattentive@gmail.com", "");

        /*Message[] messagesFrom = email.getMessagesFrom("no-reply@youtube.com");
        System.out.println(email.getEmailPojo(messagesFrom[0]).getBodyText());*/

        Message[] messagesWithSubject = email.getMessagesWithSubject("Security alert");
        System.out.println("The number of emails with the 'Security alert' subject is: " + messagesWithSubject.length);

        Message[] messagesWithSubjectFrom = email.getMessagesFromWithSubject("mail-noreply@google.com", "Three tips to get the most out of Gmail");
        System.out.println("The number of emails with the 'Three tips to get the most out of Gmail' subject from Google is: " + messagesWithSubjectFrom.length);

        Message[] messagesTo = email.getMessagesTo("testerattentive@gmail.com");
        System.out.println("The number of emails TO testerattentive@gmail.com is: " + messagesTo.length);
        System.out.println("Last message TO testerattentive@gmail.com is FROM: " + email.getEmailPojo(messagesTo[0]).getFrom());

        Message[] messagesFrom = email.getMessagesFrom("mail-noreply@google.com");
        System.out.println("The number of emails FROM mail-noreply@google.com is: " + messagesFrom.length);

        Message[] messagesToWithSubjectUnread = email.getMessagesToWithSubject("mail-noreply@google.com", "Three tips to get the most out of Gmail", true);
        System.out.println("The number of emails FROM mail-noreply@google.com is: " + messagesToWithSubjectUnread.length);

        Message[] messagesToWithSubject = email.getMessagesToWithSubject("testerattentive@gmail.com", "Your Spotio password has been updated");
        String html = email.getEmailPojo(messagesToWithSubject[0]).getBodyHtml();
        Document doc = Jsoup.parse(html);

        String name = doc.select("p").first().text().split(",")[0];
        System.out.println("The user name is: " + name);

        //String link = doc.select("a[href]").attr("href");
        Elements allText = doc.select("b");
        //Elements links = doc.select("a[href]");
        for (Element text : allText) {
            System.out.println("Text: " + text.text());
        }

        String link = doc.select("img").attr("src");
        System.out.println("The link is: " + link);

        //System.out.println(html);



    }

}
