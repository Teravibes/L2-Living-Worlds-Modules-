package modules.craftmasterfree;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.l2jmobius.gameserver.data.xml.ItemData;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.modules.GameModule;
import org.l2jmobius.gameserver.modules.ModuleContext;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Craft Master (No Recipe): crafts any recipe from data/Recipes.xml with a 100% success rate.
 * Requires the recipe item (one per craft, used up): false. Registers nothing unless Enabled = True in config/module.ini.
 */
public class CraftMasterFreeModule implements GameModule
{
	private static final int NPC_ID = 59950;
	private static final String BYPASS = "craftfree";
	private static final boolean REQUIRE_RECIPE = false;
	private static final String MODULE_DIR = "craft-master-free";
	private static final String NPC_NAME = "Elara the Artisan";
	private static final String NPC_LINE = "Bring me the materials and I will make anything, and it never fails.";
	private static final int PAGE_SIZE = 10;
	private static final boolean ONE_PER_ITEM = true;

	private static final class Node
	{
		int id;
		String name;
		Node parent;
		List<Node> kids = new ArrayList<>();
		List<Recipe> recipes = new ArrayList<>();
	}

	private final List<Node> _nodes = new ArrayList<>();
	private Node _root;

	private static final class Recipe
	{
		int listId;
		int recipeItemId;
		String name;
		int craftLevel;
		int rate;
		Node node;
		String type;
		Map<Integer, Long> ingredients = new LinkedHashMap<>();
		int productId;
		long productCount;
		String productName = "";
	}

	private final Map<Integer, Recipe> _recipes = new LinkedHashMap<>();
	private int _materialPercent = 180;
	private long _adenaFee = 0;

	@Override
	public void onEnable(ModuleContext context)
	{
		if (!context.config().getBoolean("Enabled", false))
		{
			return; // Switch off: register nothing, behave as stock.
		}

		_materialPercent = Math.max(1, context.config().getInt("MaterialPercent", 180));
		_adenaFee = Math.max(0, context.config().getInt("AdenaFee", 0));

		loadRecipes(context);
		if (_recipes.isEmpty())
		{
			context.logging().info("Craft Master (No Recipe): no recipes loaded, module idle.");
			return;
		}

		installDialoguePage(context);

		context.handlers().registerBypass(new IBypassHandler()
		{
			@Override
			public boolean onCommand(String command, Player player, Creature target)
			{
				if ((player == null) || !(target instanceof Npc) || (((Npc) target).getId() != NPC_ID))
				{
					return false;
				}

				String html;
				try
				{
					html = handle(command.split(" "), player);
				}
				catch (Exception e)
				{
					html = menuPage(player, 0, "Something went wrong, please try again.");
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
					BYPASS
				};
			}
		});

		context.logging().info("Craft Master (No Recipe) enabled, NPC " + NPC_ID + " active, " + _recipes.size() + " recipes loaded.");
	}

	// ------------------------------------------------------------------ data

	private void loadRecipes(ModuleContext context)
	{
		final Path file = Paths.get("data", "Recipes.xml");
		try
		{
			final String xml = new String(Files.readAllBytes(file), "UTF-8");
			final Matcher items = Pattern.compile("<item\\s([^>]*)>(.*?)</item>", Pattern.DOTALL).matcher(xml);
			while (items.find())
			{
				final String attrs = items.group(1);
				final String body = items.group(2);
				final Recipe r = new Recipe();
				r.listId = Integer.parseInt(attr(attrs, "id"));
				r.recipeItemId = Integer.parseInt(attr(attrs, "recipeId"));
				r.name = attr(attrs, "name");
				r.craftLevel = Integer.parseInt(attr(attrs, "craftLevel"));
				r.rate = Integer.parseInt(attr(attrs, "successRate").isEmpty() ? "100" : attr(attrs, "successRate"));
				r.type = attr(attrs, "type");
				final Matcher ing = Pattern.compile("<ingredient\\s([^>]*)/>").matcher(body);
				while (ing.find())
				{
					r.ingredients.merge(Integer.parseInt(attr(ing.group(1), "id")), Long.parseLong(attr(ing.group(1), "count")), Long::sum);
				}
				// Some recipes list their own recipe item as an ingredient. It is handled separately, so drop it here.
				r.ingredients.remove(r.recipeItemId);
				final Matcher prod = Pattern.compile("<production\\s([^>]*)/>").matcher(body);
				if (!prod.find())
				{
					continue;
				}
				r.productId = Integer.parseInt(attr(prod.group(1), "id"));
				r.productCount = Long.parseLong(attr(prod.group(1), "count"));

				final Object template = ItemData.getInstance().getTemplate(r.productId);
				if (template == null)
				{
					continue; // Product not in this server's item data.
				}
				r.productName = ItemData.getInstance().getTemplate(r.productId).getName();
				_recipes.put(r.listId, r);
			}
			buildMenu(context);
		}
		catch (Exception e)
		{
			context.logging().info("Craft Master (No Recipe): could not read " + file.toAbsolutePath() + " (" + e + ").");
		}
	}

