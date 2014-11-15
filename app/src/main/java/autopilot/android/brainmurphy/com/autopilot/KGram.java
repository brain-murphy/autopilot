package autopilot.android.brainmurphy.com.autopilot;


public class KGram {
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