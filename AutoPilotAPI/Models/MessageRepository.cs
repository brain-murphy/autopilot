using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace AutoPilotAPI.Models
{
    public class MessageRepository : IMessageRepository
    {
        private List<Message> Messages = new List<Message>();
        private int _nextId = 1;

        public MessageRepository()
        {
            Add(new Message("test1"));
            Add(new Message("test2"));
            Add(new Message("test3"));
        }

        public IEnumerable<Message> GetAll()
        {
            return Messages;
        }

        public Message Get(int id)
        {
            return Messages.Find(p => p.Id == id);
        }

        public Message Add(Message item)
        {
            if (item == null)
            {
                throw new ArgumentNullException("item");
            }
            Messages.Add(item);
            return item;
        }

        public void Remove(int id)
        {
            Messages.RemoveAll(p => p.Id == id);
        }

        public bool Update(Message item)
        {
            if (item == null)
            {
                throw new ArgumentNullException("item");
            }
            int index = Messages.FindIndex(p => p.Id == item.Id);
            if (index == -1)
            {
                return false;
            }
            Messages.RemoveAt(index);
            Messages.Add(item);
            return true;
        }
    }
}