	private static String attr(String attrs, String name)
	{
		final Matcher m = Pattern.compile("\\b" + name + "=\"([^\"]*)\"").matcher(attrs);
		return m.find() ? m.group(1) : "";
	}

	/** Builds the menu tree from config/categories.txt (ProductId|Path|Name). */
	private void buildMenu(ModuleContext context) throws Exception
	{
		_nodes.clear();
		_root = newNode("Menu", null);
		final Map<Integer, String> paths = new LinkedHashMap<>();
		final Path file = Paths.get("modules", MODULE_DIR, "config", "categories.txt");
		if (Files.exists(file))
		{
			for (String line : Files.readAllLines(file, java.nio.charset.StandardCharsets.UTF_8))
			{
				final String s = line.trim();
				if (s.isEmpty() || s.startsWith("#"))
				{
					continue;
				}
				final String[] f = s.split("\\|");
				if (f.length >= 2)
				{
					try
					{
						paths.put(Integer.parseInt(f[0].trim()), f[1].trim());
					}
					catch (NumberFormatException e)
					{
						// Skip a bad line.
					}
				}
			}
		}
		else
		{
			context.logging().info("Craft Master (No Recipe): " + file.toAbsolutePath() + " not found, everything goes under 'Other'.");
		}

		// Offer each item once, from its lowest success rate recipe (the 60% one when there is one).
		final Map<Integer, Recipe> chosen = new LinkedHashMap<>();
		final List<Recipe> all = new ArrayList<>();
		for (Recipe r : _recipes.values())
		{
			if (ONE_PER_ITEM)
			{
				final Recipe old = chosen.get(r.productId);
				if ((old == null) || (r.rate < old.rate))
				{
					chosen.put(r.productId, r);
				}
			}
			else
			{
				all.add(r);
			}
		}
		if (ONE_PER_ITEM)
		{
			all.addAll(chosen.values());
		}

		// Put recipes in the order of categories.txt, then everything else.
		final List<Integer> order = new ArrayList<>(paths.keySet());
		all.sort((a, b) ->
		{
			final int ia = order.indexOf(a.productId);
			final int ib = order.indexOf(b.productId);
			return Integer.compare(ia < 0 ? Integer.MAX_VALUE : ia, ib < 0 ? Integer.MAX_VALUE : ib);
		});
		// Create the top-level nodes in categories.txt order first so the menu order follows the file.
		for (int pid : order)
		{
			node(paths.get(pid));
		}
		for (Recipe r : all)
		{
			final Node n = node(paths.getOrDefault(r.productId, "Other"));
			r.node = n;
			n.recipes.add(r);
		}
	}

	private Node newNode(String name, Node parent)
	{
		final Node n = new Node();
		n.id = _nodes.size();
		n.name = name;
		n.parent = parent;
		_nodes.add(n);
		if (parent != null)
		{
			parent.kids.add(n);
		}
		return n;
	}

