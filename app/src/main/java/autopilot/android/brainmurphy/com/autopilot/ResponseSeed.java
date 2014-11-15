package autopilot.android.brainmurphy.com.autopilot;


import java.util.TreeMap;

public class ResponseSeed {
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
