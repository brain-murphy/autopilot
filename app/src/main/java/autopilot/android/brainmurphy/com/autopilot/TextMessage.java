package autopilot.android.brainmurphy.com.autopilot;

/**
 * Created by connorrmounts on 11/14/14.
 */
public class TextMessage {

    private String address;
    private String message;
    private double date;
    private int threadID;
    private boolean isSender;

    public TextMessage(String inAddress, String inMessage, double inDate, int inID,
                        boolean inSender) {
        address = inAddress;
        message = inMessage;
        date = inDate;
        threadID = inID;
        isSender = inSender;
    }

    public TextMessage(String inMessage, String inAddress, boolean inSender) {
        message = inMessage;
        address = inAddress;
        isSender = inSender;
    }

    public String getAddress() {
        return address;
    }

    public String getMessage() {
        return message;
    }

    public double getDate() {
        return date;
    }

    public int getThreadID() {
        return threadID;
    }

    public boolean getIsSender() {
        return isSender;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Address: ");
        builder.append(address);
        builder.append(" Message: ");
        builder.append(message);
        builder.append(" ID: ");
        builder.append(threadID);
        return builder.toString();
    }

    public String toString2() {
        StringBuilder builder = new StringBuilder();
        builder.append("Address: ");
        builder.append(address);
        builder.append(" Message: ");
        builder.append(message);
        return builder.toString();
    }
}
