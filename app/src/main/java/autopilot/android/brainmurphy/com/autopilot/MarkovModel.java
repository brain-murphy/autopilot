package autopilot.android.brainmurphy.com.autopilot;


import android.util.Log;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.TreeMap;

public class MarkovModel implements java.io.Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -7519507685860833463L;
    private static final int ORDER = 10;
    private static final String DELIMITER = "\t";

    private int order;
    private TreeMap<String, KGram> kgrams = new TreeMap<String, KGram>();
    private TreeMap<String, ResponseSeed> inputOutputTree = new TreeMap<String, ResponseSeed>();


    private class ResponseSeed implements java.io.Serializable {
        /**
         *
         */
        private static final long serialVersionUID = -2132767376343325597L;

        private TreeMap<String, Integer> responses = new TreeMap<String, Integer>();
        private int totalResponses = 0;

        public void incrementResponse(String response) {
            totalResponses++;
            if(this.responses.get(response) == null) {
                this.responses.put(response, 1);
            } else {
                int currentCount = this.responses.get(response);
                this.responses.put(response, currentCount + 1);
            }
        }

        public String randomResponseSeed() {
            String[] potentialResponses = this.responses.keySet().toArray(new String[0]);
            double[] probabilities = new double[potentialResponses.length];

            for (int idx = 0; idx < potentialResponses.length; ++idx) {
                String s = potentialResponses[idx];

                probabilities[idx] = (double)this.responses.get(s) / this.totalResponses;
            }

            int index = StdRandom.discrete(probabilities);

            return potentialResponses[index];
        }
    }

    private class KGram implements java.io.Serializable {
        /**
         *
         */
        private static final long serialVersionUID = -8251866627520701342L;

        private int frequency = 0;
        private java.util.TreeMap<Character, Integer> charCounts
                = new java.util.TreeMap<Character, Integer>();
        private char[] possibleNextCharacters = null;
        private int charCount = 0;

        /*
         * Return the number of times this kgram appears in the sample text
         *
         * @return the frequency of this kgram
         */
        public int frequency() {
            return this.frequency;
        }

        public void incrementFrequency() {
            this.frequency++;
        }

        public void incrementCharacter(char c) {
            charCount++;
            if (this.charCounts.containsKey(c)) {
                int currentCount = this.charCounts.get(c);
                this.charCounts.put(c, currentCount + 1);
            } else {
                this.possibleNextCharacters = null;
                this.charCounts.put(c, 1);
            }
        }

        public int countForCharacter(char c) {
            if (this.charCounts.get(c) == null) {
                return 0;
            }

            return this.charCounts.get(c);
        }

        /*
         * Returns an *unsorted* array of the characters found after the
         *  given kgram.
         *
         * @return array of characters
         */
        public char[] possibleNextCharacters() {
            if (possibleNextCharacters == null) {
                Character[] chars = charCounts.keySet().toArray(new Character[0]);
                char[] toReturn = new char[chars.length];
                for (int idx = 0; idx < chars.length; ++idx) {
                    toReturn[idx] = chars[idx];
                }
                possibleNextCharacters = toReturn;
            }

            return possibleNextCharacters;
        }

        /*
         * Returns the total of all of the frequencies of the characters
         *  that have come after this kgram.
         */
        public int charCount() {
            return this.charCount;
        }
    }

    public MarkovModel(int order) {
        this.order = order;
    }

    private static MarkovModel buildModelFromTextMessages(ArrayList<TextMessage> messages) {
        MarkovModel model = new MarkovModel(ORDER);

        StringBuilder builder = new StringBuilder();
        for (TextMessage text : messages) {
            if(text.getIsSender()) {
                String string = text.getMessage();
                if (Character.isLetter(string.charAt(string.length() - 1))) {
                    string = string + ". ";
                }

                builder.append(string);
            }
        }

        model.addText(builder.toString());

        return model;
    }

    private void buildInputResponseModelFromTextMessages(ArrayList<TextMessage> messages) {
        String lastReceived = null;
        for (int idx = 0; idx < messages.size(); ++idx) {
            TextMessage message = messages.get(idx);

            if (message.getIsSender()) {
                if (lastReceived != null) {
                    String input = inputCodeForMessage(lastReceived);

                    String output;
                    if(message.getMessage().length() < ORDER) {
                        output = message.getMessage();
                    } else {
                        output = message.getMessage().substring(0, ORDER);
                    }

                    ResponseSeed seed = inputOutputTree.get(input);

                    if (seed == null) {
                        seed = new ResponseSeed();
                        inputOutputTree.put(input, seed);
                    }

                    seed.incrementResponse(output);
                }
            } else {
                lastReceived = message.getMessage();
            }
        }
    }
    
    private void buildInputResponseModelFromTextMessages(ArrayList<TextMessage> messages, String userPhoneNumber) {
        String lastReceived = null;
        for (int idx = 0; idx < messages.size(); ++idx) {
            TextMessage message = messages.get(idx);
            if (message.getIsSender()) {
                if (lastReceived != null) {
                    String input;
                    if (lastReceived.length() >= ORDER) {
                        input = (lastReceived.substring(0, ORDER / 2)
                                + lastReceived.substring(lastReceived.length() - ORDER / 2, lastReceived.length())).toLowerCase();
                    } else {
                        input = lastReceived;
                    }

                    String output;
                    if (message.getMessage().length() < ORDER) {
                        output = message.getMessage();
                    } else {
                        output = message.getMessage().substring(0, ORDER);
                    }

                    ResponseSeed seed = inputOutputTree.get(input);

                    if (seed == null) {
                        seed = new ResponseSeed();
                        inputOutputTree.put(input, seed);
                    }

                    seed.incrementResponse(output);
                }
            } else {
                lastReceived = message.getMessage();
            }
        }
    }

    public static MarkovModel personalityModelFromSentTextMessages(ArrayList<TextMessage> messages) {
        StdOut.println("Building personality model from " + messages.size() + " messages...");
        MarkovModel model = MarkovModel.buildModelFromTextMessages(messages);

        StdOut.println("Building input response model...");
        model.buildInputResponseModelFromTextMessages(messages);
        StdOut.println("Ready for input:");

        return model;
    }

    public static MarkovModel defaultModel() {
        String[] files = {"test-data/SMS.csv", "test-data/SMS2.csv"};

        return MarkovModel.personalityModelFromSentTextMessages(messagesFromFiles(files, "3363914954"));
    }

    public String responseForInput(String input) {
        String code = inputCodeForMessage(input);

        ResponseSeed seed = this.closestResponseSeed(code);
        String seedString = seed.randomResponseSeed();

        //StdOut.println("Creating response for input: " + input);

        if (seedString.length() < ORDER) {
            return seedString;
        } else {
            StringBuilder builder = new StringBuilder(seedString);

            String endLetters = ".!?";

            while (true) {
                char thisChar = this.rand(seedString);

                // http://stackoverflow.com/questions/1128723/in-java-how-can-i-test-if-an-array-contains-a-certain-value
                if(endLetters.indexOf(builder.charAt(builder.length() - 1)) >= 0) {
                    if (endLetters.indexOf(thisChar) == -1) {
                        break;
                    }
                }

                builder.append(thisChar);

                seedString = builder.substring(builder.length() - ORDER);
            }

            return builder.toString();
        }
    }

    private String inputCodeForMessage(String message) {
        if(message.length() < ORDER) {
            return message.toLowerCase();
        } else {
            return message.substring(0, ORDER).toLowerCase();
        }
    }

    public void addText(String text) {
        // process text and generate model
        if (text.length() <= this.order) {
            return;
        }

        text = text + text.substring(0, this.order);

        for (int idx = 0; idx < text.length() - this.order; ++idx) {
            String substring = text.substring(idx, idx + order);

            KGram kgram = this.kgrams.get(substring);

            if (kgram == null) {
                kgram = new KGram();
                this.kgrams.put(substring, kgram);
            }

            kgram.incrementFrequency();
            kgram.incrementCharacter(text.charAt(idx + order));
        }
    }

    private double probabilityOfNextChar(String kgram, char c) {
        KGram data = closestKGram(kgram);
        if (data == null) {
            return 0;
        }

        int total = data.charCount();
        return data.countForCharacter(c) / (double) total;
    }

    public ResponseSeed closestResponseSeed(String input) {
        ResponseSeed toReturn = this.inputOutputTree.get(input);
        if (toReturn != null) {
            return toReturn;
        } else {
        	StdOut.println("COULDN'T FIND INPUT IN DB");
        	
        	int minimumDistance = Integer.MAX_VALUE;
        	String closestKey = null;

        	for (String key : this.inputOutputTree.keySet()) {
        		int distance = Distance.distance(key, input);

        		if(distance < minimumDistance) {
        			minimumDistance = distance;
        			closestKey = key;
        		}
        	}
        	
        	if(closestKey != null) {
        		return this.inputOutputTree.get(closestKey);
        	}
        }

        return null;
    }

    public KGram closestKGram(String kgram) {
        KGram toReturn = this.kgrams.get(kgram);
        if (toReturn != null) {
            return toReturn;
        } else {
            int minimumDistance = Integer.MAX_VALUE;
            String closestKey = null;
            for (String key : this.kgrams.keySet()) {
                int distance = Distance.distance(key, kgram);
                if(distance < minimumDistance) {
                    minimumDistance = distance;
                    closestKey = key;
                }
            }

            if(closestKey != null) {
                return this.kgrams.get(closestKey);
            }
        }

        return null;
    }

    public int freq(String kgram) {
        return this.kgrams.get(kgram).frequency();
    }

    public int freq(String kgram, char c) {
        return this.kgrams.get(kgram).countForCharacter(c);
    }

    public char rand(String kgram) {
        KGram kgramData = this.kgrams.get(kgram);
        if(kgramData == null) {
            return '_';
        }
        char[] chars = kgramData.possibleNextCharacters();

        double[] probabilities = new double[chars.length];
        for (int idx = 0; idx < chars.length; ++idx) {
            char c = chars[idx];
            probabilities[idx] = probabilityOfNextChar(kgram, c);
        }

        int index = StdRandom.discrete(probabilities);
        return chars[index];
    }

    public String gen(String kgram, int length) {
        StringBuilder builder = new StringBuilder(length);
        builder.append(kgram);

        for (int idx = 0; idx < length - kgram.length(); ++idx) {
            String currentKgram
                    = builder.substring(builder.length() - kgram.length());

            char rand = rand(currentKgram);
            if(rand != '_') {
                builder.append(rand(currentKgram));
            }
        }

        return builder.toString();
    }

    private static ArrayList<TextMessage> messagesFromFiles(String[] files, String userPhoneNumber) {
        ArrayList<TextMessage> messages = new ArrayList<TextMessage>();

        for (String filepath : files) {
            In reader = new In(filepath);

            if(reader.exists()) {
                while (reader.hasNextLine()) {
                    String line = reader.readLine();

                    if (line.length() < DELIMITER.length() * 4) {
                        continue;
                    }

                    String[] components = line.split(DELIMITER);

                    if (components.length != 4) {
                        continue;
                    }

                    messages.add(new TextMessage(components[3], components[0], components[0].endsWith(userPhoneNumber)));
                }
            } else {
                StdOut.print("COULDN'T READ FROM READER");
            }
        }

        return messages;
    }

    public static void main(String[] args) {
        String[] files = {"test-data/SMS.csv", "test-data/SMS2.csv"};

        MarkovModel model = MarkovModel.personalityModelFromSentTextMessages(messagesFromFiles(files, "3363914954"));

        while(true) {
            StdOut.println("\n" + model.responseForInput(StdIn.readLine()) + "\n");
        }
    }
}