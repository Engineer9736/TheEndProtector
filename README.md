# TheEndProtector
A Spigot plugin which prevents Minecraft's "The End" dimension from being demolished while still being able to fight the dragon without limitations.

Dependencies: CoreProtect (for rollback functionality)

If the Ender Dragon is not alive, then the players cannot adjust any blocks on the main island. The only exeption is that End Crystals can be placed on the Exit Portal to be able to spawn the dragon.

If the Ender Dragon is alive, then the players can adjust all blocks to be able to do the necessary to fight the Ender Dragon. For example pillaring up the obsidian towers to destroy the End Crystals.

When the Ender Dragon has been beaten, or there are no players on the main island for 5 minutes, all block changes are reverted, and if the dragon is still alive it will be removed.
