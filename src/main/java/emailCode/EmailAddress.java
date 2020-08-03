package emailCode;

import java.util.Objects;

public class EmailAddress {
    private String from;

    public EmailAddress(final String from) {this.from = from; }

    public javax.mail.Address getAddressObject() {
        return new javax.mail.Address() {
            @Override
            public String getType() {
                return "from";
            }

            @Override
            public String toString() {
                return from;
            }

            @Override
            public boolean equals(final Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null || getClass() != obj.getClass()) {
                    return false;
                }
                final EmailAddress that = (EmailAddress) obj;
                return Objects.equals(from, that.from);
            }

            @Override
            public int hashCode() {
                return Objects.hash(from);
            }
        };
    }
}
