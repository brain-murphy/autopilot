using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Text;

namespace AutoPilotAPI.App_Code
{

    public class MarkovModel
    {
        private static readonly int ORDER = 10;
        private static readonly string DELIMITER = "\t";
        private static string[] stopwords = new string[1];
        private static int stopwordIndex = 0;
        private static MarkovModel defaultModel;

        private Dictionary<string, KGram> kgrams = new Dictionary<string, KGram>();
        private Dictionary<string, ResponseSeed> inputOutputTree = new Dictionary<string, ResponseSeed>();

        private class ResponseSeed
        {
            private Dictionary<string, int> responses = new Dictionary<string, int>();
            private int totalResponses = 0;

            public int averageLength()
            {
                int total = 0;

                foreach (int num in responses.ToArray().Select<KeyValuePair<string, int>, int>(
                    a => { return a.Value; }))
                {
                    total += num;
                }

                return total / responses.Count;
            }

            public void incrementResponse(string response)
            {
                totalResponses++;
                if (!this.responses.ContainsKey(response))
                {
                    this.responses[response] = 1;
                }
                else
                {
                    this.responses[response]++;
                }
            }

            public string randomResponseSeed()
            {
                string[] potentialResponses = this.responses.Select(a => { return a.Key; }).ToArray();
                double[] probabilities = new double[potentialResponses.Length];

                for (int idx = 0; idx < potentialResponses.Length; ++idx)
                {
                    string s = potentialResponses[idx];

                    probabilities[idx] = (double)this.responses[s] / this.totalResponses;
                }

                int index = StdRandom.discrete(probabilities);

                return potentialResponses[index];
            }
        }

        private class KGram
        {
            private int frequency = 0;
            private Dictionary<char, int> charCounts = new Dictionary<char, int>();
            private char[] possibleNextCharacters = null;
            private int charCount = 0;

            /*
             * Return the number of times this kgram appears in the sample text
             * 
             * @return the frequency of this kgram
             */
            public int getFrequency()
            {
                return this.frequency;
            }

            public void incrementFrequency()
            {
                this.frequency++;
            }

            public void incrementCharacter(char c)
            {
                charCount++;
                if (this.charCounts.ContainsKey(c))
                {
                    this.charCounts[c]++;
                }
                else
                {
                    this.possibleNextCharacters = null;
                    this.charCounts[c] = 1;
                }
            }

            public int countForCharacter(char c)
            {
                if (this.charCounts[c] == null)
                {
                    return 0;
                }

                return this.charCounts[c];
            }

