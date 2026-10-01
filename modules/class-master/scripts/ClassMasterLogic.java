package modules.classmaster;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.l2jmobius.gameserver.config.PlayerConfig;
import org.l2jmobius.gameserver.data.enums.CategoryType;
import org.l2jmobius.gameserver.data.xml.ClassListData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.enums.player.PlayerClass;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;

/**
 * Dialogue and class change logic for the Class Master NPC. Plain class: the module entry point wires it to the
 * NPC-talk event and the bypass handler.
 */
public final class ClassMasterLogic
{
	/** NPC id, inside the range 59910-59919 reserved in module.json. */
	public static final int NPC_ID = 59910;

	/** The bypass command used by the dialogue buttons: "bypass -h classmaster set <classId>". */
	public static final String BYPASS = "classmaster";

	/** Level needed for the 1st, 2nd and 3rd profession change. */
	private static final int[] LEVELS =
	{
		20,
		40,
		76
	};

	private final int[] _prices;

	public ClassMasterLogic(int[] prices)
	{
		_prices = prices;
	}

	/**
	 * Which change the player is up for: 0 = 1st profession, 1 = 2nd, 2 = 3rd, -1 = none left.
	 */
	private static int tier(Player player)
	{
		if (player.isInCategory(CategoryType.FIRST_CLASS_GROUP))
		{
			return 0;
		}

		if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP))
		{
			return 1;
		}

		if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP))
		{
			return 2;
		}

		return -1;
	}

	private static String className(PlayerClass playerClass)
	{
		return ClassListData.getInstance().getClass(playerClass.getId()).getClassName();
	}

	private static String adena(int amount)
	{
		return amount <= 0 ? "Free" : NumberFormat.getIntegerInstance(Locale.US).format(amount) + " Adena";
	}

	private static String page(String body)
	{
		return "<html><body><center><br><font color=\"LEVEL\">Class Master</font><br><br>" + body + "</center></body></html>";
	}

	/**
	 * The dialogue page: the classes the player can change to, or why they can't yet.
	 */
	public String mainPage(Player player, String notice)
	{
		final String top = notice == null ? "" : "<font color=\"FF6666\">" + notice + "</font><br><br>";
		final int tier = tier(player);
		if (tier < 0)
		{
			return page(top + "You have reached your final profession.");
		}

		if (player.getLevel() < LEVELS[tier])
		{
			return page(top + "Come back at level " + LEVELS[tier] + " to change your class.");
		}

		final List<PlayerClass> options = new ArrayList<>();
		for (PlayerClass playerClass : PlayerClass.values())
		{
			if (playerClass.getParent() == player.getPlayerClass())
			{
				options.add(playerClass);
			}
		}

		if (options.isEmpty())
		{
			return page(top + "There is no class change available for you.");
		}

		final StringBuilder sb = new StringBuilder();
		sb.append(top);
		sb.append("Choose your new profession.<br>Cost: <font color=\"LEVEL\">").append(adena(_prices[tier])).append("</font><br><br>");
		for (PlayerClass playerClass : options)
		{
			sb.append("<a action=\"bypass -h ").append(BYPASS).append(" set ").append(playerClass.getId()).append("\">").append(className(playerClass)).append("</a><br>");
		}

		return page(sb.toString());
	}

	/**
	 * Charges the fee and changes the player's class. Every condition is checked again here, because the client
	 * can send any class id.
	 * @return the dialogue page to show next.
	 */
	public String change(Player player, int classId)
	{
		final int tier = tier(player);
		if ((tier < 0) || (player.getLevel() < LEVELS[tier]))
		{
			return mainPage(player, null);
		}

		final PlayerClass target = PlayerClass.getPlayerClass(classId);
		if ((target == null) || (target.getParent() != player.getPlayerClass()))
		{
			return mainPage(player, null);
		}

		final int price = _prices[tier];
		if (price > 0)
		{
			if (player.getInventory().getInventoryItemCount(Inventory.ADENA_ID, -1) < price)
			{
				return mainPage(player, "You need " + adena(price) + " for this class change.");
			}

			player.destroyItemByItemId(ItemProcessType.FEE, Inventory.ADENA_ID, price, player, true);
		}

		player.setPlayerClass(classId);
		if (player.isSubClassActive())
		{
			player.getSubClasses().get(player.getClassIndex()).setPlayerClass(player.getActiveClass());
		}
		else
		{
			player.setBaseClass(player.getActiveClass());
		}

		if (PlayerConfig.AUTO_LEARN_SKILLS)
		{
			player.giveAvailableSkills(PlayerConfig.AUTO_LEARN_FS_SKILLS, true, PlayerConfig.AUTO_LEARN_SKILLS_WITHOUT_ITEMS);
		}

		player.store(false); // Save now so a crash can't lose the new class or the fee.
		player.broadcastUserInfo();
		player.sendSkillList();
		player.sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));

		return mainPage(player, "You are now a " + className(target) + ".");
	}
}
