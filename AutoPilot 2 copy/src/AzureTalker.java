import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.table.*;
import com.microsoft.azure.storage.table.TableQuery.*;

import java.util.ArrayList;
import java.util.HashMap;

public class AzureTalker {
	public static class Message extends TableServiceEntity {
		public String message;
		public String address;
		public String recipient;
		public String modelType;
		
		public String getModelType() {
			return this.modelType;
		}
		
		public void setModelType(String s) {
			this.modelType = s;
		}
		
		public String getMessage() {
			return this.message;
		}
		
		public void setMessage(String m) {
			this.message = m;
		}
		
		public String getAddress() {
			return this.address;
		}
		
		public void setAddress(String a) {
			this.address = a;
		}
		
		public String getRecipient() {
			return this.recipient;
		}
		
		public void setRecipient(String r) {
			this.recipient = r;
		}
		
		public Message() {
			this.partitionKey = PARTITION;
			this.rowKey = "ROW";
		}
		
		public Message(String row) {
			this.partitionKey = PARTITION;
			this.rowKey = row;
			this.address = row;
		}
	}

	public static class Response extends TableServiceEntity {
		public String response;
		public String address;
		public String recipient;
		
		public String getResponse() {
			return this.response;
		}
		
		public void setResponse(String m) {
			this.response = m;
		}
		
		public String getAddress() {
			return this.address;
		}
		
		public void setAddress(String a) {
			this.address = a;
		}
		
		public String getRecipient() {
			return this.recipient;
		}
		
		public void setRecipient(String r) {
			this.recipient = r;
		}
		
		public Response() {
			this.partitionKey = PARTITION;
			this.rowKey = "ROW";
		}
		
		public Response(String row) {
			this.partitionKey = PARTITION;
			this.rowKey = row;
			this.address = row;
		}
	}

	public static final String storageConnectionString =
			"DefaultEndpointsProtocol=http;"
					+ "AccountName=connor;"
					+ "AccountKey=pY9dFj30ulxW0DF06SxdbC+4rezZsSllyVBAytd0qEFlh7gC221cdNa3Yi9COWN9hVd/dc394cIV9VNeeZQzBA==";

	static final String PARTITION = "partition";

	static final String PARTITION_KEY = "PartitionKey";
	static final String ROW_KEY = "RowKey";
	static final String TIMESTAMP = "Timestamp";
	HashMap<String, ChatterBot> bots = new HashMap<String, ChatterBot>();
	
	private String cleverbotResponse(String input, String userID) {
		
		try {
			ChatterBot bot = this.bots.get(userID);
			if(bot == null) {
				ChatterBotFactory factory = new ChatterBotFactory();
				bot = factory.create(ChatterBotType.CLEVERBOT);
				bots.put(userID, bot);
			}
			
			ChatterBotSession session = bot.createSession();
			
			String think = session.think(input);
			while(think.toLowerCase().contains("cleverbot")) {
				think = session.think(input);
			}
			
			return think;
		} catch (Exception e) {
			return null;
		}
	}

	public void checkDatabase() {
		new Thread() {
			public void run() {
				CloudTable messageTable;
				CloudTable responseTable;
				try {
					// Retrieve storage account from connection-string.
					CloudStorageAccount storageAccount =
							CloudStorageAccount.parse(storageConnectionString);
					
					// Create the table client.
					CloudTableClient tableClient = storageAccount.createCloudTableClient();

					messageTable = new CloudTable("messages", tableClient);
					messageTable.createIfNotExists();

					responseTable = new CloudTable("responses", tableClient);
					responseTable.createIfNotExists();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				
				ArrayList<Message> list = new ArrayList<Message>();

				System.out.println("Running...");
				while (true) {
					try {
						Thread.sleep(1500);
						
						String partitionFilter = TableQuery.generateFilterCondition(PARTITION_KEY, 
								QueryComparisons.EQUAL,
								PARTITION);

						// Specify a partition query, using PARTITION as the partition key filter.
						TableQuery<Message> partitionQuery = TableQuery.from(Message.class)
								.where(partitionFilter);
						
						// Loop through the results, displaying information about the entity.
						for (Message m : messageTable.execute(partitionQuery)) {
							for(Message in : list) {
								if(in.message.equals(m.message)) {
									StdOut.println("Ignoring: " + in.message);
									continue;
								}
							}
							
							list.add(m);
							
							String responseText = cleverbotResponse(m.message, m.getAddress());
							while(responseText == null) {
								responseText = cleverbotResponse(m.message, m.getAddress());
							}
							
							if(responseText.length() > 155) {
								responseText = responseText.substring(0, 160);
							}
							
							System.out.println("Received: " + m.getMessage());
							System.out.println("Response: " + responseText);
							
							Response response = new Response(m.getAddress());
							response.recipient = m.getRecipient();
							response.response = responseText;
							
							responseTable.execute(TableOperation.insertOrReplace(response));
							
							while(true) {
								try {
									messageTable.execute(TableOperation.delete(m));
									break;
								} catch (Exception e) {
									
								}
							}
							
							Thread.sleep(200);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	public static void main(String[] args) {
		MarkovModel.defaultModel();
		
		AzureTalker talker = new AzureTalker();
		talker.checkDatabase();
	}
}
