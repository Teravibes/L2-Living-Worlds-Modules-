package modules.presetbuffer;

import java.util.List;

import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Dialogue pages and button handling for the preset buffer NPC. Every button sends "bypass -h presetbuff <sub> ...".
 */
final class PresetBufferUi
{
	private static final int PER_PAGE = 10;
	private static final String BTN = "width=134 height=21 back=\"L2UI_ch3.BigButton3_over\" fore=\"L2UI_ch3.BigButton3\"";
	
	private final PresetBufferNpc _presets;
	private final int _buffPrice;
	private final int _healPrice;
	private final boolean _buffSummon;
	
	PresetBufferUi(PresetBufferNpc presets, int buffPrice, int healPrice, boolean buffSummon)
	{
		_presets = presets;
		_buffPrice = buffPrice;
		_healPrice = healPrice;
		_buffSummon = buffSummon;
	}
	
	private static String button(String label, String bypass)
	{
		return "<button value=\"" + label + "\" action=\"bypass -h presetbuff " + bypass + "\" " + BTN + ">";
	}
	
	static String mainPage()
	{
		final StringBuilder sb = new StringBuilder(2048);
		sb.append("<html><body><center><br><font color=\"LEVEL\">Buffs</font><br>");
		sb.append("<table width=280 cellpadding=2 cellspacing=0>");
		sb.append("<tr><td width=140>").append(button("Buffs", "cat Buffs 1")).append("</td><td width=140>").append(button("Resist", "cat Resist 1")).append("</td></tr>");
		sb.append("<tr><td>").append(button("Songs", "cat Songs 1")).append("</td><td>").append(button("Dances", "cat Dances 1")).append("</td></tr>");
		sb.append("<tr><td>").append(button("Chants", "cat Chants 1")).append("</td><td>").append(button("Special", "cat Special 1")).append("</td></tr>");
		sb.append("</table><br><font color=\"LEVEL\">Presets</font>");
		sb.append("<table width=280 cellpadding=2 cellspacing=0>");
		sb.append("<tr><td width=140>").append(button("Fighter", "preset fighter_normal")).append("</td><td width=140>").append(button("Mage", "preset mage_normal")).append("</td></tr>");
		sb.append("<tr><td>").append(button("Fighter+", "preset fighter_expanded")).append("</td><td>").append(button("Mage+", "preset mage_expanded")).append("</td></tr>");
		sb.append("</table><br>");
		sb.append(button("Heal", "heal")).append("<br>");
		sb.append(button("Remove Buffs", "cleanup")).append("<br>");
		sb.append("<font color=\"808080\">Fighter+ and Mage+ are the expanded presets.<br1>They need raised buff slot limits.</font>");
		sb.append("</center></body></html>");
		return sb.toString();
	}
	
	private static String escape(String s)
	{
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
	
	private static void send(Player player, Creature target, String html)
	{
		final NpcHtmlMessage message = new NpcHtmlMessage((target != null) ? target.getObjectId() : 0);
		message.setHtml(html);
		player.sendPacket(message);
	}
	
	private void showCategory(Player player, Creature target, String category, int pageValue)
	{
		final List<BuffCatalog.Entry> list = BuffCatalog.CATEGORIES.get(category);
		if (list == null)
		{
			send(player, target, mainPage());
			return;
		}
		
		final int pages = Math.max(1, (list.size() + PER_PAGE - 1) / PER_PAGE);
		final int page = Math.min(Math.max(1, pageValue), pages);
		final StringBuilder sb = new StringBuilder(4096);
		sb.append("<html><body><center><br><font color=\"LEVEL\">").append(category).append("</font> (").append(page).append("/").append(pages).append(")<br>");
		sb.append("<table width=290 cellpadding=2 cellspacing=0>");
		for (int i = (page - 1) * PER_PAGE; (i < list.size()) && (i < (page * PER_PAGE)); i++)
		{
			final BuffCatalog.Entry entry = list.get(i);
			final Skill skill = SkillData.getInstance().getSkill(entry.id, entry.level);
			final String name = (skill != null) ? skill.getName() : ("Skill " + entry.id);
			sb.append("<tr><td><a action=\"bypass -h presetbuff buff ").append(entry.id).append(" ").append(category).append(" ").append(page).append("\">").append(escape(name)).append("</a><br1><font color=\"808080\">").append(escape(entry.desc)).append("</font></td></tr>");
		}
		sb.append("</table><br><table width=200><tr>");
		sb.append("<td width=70>").append((page > 1) ? ("<a action=\"bypass -h presetbuff cat " + category + " " + (page - 1) + "\">&lt; Prev</a>") : "").append("</td>");
		sb.append("<td width=70>").append((page < pages) ? ("<a action=\"bypass -h presetbuff cat " + category + " " + (page + 1) + "\">Next &gt;</a>") : "").append("</td>");
		sb.append("</tr></table><br>").append(button("Back", "main")).append("</center></body></html>");
		send(player, target, sb.toString());
	}
	
	private boolean charge(Player player, int amount)
	{
		if (amount <= 0)
		{
			return true;
		}
		
		if (player.getInventory().getInventoryItemCount(Inventory.ADENA_ID, -1) < amount)
		{
			player.sendMessage("Not enough adena!");
			return false;
		}
		
		player.destroyItemByItemId(ItemProcessType.FEE, Inventory.ADENA_ID, amount, player, true);
		return true;
	}
	
	private static int parseInt(String s, int fallback)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			return fallback;
		}
	}
	
	/** Entry point for every presetbuff bypass. */
	boolean handle(String command, Player player, Creature target)
	{
		if (player == null)
		{
			return false;
		}
		
		final String[] p = command.trim().split("\\s+");
		final String sub = (p.length > 1) ? p[1] : "main";
		switch (sub)
		{
			case "main":
			{
				send(player, target, mainPage());
				break;
			}
			case "cat":
			{
				if (p.length >= 4)
				{
					showCategory(player, target, p[2], parseInt(p[3], 1));
				}
				break;
			}
			case "buff":
			{
				// presetbuff buff <skillId> <category> <page>
				if (p.length >= 5)
				{
					final BuffCatalog.Entry entry = BuffCatalog.find(p[3], parseInt(p[2], -1));
					if ((entry != null) && !player.isAlikeDead() && charge(player, _buffPrice))
					{
						final Skill skill = SkillData.getInstance().getSkill(entry.id, entry.level);
						if (skill != null)
						{
							skill.applyEffects(player, player);
							final Summon summon = player.getSummon();
							if (_buffSummon && (summon != null))
							{
								skill.applyEffects(player, summon);
							}
						}
					}
					
					showCategory(player, target, p[3], parseInt(p[4], 1));
				}
				break;
			}
			case "preset":
			{
				if (p.length >= 3)
				{
					_presets.apply(p[2], player);
				}
				break;
			}
			case "heal":
			{
				if (!player.isAlikeDead() && charge(player, _healPrice))
				{
					player.setCurrentHp(player.getMaxHp());
					player.setCurrentMp(player.getMaxMp());
					player.setCurrentCp(player.getMaxCp());
					final Summon summon = player.getSummon();
					if (summon != null)
					{
						summon.setCurrentHp(summon.getMaxHp());
						summon.setCurrentMp(summon.getMaxMp());
					}
					
					player.updateUserInfo();
				}
				break;
			}
			case "cleanup":
			{
				player.stopAllEffects();
				final Summon summon = player.getSummon();
				if (summon != null)
				{
					summon.stopAllEffects();
				}
				break;
			}
			default:
			{
				return false;
			}
		}
		
		return true;
	}
}
