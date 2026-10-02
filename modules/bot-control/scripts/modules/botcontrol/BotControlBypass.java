package modules.botcontrol;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.handler.ChatHandler;
import org.l2jmobius.gameserver.handler.IChatHandler;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.enums.ChatType;

/**
 * Handles bypass links sent by the Bot Control menu.
 * <p>
 * Each bypass token is a plain, single-token string with no semicolons, so {@code BypassHandler} can find it
 * either by exact match or by prefix in any L2J Mobius build. On use, the mapped text is routed through the
 * server's own party-chat handler ({@code ChatParty.onChat}), which broadcasts it to the party and then feeds
 * it to the Living World phantom brains. This makes a button press behave exactly like the player typing the
 * line into party chat.
 */
public class BotControlBypass implements IBypassHandler
{
	private final Map<String, String> _bypassToChat = new LinkedHashMap<>();
	private final Logger _log;
	
	public BotControlBypass(Logger log)
	{
		_log = log;
	}
	
	public void add(String bypass, String chatText)
	{
		_bypassToChat.put(bypass, chatText);
	}
	
	@Override
	public boolean onCommand(String command, Player player, Creature target)
	{
		if (command == null)
		{
			return false;
		}
		
		// Try exact match first, then prefix match (defensive: matches either style of BypassHandler).
		String chatText = _bypassToChat.get(command);
		if (chatText == null)
		{
			final String lower = command.toLowerCase();
			for (Map.Entry<String, String> e : _bypassToChat.entrySet())
			{
				if (lower.startsWith(e.getKey().toLowerCase()))
				{
					chatText = e.getValue();
					break;
				}
			}
		}
		
		if (chatText == null)
		{
			return false;
		}
		
		if (!player.isInParty())
		{
			player.sendMessage("You are not in a party. Bot control needs a party.");
			return true;
		}
		
		// Route through the stock party-chat handler, exactly as if the player typed the line. This is what
		// actually delivers the text to the Living World phantom brains (PhantomPartyManager / PhantomBuddyManager).
		final IChatHandler partyChat = ChatHandler.getInstance().getHandler(ChatType.PARTY);
		if (partyChat == null)
		{
			player.sendMessage("Party chat handler is not available on this server.");
			return true;
		}
		partyChat.onChat(ChatType.PARTY, player, null, chatText);
		
		if (_log != null)
		{
			_log.info("Bot Control: " + player.getName() + " sent party command \"" + chatText + "\"");
		}
		return true;
	}
	
	@Override
	public String[] getCommandList()
	{
		return _bypassToChat.keySet().toArray(new String[0]);
	}
}