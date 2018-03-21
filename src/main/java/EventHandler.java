import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAImage;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASubpod;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class EventHandler {
	
	@EventSubscriber
	public void readyEvent(ReadyEvent event) {
		IDiscordClient client = event.getClient(); // Gets the client from the event object
		IUser ourUser = client.getOurUser();// Gets the user represented by the client
		String name = ourUser.getName();// Gets the name of our user
		System.out.println("Logged in as " + name);
	}
	
	@EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event){
        String[] argArray = event.getMessage().getContent().split(" ");

        if(argArray.length == 0)
            return;

        if(!argArray[0].startsWith("/"))
            return;

        // Extract the "command" part of the first arg out by just ditching the first character
        String commandStr = argArray[0].substring(1);

        // Load the rest of the args in the array into a List for safer access
        List<String> argsList = new ArrayList<>(Arrays.asList(argArray));
        argsList.remove(0); // Remove the command
        
        switch (commandStr) {
        	case "dice":
        		commandDice(event, argsList);
        		break;
        	case "say":
        		commandSay(event, argsList);
        		break;
        	case "wolfram":
        		commandWolfram(event, argsList);
        		break;
        }
    }
	
	private void commandWolfram(MessageReceivedEvent event, List<String> argsList){
		String queryString = "";
		for (String string : argsList) {
			queryString += string + " ";
		}
		queryString = queryString.trim();
		String appid = "T6L59W-62ELH955EP";
		WAEngine engine = new WAEngine();
		engine.setAppID(appid);
		engine.addFormat("image");
		WAQuery query = engine.createQuery(queryString);
		 try {
	            // For educational purposes, print out the URL we are about to send:
	            System.out.println("Query URL:");
	            System.out.println(engine.toURL(query));
	            System.out.println("");
	            
	            // This sends the URL to the Wolfram|Alpha server, gets the XML result
	            // and parses it into an object hierarchy held by the WAQueryResult object.
	            WAQueryResult queryResult = engine.performQuery(query);
	            
	            if (queryResult.isError()) {
	                System.out.println("Query error");
	                System.out.println("  error code: " + queryResult.getErrorCode());
	                System.out.println("  error message: " + queryResult.getErrorMessage());
	            } else if (!queryResult.isSuccess()) {
	            	Library.sendMessage(event.getChannel(), "Query was not understood; no results available.");
	            } else {
	                // Got a result.
	                System.out.println("Successful query. Pods follow:\n");
	                
	                for (WAPod pod : queryResult.getPods()) {
	                    if (!pod.isError()) {
	                        for (WASubpod subpod : pod.getSubpods()) {
	                            for (Object element : subpod.getContents()) {
	                                if (element instanceof WAImage) {
	                                	EmbedBuilder builder = new EmbedBuilder();
	                                	builder.withImage(((WAImage) element).getURL());
	                                	event.getChannel().sendMessage(builder.build());
	                                }
	                            }
	                        }
	                    }
	                }
	            
	            }
	        } catch (WAException e) {
	            e.printStackTrace();
	        }
	}
	


	private void commandSay(MessageReceivedEvent event, List<String> argsList){
		StringBuilder messageBuilder = new StringBuilder();
		for (String string : argsList) {
			messageBuilder.append(string + " ");
		}
		Library.sendMessage(event.getChannel(), messageBuilder.toString());
	}
	
	private void commandDice(MessageReceivedEvent event, List<String> argsList) {
		
		Random random = new Random();
		for(String aString:argsList){
			try{
				new BigInteger(aString);
			}catch (NumberFormatException e) {
				Library.sendMessage(event.getChannel(), "/dice: /dice [max [times]]");
				return;
			}
		}
		
		if (argsList.size() < 1){
			int n = random.nextInt(100) + 1;
			String message = String.valueOf(n) + " (1-100)";
			Library.sendMessage(event.getChannel(), message);
		} else if (argsList.size() == 1) {
			BigInteger n = nextRandomBigInteger(new BigInteger(argsList.get(0))).add(BigInteger.ONE);
			String message = String.valueOf(n) + " (1-"+argsList.get(0)+")";
			Library.sendMessage(event.getChannel(), message);
		} else if (argsList.size() == 2) {
			BigInteger total = BigInteger.ZERO;
			BigInteger numDice = new BigInteger(argsList.get(1));
			BigInteger range = new BigInteger(argsList.get(0));
			long timeOutStart = System.currentTimeMillis();
			for (BigInteger i = BigInteger.ZERO; i.compareTo(numDice) < 0; i = i.add(BigInteger.ONE)) {
				if (System.currentTimeMillis() - timeOutStart > 10000){
					Library.sendMessage(event.getChannel(), "I've been rolling these " + range + " sided dice for 10 seconds and I'm giving up. I've rolled "+ i + " dice so far totalling " +total+ ", but that's all you get");
					return;
				}
				total = total.add(nextRandomBigInteger(range));
			}
			String message = String.valueOf(total) + " (1-"+range+")x"+numDice;
			Library.sendMessage(event.getChannel(), message);
		}
	}
	
	public BigInteger nextRandomBigInteger(BigInteger n) {
	    Random rand = new Random();
	    BigInteger result = new BigInteger(n.bitLength(), rand);
	    while( result.compareTo(n) >= 0 ) {
	        result = new BigInteger(n.bitLength(), rand);
	    }
	    return result;
	}
	
}
