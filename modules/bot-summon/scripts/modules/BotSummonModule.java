package modules.botsummon;

import org.l2jmobius.gameserver.modules.GameModule;
import org.l2jmobius.gameserver.modules.ModuleContext;

/**
 * Bot Summon module.
 * <p>
 * Registers three voiced commands and one bypass handler:
 * <ul>
 * <li>{@code .lfclass} - menu of specific Lineage 2 classes.</li>
 * <li>{@code .lfrole} - menu of generic roles (Tank, Heal, melee/ranged DD, Mage, Summoner, Mana Battery, Buffer, Spoiler).</li>
 * <li>{@code .lfbuff} - menu of level-80 support (Buffer, Singer, Dancer).</li>
 * </ul>
 * Every button shouts {@code "LF 1 <something>"} through the server's own shout-chat handler,
 * exactly as if the player had typed the shout. One press = one bot.
 */
public class BotSummonModule implements GameModule
{
	@Override
	public void onEnable(ModuleContext context)
	{
		if (!context.config().getBoolean("Enabled", false))
		{
			return;
		}

		final String classCommand = context.config().getString("ClassMenuCommand", "lfclass");
		final String roleCommand = context.config().getString("RoleMenuCommand", "lfrole");
		final String buffCommand = context.config().getString("BuffMenuCommand", "lfbuff");

		final BotSummonBypass bypass = new BotSummonBypass(context.logging());

		// ===== .lfclass - one button per class =====
		// Tanks
		bypass.add("_lfclass_pal", "LF 1 Paladin");
		bypass.add("_lfclass_da",  "LF 1 Dark Avenger");
		bypass.add("_lfclass_tk",  "LF 1 Temple Knight");
		bypass.add("_lfclass_sk",  "LF 1 Shillien Knight");
		// Support
		bypass.add("_lfclass_ee",  "LF 1 Elven Elder");
		bypass.add("_lfclass_se",  "LF 1 Shillien Elder");
		bypass.add("_lfclass_bp",  "LF 1 Bishop");
		bypass.add("_lfclass_pp",  "LF 1 Prophet");
		bypass.add("_lfclass_wc",  "LF 1 Warcryer");
		bypass.add("_lfclass_ol",  "LF 1 Overlord");
		bypass.add("_lfclass_sws", "LF 1 Sword Singer");
		bypass.add("_lfclass_bd",  "LF 1 Bladedancer");
		// Melee
		bypass.add("_lfclass_glad", "LF 1 Gladiator");
		bypass.add("_lfclass_wl",   "LF 1 Warlord");
		bypass.add("_lfclass_th",   "LF 1 Treasure Hunter");
		bypass.add("_lfclass_aw",   "LF 1 Abyss Walker");
		bypass.add("_lfclass_pl",   "LF 1 Plainswalker");
		bypass.add("_lfclass_dest", "LF 1 Destroyer");
		bypass.add("_lfclass_tyr",  "LF 1 Tyrant");
		// Archers
		bypass.add("_lfclass_he", "LF 1 Hawkeye");
		bypass.add("_lfclass_sr", "LF 1 Silver Ranger");
		bypass.add("_lfclass_pw", "LF 1 Phantom Ranger");
		// Mages
		bypass.add("_lfclass_sor", "LF 1 Sorcerer");
		bypass.add("_lfclass_nec", "LF 1 Necromancer");
		bypass.add("_lfclass_sps", "LF 1 Spellsinger");
		bypass.add("_lfclass_sph", "LF 1 Spellhowler");
		// Summoners
		bypass.add("_lfclass_wlk", "LF 1 Warlock Summoner");
		bypass.add("_lfclass_ele", "LF 1 Elemental Summoner");
		bypass.add("_lfclass_ps",  "LF 1 Phantom Summoner");
		// Dwarves
		bypass.add("_lfclass_bh", "LF 1 Bounty Hunter");
		bypass.add("_lfclass_ws", "LF 1 Warsmith");

		// ===== .lfrole - generic roles =====
		// Order: Tank -> Heal -> DD (melee, ranged, mage) -> Summoner -> Mana Battery -> Buff -> Spoil
		bypass.add("_lfrole_tank",   "LF 1 Tank");
		bypass.add("_lfrole_heal",   "LF 1 Healer");
		bypass.add("_lfrole_mdd",    "LF 1 Melee DD");
		bypass.add("_lfrole_rdd",    "LF 1 Ranged DD");
		bypass.add("_lfrole_mage",   "LF 1 Mage");
		bypass.add("_lfrole_summon", "LF 1 Summoner");
		bypass.add("_lfrole_battery","LF 1 SE");
		bypass.add("_lfrole_buff",   "LF 1 Buffer");
		bypass.add("_lfrole_spoil",  "LF 1 Spoiler");

		// ===== .lfbuff - level-80 support =====
		bypass.add("_lfbuff_buff",    "LF 1 Buffer level 80");
		bypass.add("_lfbuff_singer",  "LF 1 Singer level 80");
		bypass.add("_lfbuff_dancer",  "LF 1 Dancer level 80");

		context.handlers().registerBypass(bypass);
		context.handlers().registerVoicedCommand(new BotSummonVoiced(
			classCommand, roleCommand, buffCommand,
			buildClassMenuHtml(), buildRoleMenuHtml(), buildBuffMenuHtml()));

		context.logging().info("Bot Summon Menus enabled. Voiced commands: ." + classCommand
			+ ", ." + roleCommand + ", ." + buffCommand
			+ " (" + bypass.getCommandList().length + " buttons)");
	}

