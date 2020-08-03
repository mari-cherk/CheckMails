package emailCode;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Data;

import static java.util.Objects.requireNonNull;

@Data
@AllArgsConstructor
public class EmailMessage {
    private String from;
    private String to;
    private String subject;
    private String bodyText;
    private String bodyHtml;

    public EmailMessage(final Consumer<EmailMessage> builder) {
        requireNonNull(builder).accept(this);
    }

}
