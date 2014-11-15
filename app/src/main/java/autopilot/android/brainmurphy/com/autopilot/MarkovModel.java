package autopilot.android.brainmurphy.com.autopilot;


import android.util.Log;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.TreeMap;

public class MarkovModel {
	private static final int ORDER = 10;
	private static final String DELIMITER = "\t";
	
    private int order;
    private TreeMap<String, KGram> kgrams = new TreeMap<String, KGram>();
    private TreeMap<String, ResponseSeed> inputOutputTree = new TreeMap<String, ResponseSeed>();
    
    public MarkovModel(int order) {
    	this.order = order;
    }
    
    private static MarkovModel buildModelFromTextMessages(ArrayList<TextMessage> messages, String userPhoneNumber) {
    	MarkovModel model = new MarkovModel(ORDER);
    	
    	StringBuilder builder = new StringBuilder();
		for (TextMessage text : messages) {
			if(text.getAddress().endsWith(userPhoneNumber)) {
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
    
    public static MarkovModel personalityModelFromSentTextMessages(ArrayList<TextMessage> messages, String userPhoneNumber) {
    	StdOut.println("Building personality model from " + messages.size() + " messages...");
    	MarkovModel model = MarkovModel.buildModelFromTextMessages(messages, userPhoneNumber);
    	
    	StdOut.println("Building input response model...");
    	model.buildInputResponseModelFromTextMessages(messages, userPhoneNumber);
    	StdOut.println("Ready for input:");
		
		return model;
    }
    
    public String responseForInput(String input) {
    	ResponseSeed seed = this.closestResponseSeed(input);
    	String seedString = seed.randomResponseSeed();
    	
    	StdOut.println("Creating response for input: " + input);
    	
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
    
    public int order() {
        return this.order;
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
    
    public static void main(String[] args) {
    	In reader = new In("test-data/SMS.csv");
		
		ArrayList<TextMessage> list = new ArrayList<TextMessage>();
		
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

				list.add(new TextMessage(components[3], components[0], components[0].endsWith("3363914954")));
			}

			MarkovModel model = MarkovModel.personalityModelFromSentTextMessages(list, "3363914954");
			while(true) {
				StdOut.println("\n" + model.responseForInput(StdIn.readLine()) + "\n");
			}
		} else {
			StdOut.print("COULDN'T READ FROM READER");
		}
    }
}