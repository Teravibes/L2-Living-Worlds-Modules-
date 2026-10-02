package modules.botcontrol;

import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Handles the voiced command (dot command) that opens the Bot Control menu.
 */
public class BotControlVoiced implements IVoicedCommandHandler
{
	private final String _command;
	private final String _html;
	
	public BotControlVoiced(String command, String html)
	{
		_command = command;
		_html = html;
	}
	
	@Override
	public boolean onCommand(String command, Player player, String params)
	{
		if (!_command.equalsIgnoreCase(command))
		{
			return false;
		}
		
		final NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setHtml(_html);
		player.sendPacket(msg);
		return true;
	}
	
	@Override
	public String[] getCommandList()
	{
		return new String[] { _command };
	}
}