	// ===== HTML builders =====

	/**
	 * Class menu. Single column, top to bottom, with a section header before each group.
	 * Sections, in order: Tanks, Support, Melee, Archers, Mages, Summoners, Dwarves.
	 */
	private static String buildClassMenuHtml()
	{
		final StringBuilder sb = new StringBuilder(4096);
		sb.append("<html><body>");
		sb.append("<center><font color=\"LEVEL\">Summon a Class</font><br1>");
		sb.append("<font color=\"808080\">One press = one shout, one bot.</font><br>");

		sb.append("<font color=\"LEVEL\">Tanks</font><br>");
		sb.append(button("Paladin",         "_lfclass_pal"));
		sb.append(button("Dark Avenger",    "_lfclass_da"));
		sb.append(button("Temple Knight",   "_lfclass_tk"));
		sb.append(button("Shillien Knight", "_lfclass_sk"));

		sb.append("<br><font color=\"LEVEL\">Support</font><br>");
		sb.append(button("Bishop",         "_lfclass_bp"));
		sb.append(button("Prophet",        "_lfclass_pp"));
		sb.append(button("Elven Elder",    "_lfclass_ee"));
		sb.append(button("Shillien Elder", "_lfclass_se"));
		sb.append(button("Sword Singer",   "_lfclass_sws"));
		sb.append(button("Bladedancer",    "_lfclass_bd"));
		sb.append(button("Warcryer",       "_lfclass_wc"));
		sb.append(button("Overlord",       "_lfclass_ol"));

		sb.append("<br><font color=\"LEVEL\">Melee</font><br>");
		sb.append(button("Gladiator",       "_lfclass_glad"));
		sb.append(button("Warlord",         "_lfclass_wl"));
		sb.append(button("Treasure Hunter", "_lfclass_th"));
		sb.append(button("Abyss Walker",    "_lfclass_aw"));
		sb.append(button("Plainswalker",    "_lfclass_pl"));
		sb.append(button("Destroyer",       "_lfclass_dest"));
		sb.append(button("Tyrant",          "_lfclass_tyr"));

		sb.append("<br><font color=\"LEVEL\">Archers</font><br>");
		sb.append(button("Hawkeye",        "_lfclass_he"));
		sb.append(button("Silver Ranger",  "_lfclass_sr"));
		sb.append(button("Phantom Ranger", "_lfclass_pw"));

		sb.append("<br><font color=\"LEVEL\">Mages</font><br>");
		sb.append(button("Sorcerer",    "_lfclass_sor"));
		sb.append(button("Necromancer", "_lfclass_nec"));
		sb.append(button("Spellsinger", "_lfclass_sps"));
		sb.append(button("Spellhowler", "_lfclass_sph"));

		sb.append("<br><font color=\"LEVEL\">Summoners</font><br>");
		sb.append(button("Warlock",            "_lfclass_wlk"));
		sb.append(button("Elemental Summoner", "_lfclass_ele"));
		sb.append(button("Phantom Summoner",   "_lfclass_ps"));

		sb.append("<br><font color=\"LEVEL\">Dwarves</font><br>");
		sb.append(button("Bounty Hunter", "_lfclass_bh"));
		sb.append(button("Warsmith",      "_lfclass_ws"));

		sb.append("</center></body></html>");
		return sb.toString();
	}

	/** Role menu. Single column, order Tank -> Heal -> DD -> Summoner -> Battery -> Buff -> Spoil. */
	private static String buildRoleMenuHtml()
	{
		final StringBuilder sb = new StringBuilder(1024);
		sb.append("<html><body><center>");
		sb.append("<br1><font color=\"LEVEL\">Summon a Role</font><br1>");
		sb.append("<font color=\"808080\">One press = one shout, one bot.</font><br><br>");
		sb.append(button("Tank",         "_lfrole_tank"));
		sb.append(button("Healer",       "_lfrole_heal"));
		sb.append(button("Melee DD",     "_lfrole_mdd"));
		sb.append(button("Ranged DD",    "_lfrole_rdd"));
		sb.append(button("Mage",         "_lfrole_mage"));
		sb.append(button("Summoner",     "_lfrole_summon"));
		sb.append(button("Mana Battery", "_lfrole_battery"));
		sb.append(button("Buffer",       "_lfrole_buff"));
		sb.append(button("Spoiler",      "_lfrole_spoil"));
		sb.append("</center></body></html>");
		return sb.toString();
	}

	/** Level-80 support menu. */
	private static String buildBuffMenuHtml()
	{
		final StringBuilder sb = new StringBuilder(1024);
		sb.append("<html><body><center>");
		sb.append("<br1><font color=\"LEVEL\">Summon Level 80 Support</font><br1>");
		sb.append("<font color=\"808080\">One press = one shout, one bot.</font><br><br>");
		sb.append(button("Buffer",  "_lfbuff_buff"));
		sb.append(button("Singer",  "_lfbuff_singer"));
		sb.append(button("Dancer",  "_lfbuff_dancer"));
		sb.append("</center></body></html>");
		return sb.toString();
	}

	private static String button(String label, String bypass)
	{
		return "<button value=\"" + label + "\" action=\"bypass " + bypass
			+ "\" width=180 height=24 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br1>";
	}
}