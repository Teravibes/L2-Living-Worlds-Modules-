package modules.botcontrol;

import org.l2jmobius.gameserver.modules.GameModule;
import org.l2jmobius.gameserver.modules.ModuleContext;

/**
 * Bot Control module.
 * <p>
 * Registers a voiced command (".botmenu") that opens an HTML window, and a bypass handler that sends a fixed
 * party-chat message when a button is pressed. The bypass handler routes the text through the server's own
 * party-chat handler (ChatParty), so Living World phantoms (PhantomPartyManager / PhantomBuddyManager) react
 * exactly as if the player had typed the line.
 */
public class BotControlModule implements GameModule
{
	@Override
	public void onEnable(ModuleContext context)
	{
		if (!context.config().getBoolean("Enabled", false))
		{
			return;
		}
		
		final String voicedCommand = context.config().getString("VoicedCommand", "botmenu");
		
		final BotControlBypass bypassHandler = new BotControlBypass(context.logging());
		
		// Register every button: bypass token -> party-chat text.
		// Movement
		register(context, bypassHandler, "BypassAssist",       "_bbsbot_assist",       "ChatAssist",       "assist");
		register(context, bypassHandler, "BypassAttackFreely", "_bbsbot_attackfreely", "ChatAttackFreely", "attack freely");
		register(context, bypassHandler, "BypassFollow",       "_bbsbot_follow",       "ChatFollow",       "follow");
		register(context, bypassHandler, "BypassHold",         "_bbsbot_hold",         "ChatHold",         "hold");
		register(context, bypassHandler, "BypassGather",       "_bbsbot_gather",       "ChatGather",       "gather");
		// Raid
		register(context, bypassHandler, "BypassTankAttack", "_bbsbot_tankattack", "ChatTankAttack", "tank attack");
		register(context, bypassHandler, "BypassAllAttack",  "_bbsbot_allattack",  "ChatAllAttack",  "all attack");
		register(context, bypassHandler, "BypassHoldFire",   "_bbsbot_holdfire",   "ChatHoldFire",   "hold fire");
		// Support
		register(context, bypassHandler, "BypassBuffMe",  "_bbsbot_buffme",  "ChatBuffMe",  "buff me");
		register(context, bypassHandler, "BypassBuffAll", "_bbsbot_buffall", "ChatBuffAll", "buff all");
		register(context, bypassHandler, "BypassHealMe",  "_bbsbot_healme",  "ChatHealMe",  "heal me");
		register(context, bypassHandler, "BypassRes",     "_bbsbot_res",     "ChatRes",     "res");
		register(context, bypassHandler, "BypassSongs",   "_bbsbot_songs",   "ChatSongs",   "songs");
		register(context, bypassHandler, "BypassDance",   "_bbsbot_dance",   "ChatDance",   "dance");
		// Loot
		register(context, bypassHandler, "BypassReturnLoot",      "_bbsbot_returnloot",      "ChatReturnLoot",      "return loot");
		register(context, bypassHandler, "BypassPartyReturnLoot", "_bbsbot_partyreturnloot", "ChatPartyReturnLoot", "party return loot");
		// Party
		register(context, bypassHandler, "BypassStatus",  "_bbsbot_status",  "ChatStatus",  "status");
		register(context, bypassHandler, "BypassBrb",     "_bbsbot_brb",     "ChatBrb",     "brb");
		register(context, bypassHandler, "BypassDisband", "_bbsbot_disband", "ChatDisband", "disband");
		
		context.handlers().registerBypass(bypassHandler);
		
		// Voiced command opens the menu.
		final String menuHtml = buildMenuHtml();
		context.handlers().registerVoicedCommand(new BotControlVoiced(voicedCommand, menuHtml));
		
		context.logging().info("Bot Control Menu enabled. Voiced command: ." + voicedCommand
			+ " (" + bypassHandler.getCommandList().length + " buttons)");
	}
	
	/** Registers one menu button: reads its bypass token and chat text from the module config. */
	private static void register(ModuleContext context, BotControlBypass handler, String bypassKey, String bypassDefault, String chatKey, String chatDefault)
	{
		final String bypass = context.config().getString(bypassKey, bypassDefault);
		final String chat = context.config().getString(chatKey, chatDefault);
		handler.add(bypass, chat);
	}
	
	/** Builds the menu as two columns, grouped by purpose, using the bypass tokens from the handler. */
	private String buildMenuHtml()
	{
		final StringBuilder sb = new StringBuilder(2048);
		sb.append("<html><body>");
		sb.append("<center><font color=\"LEVEL\">Bot Control Menu</font></center>");
		sb.append("<br>");
		sb.append("<table width=280>");
		sb.append("<tr>");
		
		// Left column: movement + raid.
		sb.append("<td valign=top>");
		sb.append("<font color=\"LEVEL\">Movement</font><br>");
		sb.append(button("Assist",        "_bbsbot_assist"));
		sb.append(button("Attack Freely", "_bbsbot_attackfreely"));
		sb.append(button("Follow",        "_bbsbot_follow"));
		sb.append(button("Hold",          "_bbsbot_hold"));
		sb.append(button("Gather",        "_bbsbot_gather"));
		sb.append("<br><font color=\"LEVEL\">Raid</font><br>");
		sb.append(button("Tank Attack", "_bbsbot_tankattack"));
		sb.append(button("All Attack",  "_bbsbot_allattack"));
		sb.append(button("Hold Fire",   "_bbsbot_holdfire"));
		sb.append("</td>");
		
		// Right column: support + loot + party.
		sb.append("<td valign=top>");
		sb.append("<font color=\"LEVEL\">Support</font><br>");
		sb.append(button("Buff Me",  "_bbsbot_buffme"));
		sb.append(button("Buff All", "_bbsbot_buffall"));
		sb.append(button("Heal Me",  "_bbsbot_healme"));
		sb.append(button("Res",      "_bbsbot_res"));
		sb.append(button("Songs",    "_bbsbot_songs"));
		sb.append(button("Dance",    "_bbsbot_dance"));
		sb.append("<br><font color=\"LEVEL\">Loot</font><br>");
		sb.append(button("Return Loot",       "_bbsbot_returnloot"));
		sb.append(button("Party Return Loot", "_bbsbot_partyreturnloot"));
		sb.append("<br><font color=\"LEVEL\">Party</font><br>");
		sb.append(button("Status",  "_bbsbot_status"));
		sb.append(button("BRB",     "_bbsbot_brb"));
		sb.append(button("Disband", "_bbsbot_disband"));
		sb.append("</td>");
		
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</body></html>");
		return sb.toString();
	}
	
	private static String button(String label, String bypass)
	{
		return "<button value=\"" + label + "\" action=\"bypass " + bypass
			+ "\" width=130 height=24 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br1>";
	}
}