	private Node node(String path)
	{
		Node cur = _root;
		for (String part : path.split("/"))
		{
			Node next = null;
			for (Node k : cur.kids)
			{
				if (k.name.equals(part))
				{
					next = k;
					break;
				}
			}
			cur = next != null ? next : newNode(part, cur);
		}
		return cur;
	}

	private int count(Node n)
	{
		int c = n.recipes.size();
		for (Node k : n.kids)
		{
			c += count(k);
		}
		return c;
	}

	// ------------------------------------------------------------------ dialogue

	private String handle(String[] p, Player player)
	{
		// p[0] = BYPASS
		final String action = p.length > 1 ? p[1] : "menu";
		switch (action)
		{
			case "list":
				return listPage(player, Integer.parseInt(p[2]), p.length > 3 ? Integer.parseInt(p[3]) : 0);
			case "view":
				return viewPage(player, Integer.parseInt(p[2]), Integer.parseInt(p[3]), null);
			case "craft":
				return craft(player, Integer.parseInt(p[2]), Math.max(1, Math.min(10, Integer.parseInt(p[3]))), Integer.parseInt(p[4]));
			default:
				return menuPage(player, p.length > 2 ? Integer.parseInt(p[2]) : 0, null);
		}
	}

	private String menuPage(Player player, int nodeId, String notice)
	{
		final Node n = (nodeId >= 0) && (nodeId < _nodes.size()) ? _nodes.get(nodeId) : _root;
		if (n.kids.isEmpty() && (n != _root))
		{
			return listPage(player, n.id, 0);
		}
		final StringBuilder sb = new StringBuilder("<html><body><center><font color=\"LEVEL\">").append(esc(path(n))).append("</font><br>");
		if (notice != null)
		{
			sb.append("<font color=\"FFCC00\">").append(esc(notice)).append("</font><br>");
		}
		if (n == _root)
		{
			sb.append(REQUIRE_RECIPE ? "Bring the recipe item and the materials and it never fails. Each craft uses up one recipe.<br>" : "Bring the materials and it never fails.<br>");
			if (_materialPercent != 100)
			{
				sb.append("Materials: ").append(_materialPercent).append("% of the 60% recipe (rounded down).<br>");
			}
			if (_adenaFee > 0)
			{
				sb.append("Fee per craft: ").append(_adenaFee).append(" Adena.<br>");
			}
		}
		sb.append("<br>");
		for (Node k : n.kids)
		{
			sb.append("<a action=\"bypass -h ").append(BYPASS).append(k.kids.isEmpty() ? " list " : " menu ").append(k.id).append(k.kids.isEmpty() ? " 0" : "").append("\">").append(esc(k.name)).append(" (").append(count(k)).append(")</a><br>");
		}
		if (n != _root)
		{
			sb.append("<br><a action=\"bypass -h ").append(BYPASS).append(" menu ").append(n.parent.id).append("\">Back</a>");
			if (n.parent != _root)
			{
				sb.append("  <a action=\"bypass -h ").append(BYPASS).append(" menu 0\">Main menu</a>");
			}
		}
		return sb.append("</center></body></html>").toString();
	}

	private static String path(Node n)
	{
		return n.parent == null ? "Craft Master" : (n.parent.parent == null ? n.name : path(n.parent) + " / " + n.name);
	}

