package modules.gmshopfull;

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
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.modules.GameModule;
import org.l2jmobius.gameserver.modules.ModuleContext;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * GM Shop Full module entry point. Registers nothing unless Enabled = True in config/module.ini.
 * <p>
 * The NPC is a normal Merchant, so players get the game's own buy window. config/shop.txt lists items as
 * Path|ItemId|Price, where the path is a menu such as Weapons/D Grade. Every path that has items becomes one buy
 * list, and every step of a path becomes a menu page, so the menu is built from the file. On enable the module writes
 * the buy lists into data/buylists and the opening page into data/html/merchant, then asks the server to reload its
 * buy lists so the shop is live in the same start.
 */
public class GmShopFullModule implements GameModule
{
	private static final int NPC_ID = 59930;
	private static final String MODULE_ID = "gm-shop-full";
	private static final String TITLE = "GM Shop Full";
	private static final String BYPASS = "gmshopfull";
	private static final int MAX_LISTS = 1000;
	private static final int MAX_NODES = 3000;

	/** One step of the menu. A node with items is also a buy list. */
	private static final class Node
	{
		final int id;
		final String name;
		final Node parent;
		final Map<String, Node> kids = new LinkedHashMap<>();
		final List<String> items = new ArrayList<>();
		int listId = -1;

		Node(int id, String name, Node parent)
		{
			this.id = id;
			this.name = name;
			this.parent = parent;
		}
	}

