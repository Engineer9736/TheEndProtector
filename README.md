# TheEndProtector
A Spigot plugin which prevents the main island of Minecraft's "The End" dimension from being demolished by players, while still being able to fight the dragon without limitations.

Dependencies: CoreProtect (for rollback functionality)

If the Ender Dragon is not alive, then the players cannot adjust any blocks on the main island. The only exception is that End Crystals can be placed on the Exit Portal to be able to spawn the dragon.

If the Ender Dragon is alive, then the players can adjust all blocks to be able to do the necessary to fight the Ender Dragon. For example pillaring up the obsidian towers to destroy the End Crystals.

When the Ender Dragon has been beaten, or there are no players on the main island for 5 minutes, all block changes are reverted, and if the dragon is still alive then it will be removed.

If you are operator ingame, then you can use the following test/debug commands:
removedragon: Remove the dragon without the beaten sequence.
spawndragon: Spawn the dragon
killdragon: Kill the dragon inc. beaten sequence.
goto_end: Teleport yourself to The End.
goto_overworld: Teleport yourself to the overworld.
rollbacktest: Run the rollback function. Note that the dragon needs to have been spawned once. Otherwise the plugin doesn't know what time to rollback to.

https://www.spigotmc.org/resources/the-end-protector.87624/
