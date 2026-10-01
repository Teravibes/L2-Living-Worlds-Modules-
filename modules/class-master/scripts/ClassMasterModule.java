package modules.classmaster;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.modules.GameModule;
import org.l2jmobius.gameserver.modules.ModuleContext;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Class Master module entry point. Registers nothing unless Enabled = True in config/module.ini.
 */
public class ClassMasterModule implements GameModule
{
	@Override
	public void onEnable(ModuleContext context)
	{
		if (!context.config().getBoolean("Enabled", false))
		{
			return; // Switch off: register nothing, behave as stock.
		}

		final int[] prices =
		{
			Math.max(0, context.config().getInt("PriceFirstProfession", 0)),
			Math.max(0, context.config().getInt("PriceSecondProfession", 1000000)),
			Math.max(0, context.config().getInt("PriceThirdProfession", 3000000))
		};
		final ClassMasterLogic logic = new ClassMasterLogic(prices);

		installDialoguePage(context);

		// Handle the dialogue buttons.
		context.handlers().registerBypass(new IBypassHandler()
		{
			@Override
			public boolean onCommand(String command, Player player, Creature target)
			{
				// The player must be talking to the class master.
				if ((player == null) || !(target instanceof Npc) || (((Npc) target).getId() != ClassMasterLogic.NPC_ID))
				{
					return false;
				}

				final String[] parts = command.split(" ");
				String html;
				if ((parts.length >= 3) && "set".equals(parts[1]))
				{
					try
					{
						html = logic.change(player, Integer.parseInt(parts[2]));
					}
					catch (NumberFormatException e)
					{
						html = logic.mainPage(player, null);
					}
				}
				else
				{
					html = logic.mainPage(player, null);
				}

				final NpcHtmlMessage message = new NpcHtmlMessage(target.getObjectId());
				message.setHtml(html);
				player.sendPacket(message);
				return true;
			}

			@Override
			public String[] getCommandList()
			{
				return new String[]
				{
					ClassMasterLogic.BYPASS
				};
			}
		});

		context.logging().info("Class Master module enabled, NPC " + ClassMasterLogic.NPC_ID + " active.");
	}

	/**
	 * Copies the NPC's opening page into the server's default html folder (the server only reads NPC pages from
	 * there). Only writes the file when it is missing or different. A new page is read at the next server start.
	 */
	private static void installDialoguePage(ModuleContext context)
	{
		final String name = ClassMasterLogic.NPC_ID + ".htm";
		final Path source = Paths.get("modules", "class-master", "html", name);
		final Path target = Paths.get("data", "html", "default", name);
		try
		{
			if (!Files.exists(source))
			{
				context.logging().info("Class Master: dialogue page not found at " + source.toAbsolutePath() + ", copy it to " + target.toAbsolutePath() + " by hand.");
				return;
			}

			if (Files.exists(target) && (Files.mismatch(source, target) == -1))
			{
				return; // Already installed and identical.
			}

			Files.createDirectories(target.getParent());
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			context.logging().info("Class Master: installed dialogue page " + target.toAbsolutePath() + ". Restart the server once to load it.");
		}
		catch (Exception e)
		{
			context.logging().info("Class Master: could not install the dialogue page (" + e + "). Copy " + source.toAbsolutePath() + " to " + target.toAbsolutePath() + " by hand.");
		}
	}
}
