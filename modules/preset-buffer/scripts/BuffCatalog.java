package modules.presetbuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The buffs offered on the category pages (same lists as the Scheme Buffer), each at its max level.
 */
final class BuffCatalog
{
	static final class Entry
	{
		final int id;
		final int level;
		final String desc;
		
		Entry(int id, int level, String desc)
		{
			this.id = id;
			this.level = level;
			this.desc = desc;
		}
	}
	
	static final Map<String, List<Entry>> CATEGORIES = new LinkedHashMap<>();
	
	private static void add(String category, Entry... entries)
	{
		CATEGORIES.put(category, new ArrayList<>(Arrays.asList(entries)));
	}
	
	static Entry find(String category, int id)
	{
		final List<Entry> list = CATEGORIES.get(category);
		if (list != null)
		{
			for (Entry entry : list)
			{
				if (entry.id == id)
				{
					return entry;
				}
			}
		}
		return null;
	}
	
	static
	{
		add("Buffs",
			new Entry(1035, 4, "Increases resistance to mental attacks."),
			new Entry(1036, 2, "Increases M. Def."),
			new Entry(1040, 3, "Increases P. Def."),
			new Entry(1044, 3, "Temporarily increases HP recovery."),
			new Entry(1045, 6, "Increases maximum HP."),
			new Entry(1048, 6, "Increases maximum MP."),
			new Entry(1059, 3, "Increases M. Atk."),
			new Entry(1062, 2, "Reduces def. and increase atk. power."),
			new Entry(1068, 3, "Increases P. Atk."),
			new Entry(1077, 3, "Increases critical attack rate."),
			new Entry(1078, 6, "Increases magic concentration."),
			new Entry(1085, 3, "Increases Casting Spd."),
			new Entry(1086, 2, "Increases Atk. Spd."),
			new Entry(1087, 3, "Increases Evasion."),
			new Entry(1204, 2, "Increases Speed."),
			new Entry(1240, 3, "Increases Accuracy."),
			new Entry(1242, 3, "Increases critical attack."),
			new Entry(1243, 6, "Increases shield defense rate."),
			new Entry(1268, 4, "Restores HP using inflicted damage."),
			new Entry(1303, 2, "Increases crit. rate of magic attacks."),
			new Entry(1304, 3, "Increases shield defense power."),
			new Entry(1388, 3, "Increases P. Atk."),
			new Entry(1389, 3, "Increases P. Def."),
			new Entry(1390, 3, "Increases P. Atk."),
			new Entry(1391, 3, "Increases P. Def."),
			new Entry(1397, 3, "Decreases MP consumption rate."),
			new Entry(1043, 1, "Temporary holy enhancement of physical attack."));
		add("Resist",
			new Entry(1259, 4, "Increases resistance to stun attack."),
			new Entry(1393, 3, "Increases resistance to dark attacks."),
			new Entry(1392, 3, "Increases resistance to sacred attacks."),
			new Entry(1033, 3, "Increases resistance to poison"),
			new Entry(1182, 3, "Increases tolerance to attack by water"),
			new Entry(1189, 3, "Increases resistance to attack by wind"),
			new Entry(1191, 3, "increases resistance to attacks by fire"),
			new Entry(1352, 1, "Increases resistance to atures."),
			new Entry(1353, 1, "Increases resistance to dark attack."),
			new Entry(1354, 1, "Increases resistance to de-buff attack."),
			new Entry(1032, 3, "increases resistance to bleeding"),
			new Entry(307, 1, "Increases resistance to attacks by water"),
			new Entry(309, 1, "Increases resistance to attacks by earth"),
			new Entry(311, 1, "Increases resistance to terrain damage"),
			new Entry(306, 1, "Increases resistance to attacks by fire"),
			new Entry(308, 1, "Increases resistance to attacks by wind"),
			new Entry(270, 1, "Increases resistance to dark magic attacks"));
		add("Songs",
			new Entry(264, 1, "Increases P. Def."),
			new Entry(265, 1, "Increases HP regeneration."),
			new Entry(266, 1, "Increases Evasion."),
			new Entry(267, 1, "Increases M. Def."),
			new Entry(268, 1, "Increases movement."),
			new Entry(269, 1, "Increases critical rate."),
			new Entry(270, 1, "Increases resistance to dark magic."),
			new Entry(304, 1, "Increases maximum HP."),
			new Entry(305, 1, "Reflects damage received."),
			new Entry(306, 1, "Increases resistance to fire."),
			new Entry(308, 1, "Increases resistance to wind."),
			new Entry(349, 1, "Decreases re-use time."),
			new Entry(363, 1, "Increases MP regeneration rate."),
			new Entry(364, 1, "Decreases re-use time of physical skills."));
		add("Dances",
			new Entry(271, 1, "Increases P. Atk."),
			new Entry(272, 1, "Increases Accuracy."),
			new Entry(273, 1, "Increases M. Atk."),
			new Entry(274, 1, "Increases critical damage."),
			new Entry(275, 1, "Increases attack speed."),
			new Entry(276, 1, "Increases Casting Spd."),
			new Entry(277, 1, "Sacred power to physical attack."),
			new Entry(307, 1, "Increases water resistance."),
			new Entry(309, 1, "Increases earth resistance."),
			new Entry(310, 1, "Restores HP by inflicted damage."),
			new Entry(311, 1, "Increases resistance to terrain damage."),
			new Entry(365, 1, "Increases rate of magic crit. damage."));
		add("Chants",
			new Entry(1002, 3, "Increases Casting Spd."),
			new Entry(1251, 2, "Increases Atk. Spd."),
			new Entry(1252, 3, "Increases Evasion."),
			new Entry(1253, 3, "Increases critical attack."),
			new Entry(1284, 3, "Reflects damage received."),
			new Entry(1006, 3, "Increases M. Def."),
			new Entry(1007, 3, "Increases P. Atk."),
			new Entry(1009, 3, "Increases P. Def."),
			new Entry(1362, 1, "Increases resistance to Cancel attack."),
			new Entry(1310, 4, "Restores HP using inflicted damage."),
			new Entry(1309, 3, "Increases Accuracy."),
			new Entry(1308, 3, "Increases critical attack rate."));
		add("Special",
			new Entry(1355, 1, "Increases all mage abilities."),
			new Entry(1356, 1, "Increases all fighter abilities."),
			new Entry(1357, 1, "Increases all dagger abilities."),
			new Entry(1363, 1, "Increases combat abilities."),
			new Entry(1413, 1, "Increases mage abilities."),
			new Entry(4702, 13, "Increases MP regeneration rate"),
			new Entry(4703, 13, "Decreases re-use time of magic skills"),
			new Entry(4700, 13, "Increases P. Atk. and Accuracy"),
			new Entry(4699, 13, "Increases critical chance and power"));
	}
	
	private BuffCatalog()
	{
	}
}
