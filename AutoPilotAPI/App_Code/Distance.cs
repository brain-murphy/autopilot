using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace AutoPilotAPI.App_Code
{
    public class Distance
    {
        public static int distance(String a, String b)
        {
            a = a.ToLower();
            b = b.ToLower();
            // i == 0
            int[] costs = new int[b.Length + 1];
            for (int j = 0; j < costs.Length; j++)
                costs[j] = j;
            for (int i = 1; i <= a.Length; i++)
            {
                // j == 0; nw = lev(i - 1, j)
                costs[0] = i;
                int nw = i - 1;
                for (int j = 1; j <= b.Length; j++)
                {
                    int cj = Math.Min(1 + Math.Min(costs[j], costs[j - 1]), a[i - 1] == b[j - 1] ? nw : nw + 1);
                    nw = costs[j];

                    costs[j] = cj;
                }
            }

            return costs[b.Length];
        }
    }
}