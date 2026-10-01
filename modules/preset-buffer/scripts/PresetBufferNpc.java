package modules.presetbuffer;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.skill.Skill;

/**
 * Preset data and buff logic for the Giran preset buffer NPC. Plain class: the module entry point wires it to the
 * NPC-talk event and the bypass handler.
 */
public final class PresetBufferNpc
{
	/** NPC id, inside the range 59900-59909 reserved in module.json. */
	public static final int NPC_ID = 59900;
	
	/** The bypass command used by the dialogue buttons: "bypass -h presetbuff <event>". */
	public static final String BYPASS = "presetbuff";
	
	// {skillId, skillLevel}, applied in this order.
	private static final int[][] FIGHTER_NORMAL =
	{
		{4702, 13}, {1068, 3}, {1388, 3}, {1040, 3}, {1036, 2}, {1045, 6},
		{1086, 2}, {1204, 2}, {1077, 3}, {1242, 3}, {1240, 3}, {1268, 4},
		{1363, 1}, {4700, 13}, {1397, 3}, {1259, 4}, {1354, 1}, {1062, 2},
		{1087, 3}, {1048, 6}, {271, 1}, {275, 1}, {274, 1}, {264, 1},
		{267, 1}, {269, 1}, {304, 1}, {364, 1}, {349, 1}, {268, 1},
		{265, 1}, {310, 1}
	};

	
	private static final int[][] MAGE_NORMAL =
	{
		{1353, 1}, {1393, 3}, {1392, 3}, {1059, 3}, {1085, 3}, {1078, 6},
		{1303, 2}, {1413, 1}, {4703, 13}, {1045, 6}, {1048, 6}, {1036, 2},
		{1040, 3}, {1035, 4}, {1204, 2}, {1397, 3}, {1062, 2}, {1259, 4},
		{1354, 1}, {1389, 3}, {273, 1}, {276, 1}, {365, 1}, {264, 1},
		{267, 1}, {304, 1}, {363, 1}, {349, 1}, {268, 1}, {265, 1},
		{266, 1}, {270, 1}
	};

	
	private static final int[][] FIGHTER_EXPANDED =
	{
		{1068, 3}, {1388, 3}, {1040, 3}, {1036, 2}, {1035, 4}, {1045, 6},
		{1048, 6}, {1086, 2}, {1204, 2}, {1077, 3}, {1242, 3}, {1240, 3},
		{1268, 4}, {1363, 1}, {4700, 13}, {4702, 13}, {1397, 3}, {1062, 2},
		{1087, 3}, {1043, 1}, {1044, 3}, {1259, 4}, {1393, 3}, {1392, 3},
		{1033, 3}, {1182, 3}, {1189, 3}, {1191, 3}, {1352, 1}, {1353, 1},
		{1354, 1}, {1032, 3}, {271, 1}, {272, 1}, {274, 1}, {275, 1},
		{277, 1}, {307, 1}, {309, 1}, {310, 1}, {311, 1}, {264, 1},
		{265, 1}, {266, 1}, {267, 1}, {268, 1}, {269, 1}, {270, 1},
		{304, 1}, {305, 1}, {306, 1}, {308, 1}, {349, 1}, {363, 1},
		{364, 1}
	};

	
	private static final int[][] MAGE_EXPANDED =
	{
		{1059, 3}, {1085, 3}, {1078, 6}, {1303, 2}, {1413, 1}, {4703, 13},
		{1045, 6}, {1048, 6}, {1036, 2}, {1040, 3}, {1035, 4}, {1204, 2},
		{1397, 3}, {1062, 2}, {1087, 3}, {1044, 3}, {1389, 3}, {1259, 4},
		{1393, 3}, {1392, 3}, {1033, 3}, {1182, 3}, {1189, 3}, {1191, 3},
		{1352, 1}, {1353, 1}, {1354, 1}, {1032, 3}, {273, 1}, {276, 1},
		{365, 1}, {307, 1}, {309, 1}, {311, 1}, {264, 1}, {265, 1},
		{266, 1}, {267, 1}, {268, 1}, {270, 1}, {304, 1}, {306, 1},
		{308, 1}, {349, 1}, {363, 1}
	};
	
	private final int _pricePerBuff;
	private final boolean _buffSummon;
	
	public PresetBufferNpc(int pricePerBuff, boolean buffSummon)
	{
		_pricePerBuff = pricePerBuff;
		_buffSummon = buffSummon;
	}
	
	public void apply(String event, Player player)
	{
		if ((player == null) || player.isAlikeDead())
		{
			return;
		}
		
		final int[][] preset;
		switch (event)
		{
			case "fighter_normal":
			{
				preset = FIGHTER_NORMAL;
				break;
			}
			case "mage_normal":
			{
				preset = MAGE_NORMAL;
				break;
			}
			case "fighter_expanded":
			{
				preset = FIGHTER_EXPANDED;
				break;
			}
			case "mage_expanded":
			{
				preset = MAGE_EXPANDED;
				break;
			}
			default:
			{
				return;
			}
		}
		
		final int cost = _pricePerBuff * preset.length;
		if (cost > 0)
		{
			if (player.getInventory().getInventoryItemCount(Inventory.ADENA_ID, -1) < cost)
			{
				player.sendMessage("Not enough adena!");
				return;
			}
			
			player.destroyItemByItemId(ItemProcessType.FEE, Inventory.ADENA_ID, cost, player, true);
		}
		
		final List<Creature> targets = new ArrayList<>(2);
		targets.add(player);
		final Summon summon = player.getSummon();
		if (_buffSummon && (summon != null))
		{
			targets.add(summon);
		}
		
		for (int[] buff : preset)
		{
			final Skill skill = SkillData.getInstance().getSkill(buff[0], buff[1]);
			if (skill == null)
			{
				continue;
			}
			
			for (Creature target : targets)
			{
				skill.applyEffects(player, target);
			}
		}
	}
}
