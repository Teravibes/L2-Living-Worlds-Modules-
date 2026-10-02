package modules.botsummon;

import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Opens one of the three summon menus: {@code .lfclass}, {@code .lfrole} or {@code .lfbuff}.
 * The actual shouting is done by {@link BotSummonBypass}.
 */
public class BotSummonVoiced implements IVoicedCommandHandler
{
	private final String _classCommand;
	private final String _roleCommand;
	private final String _buffCommand;
	private final String _classHtml;
	private final String _roleHtml;
	private final String _buffHtml;

	public BotSummonVoiced(String classCommand, String roleCommand, String buffCommand,
		String classHtml, String roleHtml, String buffHtml)
	{
		_classCommand = classCommand;
		_roleCommand = roleCommand;
		_buffCommand = buffCommand;
		_classHtml = classHtml;
		_roleHtml = roleHtml;
		_buffHtml = buffHtml;
	}

	@Override
	public boolean onCommand(String command, Player player, String params)
	{
		final String html;
		if (_classCommand.equalsIgnoreCase(command))
		{
			html = _classHtml;
		}
		else if (_roleCommand.equalsIgnoreCase(command))
		{
			html = _roleHtml;
		}
		else if (_buffCommand.equalsIgnoreCase(command))
		{
			html = _buffHtml;
		}
		else
		{
			return false;
		}

		final NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setHtml(html);
		player.sendPacket(msg);
		return true;
	}

	@Override
	public String[] getCommandList()
	{
		return new String[] { _classCommand, _roleCommand, _buffCommand };
	}
}