	private String listPage(Player player, int nodeId, int page)
	{
		final Node n = (nodeId >= 0) && (nodeId < _nodes.size()) ? _nodes.get(nodeId) : _root;
		final List<Recipe> list = n.recipes;
		final int pages = Math.max(1, (list.size() + PAGE_SIZE - 1) / PAGE_SIZE);
		page = Math.max(0, Math.min(page, pages - 1));
		final StringBuilder sb = new StringBuilder("<html><body><center><font color=\"LEVEL\">").append(esc(path(n))).append("</font>");
		if (pages > 1)
		{
			sb.append(" (page ").append(page + 1).append("/").append(pages).append(")");
		}
		sb.append("<br>");
		for (int i = page * PAGE_SIZE; i < Math.min(list.size(), (page + 1) * PAGE_SIZE); i++)
		{
			final Recipe r = list.get(i);
			sb.append("<a action=\"bypass -h ").append(BYPASS).append(" view ").append(r.listId).append(" ").append(n.id).append(" ").append(page).append("\">").append(esc(r.productName));
			if (r.productCount > 1)
			{
				sb.append(" x").append(r.productCount);
			}
			if (REQUIRE_RECIPE && sameProduct(list, r))
			{
				sb.append(" (").append(r.rate).append("%)");
			}
			sb.append("</a><br1>");
		}
		sb.append("<br>");
		if (page > 0)
		{
			sb.append("<a action=\"bypass -h ").append(BYPASS).append(" list ").append(n.id).append(" ").append(page - 1).append("\">Previous</a>  ");
		}
		if (page < (pages - 1))
		{
			sb.append("<a action=\"bypass -h ").append(BYPASS).append(" list ").append(n.id).append(" ").append(page + 1).append("\">Next</a>");
		}
		sb.append("<br><a action=\"bypass -h ").append(BYPASS).append(" menu ").append(n.parent == null ? 0 : n.parent.id).append("\">Back</a>  <a action=\"bypass -h ").append(BYPASS).append(" menu 0\">Main menu</a></center></body></html>");
		return sb.toString();
	}

	/** True if another recipe in the list makes the same item. */
	private static boolean sameProduct(List<Recipe> list, Recipe r)
	{
		for (Recipe o : list)
		{
			if ((o != r) && (o.productId == r.productId))
			{
				return true;
			}
		}
		return false;
	}

	private String viewPage(Player player, int listId, int nodeId, String notice)
	{
		final Recipe r = _recipes.get(listId);
		if (r == null)
		{
			return menuPage(player, 0, "Unknown recipe.");
		}
		final int page = 0;
		final StringBuilder sb = new StringBuilder("<html><body><center><font color=\"LEVEL\">").append(esc(r.productName));
		if (r.productCount > 1)
		{
			sb.append(" x").append(r.productCount);
		}
		sb.append("</font><br>");
		if (notice != null)
		{
			sb.append("<font color=\"FFCC00\">").append(esc(notice)).append("</font><br>");
		}
		sb.append("Success rate: 100%<br>Materials per craft:<br1>");
		for (Map.Entry<Integer, Long> e : r.ingredients.entrySet())
		{
			final long need = need(e.getValue(), r);
			final long have = player.getInventory().getInventoryItemCount(e.getKey(), -1);
			final String color = have >= need ? "FFFFFF" : "FF4444";
			sb.append("<font color=\"").append(color).append("\">").append(esc(nameOf(e.getKey()))).append(" ").append(have).append("/").append(need).append("</font><br1>");
		}
		if (REQUIRE_RECIPE)
		{
			final long haveRecipe = player.getInventory().getInventoryItemCount(r.recipeItemId, -1);
			sb.append("<font color=\"").append(haveRecipe >= 1 ? "FFFFFF" : "FF4444").append("\">").append(esc(nameOf(r.recipeItemId))).append(" ").append(haveRecipe).append("/1 (used up)</font><br1>");
		}
		if (_adenaFee > 0)
		{
			final long have = player.getInventory().getInventoryItemCount(Inventory.ADENA_ID, -1);
			sb.append("<font color=\"").append(have >= _adenaFee ? "FFFFFF" : "FF4444").append("\">Adena fee ").append(_adenaFee).append("</font><br1>");
		}
		sb.append("<br>Craft: ");
		for (int n : new int[] {1, 5, 10})
		{
			sb.append("<a action=\"bypass -h ").append(BYPASS).append(" craft ").append(r.listId).append(" ").append(n).append(" ").append(nodeId).append("\">x").append(n).append("</a> ");
		}
		sb.append("<br><a action=\"bypass -h ").append(BYPASS).append(" list ").append(nodeId).append(" ").append(page).append("\">Back to list</a>  <a action=\"bypass -h ").append(BYPASS).append(" menu 0\">Main menu</a></center></body></html>");
		return sb.toString();
	}

