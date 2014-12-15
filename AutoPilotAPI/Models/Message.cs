using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace AutoPilotAPI.Models
{
    
        public class Message
        {
            private static int idCounter = 0;


            public Message(string incomingText)
            {
                this.incomingText = incomingText;
                id = idCounter++;
            }

            int id;
            public int Id
            {
                get { return id; }
            }

            public string Response
            {
                get;
                set;
            }

            string incomingText;
            public string IncomingText
            {
                set { incomingText = value; }
            }
        }
}