package modules.botsummon;

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
 * Handles bypass links from the summon menus (.lfclass / .lfrole / .lfbuff).
 * <p>
 * Each bypass carries a ready-made {@code "LF 1 <something>"} line that is routed through the
 * server's own shout-chat handler, exactly as if the player had typed the shout. This is the same
 * path the Living World phantom recruiter listens on, so one button press = one bot on the way.
 */
public class BotSummonBypass implements IBypassHandler
{
	private final Map<String, String> _bypassToShout = new LinkedHashMap<>();
	private final Logger _log;

	public BotSummonBypass(Logger log)
	{
		_log = log;
	}

	public void add(String bypass, String shoutText)
	{
		_bypassToShout.put(bypass, shoutText);
	}

	@Override
	public boolean onCommand(String command, Player player, Creature target)
	{
		if (command == null)
		{
			return false;
		}

		// Exact match first, then prefix match - the same defensive approach used by BotControlBypass,
		// so it works whichever way BypassHandler looks up its keys on this build.
		String shout = _bypassToShout.get(command);
		if (shout == null)
		{
			final String lower = command.toLowerCase();
			for (Map.Entry<String, String> e : _bypassToShout.entrySet())
			{
				if (lower.startsWith(e.getKey().toLowerCase()))
				{
					shout = e.getValue();
					break;
				}
			}
		}
		if (shout == null)
		{
			return false;
		}

		final IChatHandler shoutHandler = ChatHandler.getInstance().getHandler(ChatType.SHOUT);
		if (shoutHandler == null)
		{
			player.sendMessage("Shout chat handler is not available on this server.");
			return true;
		}
		shoutHandler.onChat(ChatType.SHOUT, player, null, shout);

		if (_log != null)
		{
			_log.info("Bot Summon: " + player.getName() + " shouted \"" + shout + "\"");
		}
		return true;
	}

	@Override
	public String[] getCommandList()
	{
		return _bypassToShout.keySet().toArray(new String[0]);
	}
}