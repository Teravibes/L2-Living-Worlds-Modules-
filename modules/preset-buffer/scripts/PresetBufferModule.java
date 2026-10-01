package modules.presetbuffer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.modules.GameModule;
import org.l2jmobius.gameserver.modules.ModuleContext;

/**
 * Preset Buffer module entry point. Registers nothing unless Enabled = True in config/module.ini.
 */
public class PresetBufferModule implements GameModule
{
	@Override
	public void onEnable(ModuleContext context)
	{
		if (!context.config().getBoolean("Enabled", false))
		{
			return; // Switch off: register nothing, behave as stock.
		}
		
		final int pricePerBuff = Math.max(0, context.config().getInt("PricePerBuff", 0));
		final int buffPrice = Math.max(0, context.config().getInt("BuffPrice", 0));
		final int healPrice = Math.max(0, context.config().getInt("HealPrice", 0));
		final boolean buffSummon = context.config().getBoolean("BuffSummon", true);
		final PresetBufferNpc presets = new PresetBufferNpc(pricePerBuff, buffSummon);
		final PresetBufferUi ui = new PresetBufferUi(presets, buffPrice, healPrice, buffSummon);
		
		installDialoguePage(context);
		
		// Handle the dialogue buttons.
		context.handlers().registerBypass(new IBypassHandler()
		{
			@Override
			public boolean onCommand(String command, Player player, Creature target)
			{
				return ui.handle(command, player, target);
			}
			
			@Override
			public String[] getCommandList()
			{
				return new String[]
				{
					PresetBufferNpc.BYPASS
				};
			}
		});
		
		context.logging().info("Preset Buffer module enabled, NPC " + PresetBufferNpc.NPC_ID + " active.");
	}
	
	/**
	 * Copies the NPC dialogue page into the server's default html folder (the server only reads NPC pages from
	 * there). Runs on every enable and only writes the file when it is missing or different, so edits to the module
	 * copy are picked up. A new or changed page is read at the next server start.
	 */
	private static void installDialoguePage(ModuleContext context)
	{
		final String name = PresetBufferNpc.NPC_ID + ".htm";
		final Path source = Paths.get("modules", "preset-buffer", "html", name);
		final Path target = Paths.get("data", "html", "default", name);
		try
		{
			if (!Files.exists(source))
			{
				context.logging().info("Preset Buffer: dialogue page not found at " + source.toAbsolutePath() + ", copy it to " + target.toAbsolutePath() + " by hand.");
				return;
			}
			
			if (Files.exists(target) && (Files.mismatch(source, target) == -1))
			{
				return; // Already installed and identical.
			}
			
			Files.createDirectories(target.getParent());
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			context.logging().info("Preset Buffer: installed dialogue page " + target.toAbsolutePath() + ". Restart the server once to load it.");
		}
		catch (Exception e)
		{
			context.logging().info("Preset Buffer: could not install the dialogue page (" + e + "). Copy " + source.toAbsolutePath() + " to " + target.toAbsolutePath() + " by hand.");
		}
	}
}
