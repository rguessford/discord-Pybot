import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

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
        }
        Library.sendMessage(event.getChannel(), commandStr);
    }

	private void commandDice(MessageReceivedEvent event, List<String> argsList) {
		Random random = new Random();
		if (argsList.size() < 1){
			int n = random.nextInt(100) + 1;
			String message = String.valueOf(n) + "(1-100)";
			Library.sendMessage(event.getChannel(), message);
		}
	}
}
