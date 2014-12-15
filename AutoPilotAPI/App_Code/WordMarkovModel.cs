using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace AutoPilotAPI.App_Code
{
    public class WordMarkovModel
    {
        private Cluster<string[]>[] clusters;

        public WordMarkovModel(List<string> incomingTexts)
        {
            clusters = new Cluster<string[]>[incomingTexts.Count];
            string stopwordString = System.IO.File.ReadAllText(HttpContext.Current.Server.MapPath(@"~\App_Data\stopwords.txt"));
            string[] stopWords = stopwordString.Split(new char[] { ' ', '\n' }).Select( s => s.Trim().ToLower()).ToArray();

                for (int i = incomingTexts.Count - 1; i >= 0; i--)
                {
                    string[] words = incomingTexts.ElementAt(i).Split(new char[] { '.', ' ', ',', '?', '(', ')', '!' });
                    
                        words = words
                            .Distinct()
                            .Where(str =>
                            {
                                if (str.Length == 0) {
                                    return false;
                                }
                                foreach (string word in stopWords)
                                {
                                    if (word == str.Trim().ToLower()) {
                                        return false;
                                    }
                                }
                                return true;
                            })
                            .ToArray();
                    
                        clusters[i] = (new Cluster<string[]>(words, DistanceBetweenClusters));
                }
                clusters = clusters.Where(clust => clust.Get(0).Length > 0).ToArray();
            int startLength = clusters.Length;
            byte[,] pastVals = new byte[clusters.Length, clusters.Length];
            for (int i = 0; i < pastVals.GetLength(0); i++)
            {
                for (int j = 0; j < pastVals.GetLength(1); j++)
                {
                    pastVals[i, j] = byte.MaxValue;
                }
            }
            int itemsClustered = 0;
                while (itemsClustered < 1300)
                {
                    byte maxSimilarity = byte.MinValue;
                    int firstClusterI = 0;
                    int secondClusterI = 0;
                    for (int i = 0; i < clusters.Length; i++)
                    {
                        if (clusters[i] == null) {
                            continue;
                        }
                        for (int j = 0; j < clusters.Length; j++)
                        {
                            if (clusters[j] == null) {
                                continue;
                            }
                            if (i != j)
                            {
                                byte distance;
                                if (pastVals[i, j] == byte.MaxValue)
                                {
                                    pastVals[i, j] = (byte) DistanceBetweenClusters(clusters[i].Representative, clusters[j].Representative);
                                }

                                distance = pastVals[i, j];

                                if (distance > maxSimilarity)
                                {
                                    maxSimilarity = distance;
                                    firstClusterI = i;
                                    secondClusterI = j;
                                }
                            }
                        }
                    }

                    if (maxSimilarity < 20)
                    {
                        break;
                    }
                    itemsClustered++;
                    clusters[firstClusterI].Add(clusters[secondClusterI]);
                    clusters[secondClusterI] = null;
                    for (int i = 0; i < clusters.Length; i++)
                    {
                        if (clusters[i] != null)
                        {
                            pastVals[firstClusterI, i] = DistanceBetweenClusters(clusters[firstClusterI].Representative, clusters[i].Representative);
                            pastVals[i, firstClusterI] = DistanceBetweenClusters(clusters[i].Representative, clusters[firstClusterI].Representative);
                        }
                    }
                }
            int count = clusters.Select(e => e.Count).Aggregate((e, i) => { return e + i; });
        }

        public byte DistanceBetweenClusters(string[] first, string[] second)
        {
            //byte numInCommon = (byte)first.Count(str => second.Contains(str));
            //byte lengthDiff = (byte)(first.Length - second.Length);
            //return (byte)Math.Sqrt(Math.Pow(numInCommon, 2) + Math.Pow(lengthDiff, 2));
            return (byte)((first.Count(str => second.Contains(str)) / first.Length) * 100);
            //TODO implement levenshtien
        }








        private class Cluster<T> : IEnumerable<T>
        {
            public delegate byte DistanceMeasureDel(T first, T second);

            T[] backingArr;

            T representative;

            public T Representative
            {
                get { return representative; }
            }

            DistanceMeasureDel measurer;
 
            public int GetDistance(Cluster<T> other)
            {
                return measurer(representative, other.Representative);
            }

            public int Count
            {
                get { return backingArr.Length; }
            }

            public Cluster(T firstElem, DistanceMeasureDel del)
            {
                backingArr = new T[1];
                backingArr[0] = firstElem;
                measurer = del;
                representative = firstElem;
            }

            public void Add(Cluster<T> other)
            {
                backingArr = backingArr.Concat(other.backingArr).ToArray();

                representative = FindRepresentative();
            }

            public T Get(int index)
            {
                return backingArr[index];
            }

            private T FindRepresentative()
            {
                //calculate distances and reuse calculated values to find a central point//
                int[,] distances = new int[backingArr.Length, backingArr.Length];
                int maxSimilarity = int.MinValue;
                int index = 0;
                for (int i = 0; i < backingArr.Length; i++)
                {
                    int cumDistance = 0;
                    for (int j = 1; j < i; j++)
                    {
                        cumDistance += distances[j, i];
                    }
                    for (int j = i + 1; j < backingArr.Length; j++)
                    {
                        distances[i, j] = measurer(backingArr[i], backingArr[j]);
                        cumDistance += distances[i, j];
                    }
                    if (cumDistance > maxSimilarity)
                    {
                        maxSimilarity = cumDistance;
                        index = i;
                    }
                }

                return backingArr[index];
            }


            public IEnumerator<T> GetEnumerator()
            {
                return new BoxEnumerator(this);
            }

            System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
            {
                throw new NotImplementedException();
            }





            public class BoxEnumerator : IEnumerator<T>
            {
                private Cluster<T> _collection;
                private int curIndex;
                private T curBox;


                public BoxEnumerator(Cluster<T> collection)
                {
                    _collection = collection;
                    curIndex = -1;
                    curBox = default(T);

                }

                public bool MoveNext()
                {
                    //Avoids going beyond the end of the collection. 
                    if (++curIndex >= _collection.Count)
                    {
                        return false;
                    }
                    else
                    {
                        // Set current box to next item in collection.
                        curBox = _collection.Get(curIndex);
                    }
                    return true;
                }

                public void Reset() { curIndex = -1; }

                void IDisposable.Dispose() { }

                public T Current
                {
                    get { return curBox; }
                }


                object System.Collections.IEnumerator.Current
                {
                    get { return Current; }
                }

            }
        }
    }

    static class LevenshteinDistance
    {
        /// <summary>
        /// Compute the distance between two strings.
        /// </summary>
        public static int Compute(string s, string t)
        {
            int n = s.Length;
            int m = t.Length;
            int[,] d = new int[n + 1, m + 1];

            // Step 1
            if (n == 0)
            {
                return m;
            }

            if (m == 0)
            {
                return n;
            }

            // Step 2
            for (int i = 0; i <= n; d[i, 0] = i++)
            {
            }

            for (int j = 0; j <= m; d[0, j] = j++)
            {
            }

            // Step 3
            for (int i = 1; i <= n; i++)
            {
                //Step 4
                for (int j = 1; j <= m; j++)
                {
                    // Step 5
                    int cost = (t[j - 1] == s[i - 1]) ? 0 : 1;

                    // Step 6
                    d[i, j] = Math.Min(
                        Math.Min(d[i - 1, j] + 1, d[i, j - 1] + 1),
                        d[i - 1, j - 1] + cost);
                }
            }
            // Step 7
            return d[n, m];
        }
    }
}