package emailCode;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import java.io.IOException;
import java.util.Properties;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPSSLStore;


public class EmailUtils implements AutoCloseable {
    protected Session session;
    protected Store store;
    protected IMAPFolder folder;
    protected String username;
    protected String password;
    private boolean connected;
    private static final String URL = "imap.gmail.com";
    private static final int PORT = 993;
    private static final String PROTOCOL = "imaps";

    public EmailUtils(final String username, final String password){
        this.username = username;
        this.password = password;
        this.connected = false;

        final Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", 465);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", 465);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");

        props.setProperty("mail.store.protocol", PROTOCOL);
        props.setProperty("mail.user", username);
        props.setProperty("mail.password", password);

        session = Session.getInstance(props);

        final URLName urlName = new URLName(PROTOCOL, URL, PORT, null, username, password);
        store = new IMAPSSLStore(session, urlName);
    }

    private void connect(){
        if(!connected) {
            try{
                store.connect(URL, username, password);
                folder = (IMAPFolder) store.getFolder("INBOX");
                folder.open(Folder.READ_WRITE);
                connected = true;
            } catch (MessagingException e) {
                System.out.println("Error while connecting to the email server");
            }
        }
    }

    @Override
    public void close(){
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(true);
            }
            store.close();
            connected = false;
        } catch (MessagingException e) {
            System.out.println(e);
        }
    }

    public Message[] getMessagesFrom(final String from){
        connect();
        Message[] msg = null;
        try {
            msg = folder.search(new FromTerm(new EmailAddress(from).getAddressObject()));
        } catch (MessagingException e){
            System.out.println("Error while searching for emails");
        }
        return msg;
    }

    public Message[] getMessagesWithSubject(final String subject) {
        connect();
        Message[] msg = null;
        try {
            msg = folder.search(new SubjectTerm(subject));
        } catch (MessagingException e){
            System.out.println("Error while searching for emails");
        }
        return msg;
    }

    public Message[] getMessagesFromWithSubject(final String from, final String subject) {
        connect();
        Message [] msg = null;
        try{
            msg = folder.search(new AndTerm(new SearchTerm[]{new FromTerm(new EmailAddress(from).getAddressObject()), new SubjectTerm(subject)}));
        } catch (MessagingException e) {
            System.out.println("Error while searching for emails");
        }
        return msg;
    }

    public Message[] getMessagesTo(final String to){
        connect();
        Message[] msg = null;
        try {
            msg = folder.search(new RecipientStringTerm(Message.RecipientType.TO, to));
        } catch (MessagingException e) {
            System.out.println("Error while searching for emails");
        }
        return msg;
    }

    public Message[] getMessagesToWithSubject(final String to, final String subject){
        connect();
        Message[] msg = null;
        try {
            msg = folder.search(new AndTerm(new SearchTerm[]{new RecipientStringTerm(Message.RecipientType.TO, to), new SubjectTerm(subject)}));
        } catch (MessagingException e) {
            System.out.println("Error while searching for emails");
        }
        return msg;
    }

    public Message[] getMessagesToWithSubject(final String to, final String subject, final boolean isSeen) {
        connect();
        Message[] msg = null;
        try {
            msg = folder.search(new AndTerm(new SearchTerm[]{
                    new RecipientStringTerm(Message.RecipientType.TO, to),
                    new FlagTerm(new Flags(Flags.Flag.SEEN), isSeen),
                    new SubjectTerm(subject)}));
        } catch (MessagingException e) {
            System.out.println("Error while searching for emails");
        }
        return msg;
    }

    public EmailMessage getEmailPojo(final Message message) {
        return new EmailMessage(data -> {
            try {
                data.setFrom(message.getFrom()[0].toString());
                data.setTo(message.getRecipients(Message.RecipientType.TO)[0].toString());
                data.setSubject(message.getSubject());
                data.setBodyText(getTextFromMessage(message));
                data.setBodyHtml(getHtmlFromMessage(message));
            } catch (MessagingException e) {
                System.out.println("Error while parsing email to POJO");
            }
        });
    }

    private String getHtmlFromMessage(final Message message) {
        String result = null;
        try {
            result = getContentFromMessage(message, "text/html");
        } catch (MessagingException | IOException e) {
            System.out.println("Error trying to get text. assumed multipart content type with at least one plain text part");
        }
        return result;
    }

    private String getTextFromMessage(final Message message) {
        String result = null;
        try {
            result = getContentFromMessage(message, "text/plain");
        } catch (MessagingException | IOException e) {
            System.out.println("Error trying to get text. assumed multipart content type with at least one plain text part");
        }
        return result;
    }

    // todo remove throws from signature
    private String getContentFromMessage(final Message message, final String mimeType) throws IOException, MessagingException {
        String result = null;
        if (message.isMimeType("multipart/*")) {
            final MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            final int count = mimeMultipart.getCount();

            for (int i = 0; i < count; i++) {
                final BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.isMimeType(mimeType)) {
                    result = bodyPart.getContent().toString();
                    break;
                }
                if (bodyPart.isMimeType("multipart/ALTERNATIVE")) {
                    final Multipart mp = (Multipart) bodyPart.getContent();
                    final int count2 = mp.getCount();
                    for (int j = 0; j < count2; j++) {
                        final Part bp = mp.getBodyPart(j);
                        if (bp.isMimeType(mimeType)) {
                            result = (String) bp.getContent();
                            break;
                        }
                    }
                }
            }
        }
        if (message.isMimeType("text/plain")) {
            result = (String) message.getContent();
        }
        return result;
    }

}