	// ------------------------------------------------------------------ crafting

	private String craft(Player player, int listId, int times, int nodeId)
	{
		final Recipe r = _recipes.get(listId);
		if (r == null)
		{
			return menuPage(player, 0, "Unknown recipe.");
		}
		if (REQUIRE_RECIPE && (player.getInventory().getInventoryItemCount(r.recipeItemId, -1) < times))
		{
			return viewPage(player, listId, nodeId, "You need " + times + " of the recipe item for x" + times + ".");
		}
		// Check everything first so nothing is taken on a failed attempt.
		for (Map.Entry<Integer, Long> e : r.ingredients.entrySet())
		{
			if (player.getInventory().getInventoryItemCount(e.getKey(), -1) < (need(e.getValue(), r) * times))
			{
				return viewPage(player, listId, nodeId, "You do not have enough materials for x" + times + ".");
			}
		}
		if ((_adenaFee > 0) && (player.getInventory().getInventoryItemCount(Inventory.ADENA_ID, -1) < (_adenaFee * times)))
		{
			return viewPage(player, listId, nodeId, "You do not have enough Adena for x" + times + ".");
		}

		if (REQUIRE_RECIPE)
		{
			player.destroyItemByItemId(ItemProcessType.FEE, r.recipeItemId, times, player, true);
		}
		for (Map.Entry<Integer, Long> e : r.ingredients.entrySet())
		{
			player.destroyItemByItemId(ItemProcessType.FEE, e.getKey(), (int) (need(e.getValue(), r) * times), player, true);
		}
		if (_adenaFee > 0)
		{
			player.destroyItemByItemId(ItemProcessType.FEE, Inventory.ADENA_ID, (int) (_adenaFee * times), player, true);
		}
		player.addItem(processType(), r.productId, (int) (r.productCount * times), player, true);
		return viewPage(player, listId, nodeId, "Crafted " + (r.productCount * times) + " " + r.productName + ".");
	}

	/** Materials needed for one craft. Recipes that can fail (under 100%) use MaterialPercent, rounded down; 100% recipes use their normal amounts. */
	private long need(long base, Recipe r)
	{
		if (r.rate >= 100)
		{
			return base;
		}
		return Math.max(1, (base * _materialPercent) / 100);
	}

	private static ItemProcessType processType()
	{
		for (String n : new String[] {"CRAFT", "REWARD", "QUEST"})
		{
			try
			{
				return ItemProcessType.valueOf(n);
			}
			catch (Exception e)
			{
				// Try the next name.
			}
		}
		return ItemProcessType.values()[0];
	}

	private static String nameOf(int itemId)
	{
		final Object t = ItemData.getInstance().getTemplate(itemId);
		return t == null ? "Item " + itemId : ItemData.getInstance().getTemplate(itemId).getName();
	}

	private static String esc(String s)
	{
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	/** Writes the NPC's opening page to data/html/merchant, where the server reads a Merchant NPC's page. */
	private static void installDialoguePage(ModuleContext context)
	{
		final String page = "<html><body><center><br><font color=\"LEVEL\">%s</font><br><br>%s<br><br><a action=\"bypass -h " + BYPASS + " menu 0\">Craft something</a></center></body></html>";
		final Path target = Paths.get("data", "html", "merchant", NPC_ID + ".htm");
		try
		{
			final String content = String.format(page, NPC_NAME, NPC_LINE);
			if (Files.exists(target) && new String(Files.readAllBytes(target), "UTF-8").equals(content))
			{
				return; // Already installed and identical.
			}
			Files.createDirectories(target.getParent());
			Files.write(target, content.getBytes("UTF-8"));
			context.logging().info("Craft Master (No Recipe): wrote the dialogue page " + target.toAbsolutePath() + ".");
		}
		catch (Exception e)
		{
			context.logging().info("Craft Master (No Recipe): could not write the dialogue page " + target.toAbsolutePath() + " (" + e + "). Check that the folder is writable.");
		}
	}
}
