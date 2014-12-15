using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace AutoPilotAPI.App_Code
{
    public class StdRandom
    {
        static Random random = new Random();

        public static int discrete(double[] a)
        {
            double EPSILON = 1E-14;
            double sum = 0.0;
            for (int i = 0; i < a.Length; i++)
            {
                if (!(a[i] >= 0.0)) throw new Exception("array entry " + i + " must be nonnegative: " + a[i]);
                sum = sum + a[i];
            }
            if (sum > 1.0 + EPSILON || sum < 1.0 - EPSILON)
                throw new Exception("sum of array entries does not approximately equal 1.0: " + sum);

            // the for loop may not return a value when both r is (nearly) 1.0 and when the
            // cumulative sum is less than 1.0 (as a result of floating-point roundoff error)
            while (true)
            {
                double r = uniform();
                sum = 0.0;
                for (int i = 0; i < a.Length; i++)
                {
                    sum = sum + a[i];
                    if (sum > r) return i;
                }
            }
        }
        
        public static double uniform() {
            return random.NextDouble();
        }
    }

    
}