	@Override
	public void onEnable(ModuleContext context)
	{
		if (!context.config().getBoolean("Enabled", false))
		{
			return; // Switch off: register nothing, behave as stock.
		}

		final int pricePercent = Math.max(1, context.config().getInt("PricePercent", 100));

		// Read config/shop.txt: Path|ItemId|Price. Lines starting with # are comments.
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

		final Node root = new Node(0, "", null);
		final Map<Integer, Node> nodes = new LinkedHashMap<>();
		nodes.put(0, root);
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

				final int itemId = Integer.parseInt(fields[1].trim());
				final long price = Long.parseLong(fields[2].trim());
				if ((price < 0) || (ItemData.getInstance().getTemplate(itemId) == null))
				{
					throw new NumberFormatException("bad price or unknown item id");
				}

				final List<String> path = new ArrayList<>();
				for (String step : fields[0].split("/"))
				{
					if (!step.trim().isEmpty())
					{
						path.add(step.trim());
					}
				}

				if (path.isEmpty())
				{
					throw new NumberFormatException("empty path");
				}

				// Find or create the menu steps, but only keep them if the line is accepted.
				final List<Node> created = new ArrayList<>();
				Node node = root;
				for (String step : path)
				{
					Node next = node.kids.get(step);
					if (next == null)
					{
						if (nodes.size() >= MAX_NODES)
						{
							throw new NumberFormatException("too many menu pages");
						}

						next = new Node(nodes.size(), step, node);
						node.kids.put(step, next);
						nodes.put(next.id, next);
						created.add(next);
					}

					node = next;
				}

				if ((node.listId < 0) && (countLists(nodes) >= MAX_LISTS))
				{
					for (int i = created.size() - 1; i >= 0; i--)
					{
						final Node undo = created.get(i);
						undo.parent.kids.remove(undo.name);
						nodes.remove(undo.id);
					}

					throw new NumberFormatException("too many shop lists");
				}

				if (node.listId < 0)
				{
					node.listId = -2; // Marks "has a list"; the real id is given below.
				}

				final long unit = price == 0 ? 0 : Math.max(1, (price * pricePercent) / 100);
				final String itemName = ItemData.getInstance().getTemplate(itemId).getName().replace("--", "-");
				node.items.add("\t<item id=\"" + itemId + "\" price=\"" + unit + "\" /> <!-- " + itemName + " -->\n");
				items++;
			}
			catch (NumberFormatException e)
			{
				skipped++;
				context.logging().info(TITLE + ": skipped shop line '" + line + "' (" + e.getMessage() + ").");
			}
		}

		// Give every node that has items its buy list id.
		int lists = 0;
		for (Node node : nodes.values())
		{
			if (node.listId == -2)
			{
				node.listId = (NPC_ID * 1000) + lists;
				lists++;
			}
		}

		// Write one buy list per node with items, and the opening page.
		final Path buyListDir = Paths.get("data", "buylists");
		final Path pageFile = Paths.get("data", "html", "merchant", NPC_ID + ".htm");
		int changed = 0;
		try
		{
			for (Node node : nodes.values())
			{
				if (node.listId < 0)
				{
					continue;
				}

				final StringBuilder xml = new StringBuilder();
				xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				xml.append("<list xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"../xsd/buylist.xsd\">\n");
				xml.append("\t<npcs>\n\t\t<npc>").append(NPC_ID).append("</npc>\n\t</npcs>\n");
				for (String item : node.items)
				{
					xml.append(item);
				}

				xml.append("</list>\n");
				if (writeIfChanged(buyListDir.resolve(node.listId + ".xml"), xml.toString()))
				{
					changed++;
				}
			}

			// Remove lists left over from a path that no longer exists.
			for (int i = lists; i < MAX_LISTS; i++)
			{
				if (Files.deleteIfExists(buyListDir.resolve(((NPC_ID * 1000) + i) + ".xml")))
				{
					changed++;
				}
			}

			if (writeIfChanged(pageFile, render(root, "%objectId%", "Merchant:<br>\nWhat are you looking for?<br>\n")))
			{
				changed++;
			}
		}
		catch (IOException e)
		{
			context.logging().info(TITLE + ": could not write the shop files (" + e + "). Check that " + buyListDir.toAbsolutePath() + " and " + pageFile.getParent().toAbsolutePath() + " are writable.");
			return;
		}

		context.logging().info(TITLE + ": loaded " + items + " items in " + lists + " shop lists" + (skipped > 0 ? ", skipped " + skipped + " bad lines" : "") + ".");
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

		// The menu pages below the opening page are sent on demand.
		boolean hasMenus = false;
		for (Node node : nodes.values())
		{
			if (!node.kids.isEmpty() && (node != root))
			{
				hasMenus = true;
				break;
			}
		}

		if (hasMenus)
		{
			context.handlers().registerBypass(new IBypassHandler()
			{
				@Override
				public boolean onCommand(String command, Player player, Creature target)
				{
					// The player must be talking to this shop.
					if ((player == null) || !(target instanceof Npc) || (((Npc) target).getId() != NPC_ID))
					{
						return false;
					}

					Node page = root;
					final String[] parts = command.split(" ");
					if ((parts.length >= 3) && "menu".equals(parts[1]))
					{
						try
						{
							final Node found = nodes.get(Integer.parseInt(parts[2]));
							if ((found != null) && !found.kids.isEmpty())
							{
								page = found;
							}
						}
						catch (NumberFormatException e)
						{
							// Show the opening menu.
						}
					}

					final String intro = page == root ? "Merchant:<br>\nWhat are you looking for?<br>\n" : "";
					final NpcHtmlMessage message = new NpcHtmlMessage(target.getObjectId());
					message.setHtml(render(page, String.valueOf(target.getObjectId()), intro));
					player.sendPacket(message);
					return true;
				}

				@Override
				public String[] getCommandList()
				{
					return new String[]
					{
						BYPASS
					};
				}
			});
		}

		context.logging().info(TITLE + " module enabled, NPC " + NPC_ID + " active.");
	}

	private static int countLists(Map<Integer, Node> nodes)
	{
		int count = 0;
		for (Node node : nodes.values())
		{
			if (node.listId != -1)
			{
				count++;
			}
		}

		return count;
	}

	/**
	 * Builds one menu page: the way back, then a link for every step below this one. A step that only holds items
	 * opens the buy window directly.
	 */
	private static String render(Node page, String objectId, String intro)
	{
		final StringBuilder html = new StringBuilder();
		html.append("<html><body>").append(intro);
		if (page.parent != null)
		{
			html.append(escape(path(page))).append("<br>\n");
		}

		if (page.listId >= 0)
		{
			html.append("<a action=\"bypass -h npc_").append(objectId).append("_Buy ").append(page.listId).append("\">Buy all ").append(escape(page.name)).append(".</a><br>\n");
		}

		for (Node kid : page.kids.values())
		{
			if (kid.kids.isEmpty())
			{
				html.append("<a action=\"bypass -h npc_").append(objectId).append("_Buy ").append(kid.listId).append("\">").append(escape(kid.name)).append("</a><br>\n");
			}
			else
			{
				html.append("<a action=\"bypass -h ").append(BYPASS).append(" menu ").append(kid.id).append("\">").append(escape(kid.name)).append("</a><br>\n");
			}
		}

		if (page.kids.isEmpty() && (page.listId < 0))
		{
			html.append("There is nothing for sale right now.<br>\n");
		}

		if (page.parent != null)
		{
			html.append("<br>\n");
			html.append("<a action=\"bypass -h ").append(BYPASS).append(" menu ").append(page.parent.id).append("\">Back</a><br>\n");
			if (page.parent.parent != null)
			{
				html.append("<a action=\"bypass -h ").append(BYPASS).append(" menu 0\">Main menu</a><br>\n");
			}
		}

		html.append("</body></html>\n");
		return html.toString();
	}

	private static String path(Node node)
	{
		return node.parent == null || node.parent.parent == null ? node.name : path(node.parent) + " / " + node.name;
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
