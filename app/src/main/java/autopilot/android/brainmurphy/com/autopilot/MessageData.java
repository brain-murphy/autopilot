package autopilot.android.brainmurphy.com.autopilot;

import java.util.ArrayList;

/**
 * Created by connorrmounts on 11/14/14.
 */
public class MessageData {

    private ArrayList<MessageThread> threads;

    public MessageData() {
        threads = new ArrayList<MessageThread>();
    }

    public void addTextMessage(TextMessage txt) {
        boolean isNewThread = true;
        for (MessageThread thread : threads) {
            if (txt.getThreadID() == thread.getKey()) {
                isNewThread = false;
                thread.addMessage(txt);
                break;
            }
        }
        if (isNewThread) {
            MessageThread thread = new MessageThread(txt.getThreadID());
            thread.addMessage(txt);
            threads.add(thread);
        }
    }

    public void printThread(int threadID) {
        for (MessageThread thread : threads) {
            if (threadID == thread.getKey()) {
                thread.printThread();
                break;
            }
        }
    }

    public ArrayList<TextMessage> getMessages() {
        ArrayList<TextMessage> messages = new ArrayList<TextMessage>();
        for (MessageThread thread : threads) {
            messages.addAll(thread.getMessages());
        }
        return messages;
    }

    private class MessageThread {

        private ArrayList<TextMessage> messages;
        private int key;

        public MessageThread(int inKey) {
            messages = new ArrayList<TextMessage>();
            key = inKey;
        }

        public int getKey() {
            return key;
        }

        public void addMessage(TextMessage txt) {
            boolean isMostRecent = true;
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).getDate() > txt.getDate()) {
                    messages.add(i,txt);
                    isMostRecent = false;
                    break;
                }
            }
            if (isMostRecent) {
                messages.add(txt);
            }
        }

        public void printThread() {
            for (TextMessage txt : messages) {
                System.out.println(txt.getMessage());
            }
        }

        public ArrayList<TextMessage> getMessages() {
            return messages;
        }
    }
}