            /*
             * Returns an *unsorted* array of the characters found after the 
             *  given kgram.
             * 
             * @return array of characters
             */
            public char[] getPossibleNextCharacters()
            {
                if (possibleNextCharacters == null)
                {
                    char[] chars = charCounts.Select(a => { return a.Key; }).ToArray();
                    char[] toReturn = new char[chars.Length];
                    for (int idx = 0; idx < chars.Length; ++idx)
                    {
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
            public int getCharCount()
            {
                return this.charCount;
            }
        }

        public MarkovModel()
        {
            string str = System.IO.File.ReadAllText(HttpContext.Current.Server.MapPath(@"~\App_Data\stopwords.txt"));
            stopwords = str.Split(new char[] { ' ', '\n' });
        }

        private static MarkovModel buildModelFromTextMessages(List<TextMessage> messages)
        {
            MarkovModel model = new MarkovModel();

            StringBuilder builder = new StringBuilder();
            foreach (TextMessage text in messages)
            {
                if (text.getIsSender())
                {
                    string str = text.getMessage();
                    if (str != "" && Char.IsLetter(str.Last()))
                    {
                        str +=". ";
                    }

                    builder.Append(str);
                }
            }

            model.addText(builder.ToString());

            return model;
        }

        private void buildInputResponseModelFromTextMessages(List<TextMessage> messages)
        {
            string lastReceived = null;
            for (int idx = 0; idx < messages.Count; ++idx)
            {
                TextMessage message = messages[idx];

                if (message.getIsSender())
                {
                    if (lastReceived != null)
                    {
                        string input = inputCodeForMessage(lastReceived);

                        string output;
                        if (message.getMessage().Length < ORDER)
                        {
                            output = message.getMessage();
                        }
                        else
                        {
                            output = message.getMessage().Substring(0, ORDER);
                        }

                        ResponseSeed seed;

                        if (inputOutputTree.ContainsKey(input))
                        {
                             seed = inputOutputTree[input];
                        } else
                        {
                            seed = new ResponseSeed();
                            inputOutputTree[input] = seed;
                        }

                        seed.incrementResponse(output);
                    }
                }
                else
                {
                    lastReceived = message.getMessage();
                }
            }
        }

        public static MarkovModel personalityModelFromSentTextMessages(List<TextMessage> messages)
        {
            Console.WriteLine("Building personality model from " + messages.Count + " messages...");
            MarkovModel model = MarkovModel.buildModelFromTextMessages(messages);

            Console.WriteLine("Building input response model...");
            model.buildInputResponseModelFromTextMessages(messages);
            Console.WriteLine("Ready for input:");

            return model;
        }

        public static MarkovModel getDefaultModel()
        {
            if (defaultModel != null)
            {
                return defaultModel;
            }

            string[] files = { HttpContext.Current.Server.MapPath("~/App_Data/SMS.csv") };

            defaultModel = MarkovModel.personalityModelFromSentTextMessages(messagesFromFiles(files, "3363914954"));

            return defaultModel;
        }

        public string responseForInput(string input)
        {
            string code = inputCodeForMessage(input);

            ResponseSeed seed = this.closestResponseSeed(code);
            string seedString = seed.randomResponseSeed();

            if (seedString.Length < ORDER)
            {
                return seedString;
            }
            else
            {
                StringBuilder builder = new StringBuilder(seedString);

                string endLetters = ".!?";

                int average = seed.averageLength();

                while (true)
                {
                    char thisChar = this.getRand(seedString);

                    if (builder.Length >= average)
                    {
                        if (endLetters.IndexOf(builder[builder.Length - 1]) >= 0)
                        {
                            if (endLetters.IndexOf(thisChar) == -1)
                            {
                                break;
                            }
                        }
                    }

                    builder.Append(thisChar);

                    seedString = builder.ToString().Substring(builder.Length - ORDER);
                }

                return builder.ToString();
            }
        }

        private string inputCodeForMessage(string message)
        {
            if (message.Length < ORDER)
            {
                return message.ToLower();
            }
            else
            {
                return message.Substring(0, ORDER).ToLower();
            }
        }

        public void addText(string text)
        {
            // process text and generate model
            if (text.Length <= ORDER)
            {
                return;
            }

            text = text + text.Substring(0, ORDER);

            for (int idx = 0; idx < text.Length - ORDER; ++idx)
            {
                string substring = text.Substring(idx, ORDER);
                KGram kgram;
                
                
                if (kgrams.ContainsKey(substring))
                {
                    kgram = this.kgrams[substring];
                } else
                {
                    kgram = new KGram();
                    this.kgrams[substring] = kgram;
                }

                kgram.incrementFrequency();
                kgram.incrementCharacter(text[idx + ORDER]);
                if (idx > 1000)
                {
                    Console.WriteLine("test");
                }
                if (idx > 100000)
                {
                    Console.WriteLine("test");
                }
                if (idx > 1000000)
                {

                    Console.WriteLine("test");
                }
            }
            Console.WriteLine("test");
        }

        private double probabilityOfNextChar(string kgram, char c)
        {
            KGram data = closestKGram(kgram);
            if (data == null)
            {
                return 0;
            }

            int total = data.getCharCount();
            return data.countForCharacter(c) / (double)total;
        }

        private ResponseSeed closestResponseSeed(string input)
        {
            if (this.inputOutputTree.ContainsKey(input))
            {
                return this.inputOutputTree[input];
            }
            else
            {
                Console.WriteLine("COULDN'T FIND INPUT IN DB");

                int minimumDistance = int.MaxValue;
                string closestKey = null;
                foreach (string key in this.inputOutputTree.Select(a => a.Key))
                {
                    int distance = Distance.distance(key, input);
                    if (distance < minimumDistance)
                    {
                        minimumDistance = distance;
                        closestKey = key;
                    }
                }

                if (closestKey != null)
                {
                    return this.inputOutputTree[closestKey];
                }
            }

            return null;
        }

        private KGram closestKGram(string kgram)
        {
            KGram toReturn = this.kgrams[kgram];
            if (toReturn != null)
            {
                return toReturn;
            }
            else
            {
                int minimumDistance = int.MaxValue;
                string closestKey = null;
                foreach (string key in this.kgrams.Select(a => a.Key))
                {
                    int distance = Distance.distance(key, kgram);
                    if (distance < minimumDistance)
                    {
                        minimumDistance = distance;
                        closestKey = key;
                    }
                }

                if (closestKey != null)
                {
                    return this.kgrams[closestKey];
                }
            }

            return null;
        }

        public int freq(string kgram)
        {
            return this.kgrams[kgram].getFrequency();
        }

        public int freq(string kgram, char c)
        {
            return this.kgrams[kgram].countForCharacter(c);
        }

        public char getRand(string kgram)
        {
            KGram kgramData = this.kgrams[kgram];
            if (kgramData == null)
            {
                return '_';
            }
            char[] chars = kgramData.getPossibleNextCharacters();

            double[] probabilities = new double[chars.Length];
            for (int idx = 0; idx < chars.Length; ++idx)
            {
                char c = chars[idx];
                probabilities[idx] = probabilityOfNextChar(kgram, c);
            }

            int index = StdRandom.discrete(probabilities);
            return chars[index];
        }

        public string gen(string kgram, int length)
        {
            StringBuilder builder = new StringBuilder(length);
            builder.Append(kgram);

            for (int idx = 0; idx < length - kgram.Length; ++idx)
            {
                string currentKgram
                = builder.ToString().Substring(builder.Length - kgram.Length);

                char rand = getRand(currentKgram);
                if (rand != '_')
                {
                    builder.Append(getRand(currentKgram));
                }
            }

            return builder.ToString();
        }

        private static List<TextMessage> messagesFromFiles(string[] files, string userPhoneNumber)
        {
            List<TextMessage> messages = new List<TextMessage>();

            foreach (string filepath in files)
            {
                string[] fileStrs = System.IO.File.ReadAllLines(filepath);

                foreach (string line in fileStrs)
                {

                    if (line.Length < DELIMITER.Length * 4)
                    {
                        continue;
                    }

                    string[] components = line.Split(new char[] { DELIMITER[0] });

                    if (components.Length != 4)
                    {
                        continue;
                    }

                    messages.Add(new TextMessage(components[3], components[0], components[0].EndsWith(userPhoneNumber)));
                }
            }

            return messages.Take(100000).ToList();
        }

        public static void main(string[] args)
        {
            MarkovModel.getDefaultModel();
        }
    }
}