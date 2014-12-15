using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Text;

namespace AutoPilotAPI.App_Code
{
    public class TextMessage
    {


        private String address;
        private String message;
        private double date;
        private int threadID;
        private bool isSender;

        public TextMessage(String inAddress, String inMessage, double inDate, int inID,
                            bool inSender)
        {
            address = inAddress;
            message = inMessage;
            date = inDate;
            threadID = inID;
            isSender = inSender;
        }

        public TextMessage(String inMessage, String inAddress, bool inSender)
        {
            message = inMessage;
            address = inAddress;
            isSender = inSender;
        }

        public String getAddress()
        {
            return address;
        }

        public String getMessage()
        {
            return message;
        }

        public double getDate()
        {
            return date;
        }

        public int getThreadID()
        {
            return threadID;
        }

        public bool getIsSender()
        {
            return isSender;
        }

        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.Append("Address: ");
            builder.Append(address);
            builder.Append(" Message: ");
            builder.Append(message);
            builder.Append(" ID: ");
            builder.Append(threadID);
            return builder.ToString();
        }
    }
}