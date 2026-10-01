// Example Module entry point.
//
// The package is modules.<id> with hyphens removed. For the id "example-module"
// that is modules.examplemodule. Replace this skeleton with your own feature.
//
// This file is licensed under the GNU General Public License v3.0, the same as
// the repository. Keep a GPL header on your source files.

package modules.examplemodule;

import org.l2jmobius.gameserver.modules.GameModule;
import org.l2jmobius.gameserver.modules.ModuleContext;

public final class ExampleModule implements GameModule
{
	@Override
	public void onEnable(ModuleContext context)
	{
		// Always check the enable flag first, and do nothing when it is off.
		if (!context.config().getBoolean("Enabled", false))
		{
			return;
		}

		context.logging().info("Example Module enabled.");

		// Register your handlers, events, and hooks here through the context, for
		// example:
		//   context.handlers().register(...);
		//   context.events().register(...);
	}
}
