package modules.gmshoplite;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.l2jmobius.gameserver.data.xml.BuyListData;
import org.l2jmobius.gameserver.data.xml.ItemData;
import org.l2jmobius.gameserver.modules.GameModule;
import org.l2jmobius.gameserver.modules.ModuleContext;

/**
 * GM Shop Lite module entry point. Registers nothing unless Enabled = True in config/module.ini.
 * <p>
 * The NPC is a normal Merchant, so players get the game's own buy window. On enable the module reads config/shop.txt
 * and writes one buy list per category into data/buylists, plus the NPC's dialogue page into data/html/merchant.
 * The server reads buy lists at startup, before modules are enabled, so after writing them the module asks the
 * server to reload its buy lists. That makes a new or changed shop live in the same start.
 */
public class GmShopLiteModule implements GameModule
{
	private static final int NPC_ID = 59920;
	private static final String MODULE_ID = "gm-shop-lite";
	private static final String TITLE = "GM Shop Lite";
	private static final int MAX_LISTS = 100;

	@Override
	public void onEnable(ModuleContext context)
	{
		if (!context.config().getBoolean("Enabled", false))
		{
			return; // Switch off: register nothing, behave as stock.
		}

		final int pricePercent = Math.max(1, context.config().getInt("PricePercent", 100));

		// Read config/shop.txt: Category|ItemId|Price. Lines starting with # are comments.
		final Path shop = Paths.get("modules", MODULE_ID, "config", "shop.txt");
		final List<String> lines;
		try
		{
			lines = Files.readAllLines(shop, StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			context.logging().info(TITLE + ": could not read " + shop.toAbsolutePath() + " (" + e + "). The shop will be empty.");
			return;
		}

		final Map<String, List<String>> categories = new LinkedHashMap<>();
		int items = 0;
		int skipped = 0;
		for (String raw : lines)
		{
			final String line = raw.trim();
			if (line.isEmpty() || line.startsWith("#"))
			{
				continue;
			}

			final String[] fields = line.split("\\|");
			try
			{
				if (fields.length < 3)
				{
					throw new NumberFormatException("too few fields");
				}

				final String category = fields[0].trim();
				final int itemId = Integer.parseInt(fields[1].trim());
				final long price = Long.parseLong(fields[2].trim());
				if (category.isEmpty() || (price < 0) || (ItemData.getInstance().getTemplate(itemId) == null))
				{
					throw new NumberFormatException("bad category, price or unknown item id");
				}

				if (!categories.containsKey(category) && (categories.size() >= MAX_LISTS))
				{
					throw new NumberFormatException("too many categories");
				}

				final long unit = price == 0 ? 0 : Math.max(1, (price * pricePercent) / 100);
				final String itemName = ItemData.getInstance().getTemplate(itemId).getName().replace("--", "-");
				List<String> list = categories.get(category);
				if (list == null)
				{
					list = new ArrayList<>();
					categories.put(category, list);
				}

				list.add("\t<item id=\"" + itemId + "\" price=\"" + unit + "\" /> <!-- " + itemName + " -->\n");
				items++;
			}
			catch (NumberFormatException e)
			{
				skipped++;
				context.logging().info(TITLE + ": skipped shop line '" + line + "' (" + e.getMessage() + ").");
			}
		}

		// Write one buy list per category, and the dialogue page that links to them.
		final Path buyListDir = Paths.get("data", "buylists");
		final Path pageFile = Paths.get("data", "html", "merchant", NPC_ID + ".htm");
		final StringBuilder page = new StringBuilder();
		page.append("<html><body>Provisioner:<br>\nShots, consumables and accessories, all in one place.<br>\n");
		int changed = 0;
		try
		{
			int index = 0;
			for (Map.Entry<String, List<String>> entry : categories.entrySet())
			{
				final int listId = (NPC_ID * 100) + index;
				final StringBuilder xml = new StringBuilder();
				xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				xml.append("<list xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"../xsd/buylist.xsd\">\n");
				xml.append("\t<npcs>\n\t\t<npc>").append(NPC_ID).append("</npc>\n\t</npcs>\n");
				for (String item : entry.getValue())
				{
					xml.append(item);
				}

				xml.append("</list>\n");
				if (writeIfChanged(buyListDir.resolve(listId + ".xml"), xml.toString()))
				{
					changed++;
				}

				page.append("<a action=\"bypass -h npc_%objectId%_Buy ").append(listId).append("\">Buy ").append(escape(entry.getKey())).append(".</a><br>\n");
				index++;
			}

			// Remove lists left over from a category that no longer exists.
			for (int i = index; i < MAX_LISTS; i++)
			{
				if (Files.deleteIfExists(buyListDir.resolve(((NPC_ID * 100) + i) + ".xml")))
				{
					changed++;
				}
			}

			if (categories.isEmpty())
			{
				page.append("There is nothing for sale right now.<br>\n");
			}

			page.append("</body></html>\n");
			if (writeIfChanged(pageFile, page.toString()))
			{
				changed++;
			}
		}
		catch (IOException e)
		{
			context.logging().info(TITLE + ": could not write the shop files (" + e + "). Check that " + buyListDir.toAbsolutePath() + " and " + pageFile.getParent().toAbsolutePath() + " are writable.");
			return;
		}

		context.logging().info(TITLE + ": loaded " + items + " items in " + categories.size() + " categories" + (skipped > 0 ? ", skipped " + skipped + " bad lines" : "") + ".");
		if (changed > 0)
		{
			context.logging().info(TITLE + ": wrote " + changed + " shop file(s) under " + Paths.get("data").toAbsolutePath() + ".");
			try
			{
				BuyListData.getInstance().load();
				context.logging().info(TITLE + ": reloaded the server's buy lists.");
			}
			catch (RuntimeException e)
			{
				context.logging().info(TITLE + ": could not reload the buy lists (" + e + "). Restart the server once to load them.");
			}
		}

		context.logging().info(TITLE + " module enabled, NPC " + NPC_ID + " active.");
	}

	/**
	 * Writes the file only when it is missing or different.
	 * @return true if the file was written.
	 */
	private static boolean writeIfChanged(Path file, String content) throws IOException
	{
		final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
		if (Files.exists(file) && Arrays.equals(Files.readAllBytes(file), bytes))
		{
			return false;
		}

		Files.createDirectories(file.getParent());
		Files.write(file, bytes);
		return true;
	}

	private static String escape(String text)
	{
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
