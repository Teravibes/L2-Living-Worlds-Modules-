package modules.bufflimits;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.modules.GameModule;
import org.l2jmobius.gameserver.modules.ModuleContext;

/**
 * Buff Limits module. Sets 2-hour durations on the buffer-NPC buffs and on perma-uptime class self buffs.
 *
 * Durations are changed in memory at startup through reflection. Nothing on disk is edited: disable the module and
 * restart, and the server is stock again. Buff and dance slots are set in Player.ini instead.
 */
public class BuffLimitsModule implements GameModule
{
	private static final int[] BUFF_IDS =
	{
		264, 265, 266, 267, 268, 269, 270, 271, 272, 273, 274, 275,
		276, 277, 304, 305, 306, 307, 308, 309, 310, 311, 349, 363,
		364, 365, 1002, 1006, 1007, 1009, 1032, 1033, 1035, 1036, 1040, 1043,
		1044, 1045, 1048, 1059, 1062, 1068, 1077, 1078, 1085, 1086, 1087, 1182,
		1189, 1191, 1204, 1240, 1242, 1243, 1251, 1252, 1253, 1259, 1268, 1284,
		1303, 1304, 1308, 1309, 1310, 1352, 1353, 1354, 1355, 1356, 1357, 1362,
		1363, 1388, 1389, 1390, 1391, 1392, 1393, 1397, 1413, 4699, 4700, 4702,
		4703
	};
	
	// Class self buffs that can be kept up permanently by recasting (War Cry, Rage, auras...). Burst and emergency
	// skills (Frenzy, Guts, Zealot, Lionheart, Ultimate Defense...) and stance-locked skills are deliberately left out.
	private static final int[] SELF_BUFF_IDS =
	{
		72, 77, 78, 82, 86, 91, 94, 99, 121, 123, 130, 131, 230, 297, 303, 415, 416, 423, 1047
	};
	
	@Override
	public void onEnable(ModuleContext context)
	{
		if (!context.config().getBoolean("Enabled", false))
		{
			return; // Switch off: register nothing, behave as stock.
		}
		
		final int duration = context.config().getInt("BuffDurationSeconds", 7200);
		final int selfDuration = context.config().getInt("SelfBuffDurationSeconds", 7200);
		
		if (duration > 0)
		{
			applyDuration(context, "buffer", BUFF_IDS, duration);
		}
		
		if (selfDuration > 0)
		{
			applyDuration(context, "self buff", SELF_BUFF_IDS, selfDuration);
		}
	}
	
	/** Sets the duration of the listed buffer skills (all levels) already loaded in memory. */
	private static void applyDuration(ModuleContext context, String label, int[] ids, int seconds)
	{
		int changed = 0;
		int missing = 0;
		for (int id : ids)
		{
			boolean found = false;
			final int maxLevel = SkillData.getInstance().getMaxLevel(id);
			for (int level = 1; level <= maxLevel; level++)
			{
				final Skill skill = SkillData.getInstance().getSkill(id, level);
				if (skill == null)
				{
					continue;
				}
				
				final Field field = findAbnormalTimeField(skill.getClass());
				if (field == null)
				{
					continue;
				}
				
				try
				{
					field.setAccessible(true);
					if (field.getInt(skill) != seconds)
					{
						field.setInt(skill, seconds);
						changed++;
					}
					
					found = true;
				}
				catch (Exception e)
				{
					context.logging().info("Buff Limits: could not set duration on skill " + id + ": " + e);
					return;
				}
			}
			
			if (!found)
			{
				missing++;
			}
		}
		
		context.logging().info("Buff Limits: " + label + " duration set to " + seconds + "s on " + changed + " skill levels (" + missing + " skill ids not found).");
	}
	
	private static Field findAbnormalTimeField(Class<?> type)
	{
		for (Class<?> c = type; c != null; c = c.getSuperclass())
		{
			for (Field field : c.getDeclaredFields())
			{
				if (!Modifier.isStatic(field.getModifiers()) && (field.getType() == int.class) && field.getName().replace("_", "").equalsIgnoreCase("abnormalTime"))
				{
					return field;
				}
			}
		}
		
		return null;
	}
}
