using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using AutoPilotAPI.Models;
using AutoPilotAPI.App_Code;
using ChatterBotAPI;

namespace AutoPilotAPI.Controllers
{
    public class MessageController : ApiController {
        static readonly IMessageRepository repository = new MessageRepository();

        static readonly Dictionary<string, ChatterBotSession> botSessions;
        static readonly ChatterBot bot;

        //static readonly MarkovModel markovModel;

        static MessageController()
        {
            ChatterBotFactory factory = new ChatterBotFactory();

            bot = factory.Create(ChatterBotType.CLEVERBOT);
            botSessions = new Dictionary<string, ChatterBotSession>();
            
            
            //Console.WriteLine("in static ctor");
            //markovModel = MarkovModel.getDefaultModel();

            
                //string[] fileStrs = System.IO.File.ReadAllLines(HttpContext.Current.Server.MapPath("~/App_Data/SMS.csv"));

                //List<string> texts = new List<string>();

                //for (int i = 0; i < 10000; i++)           
                //{
                //    string line = fileStrs[i];
                //    if (line.Length < 4)
                //    {
                //        continue;
                //    }

                //    string[] components = line.Split(new char[] { '\t' });

                //    if (components.Length != 4)
                //    {
                //        continue;
                //    }

                //    texts.Add(components[3]);
                //}

                //markovModel = new WordMarkovModel(texts);
        }

        /// <summary>
        /// Gets response message for input, using whatever model is currently active
        /// </summary>
        /// <param name="text">Incoming text to which the model will respond</param>
        /// <param name="uid">iteger id used to distinguish the user agent. Subject to change.</param>
        /// <example>
        /// Below is a sample query and response
        /// <code>
        /// autopilotapi.taptools.net/api/message?text="hey"&amp;uid=2132
        /// {Id:2132, Response:"what's up?"}
        /// </code>
        /// </example>
        /// <returns>JSON containing a Response and Id. The uid is the property passed as UID. The JSON keys are
        /// as mentioned above.</returns>
        /// 
        public Message GetMessage(string text, string uid)
        {
            Message m = new Message(text);
            //m.Response = markovModel.responseForInput(text);
            ChatterBotSession session;
            if (botSessions.ContainsKey(uid))
            {
                session = botSessions[uid];
            }
            else
            {
                session = bot.CreateSession();
                botSessions[uid] = session;
            }
            m.Response = session.Think(text);

            return m;
        }
    }
}
