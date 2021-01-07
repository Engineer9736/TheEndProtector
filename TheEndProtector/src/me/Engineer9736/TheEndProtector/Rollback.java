package me.Engineer9736.TheEndProtector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

public class Rollback implements Runnable {

	@Override
	public void run() {
		try {
			CoreProtectAPI CoreProtect = getCoreProtect();
			if (CoreProtect != null) { // Ensure we have access to the API
				// Rollback one hour, 150 blocks radius from the middle of The End.
				World theEnd = Bukkit.getServer().getWorld("world_the_end");
				CoreProtect.performRollback(3600, null, null, null, null, null, 150, new Location(theEnd, 00, 80, 0));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private CoreProtectAPI getCoreProtect() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");
     
        // Check that CoreProtect is loaded
        if (plugin == null || !(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (CoreProtect.isEnabled() == false) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        //if (CoreProtect.APIVersion() < 6) {
       //     return null;
        //}

        return CoreProtect;
	}
}
