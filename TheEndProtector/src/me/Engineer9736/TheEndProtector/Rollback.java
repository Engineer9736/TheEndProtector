package me.Engineer9736.TheEndProtector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

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
				
				// Get the rollback timestamp from file. This was saved during the dragon spawn event.
				String dragonSpawnTimeString = "";
				try {
					dragonSpawnTimeString = new String (Files.readAllBytes(Paths.get("the_end_protector.dat")));
		        } 
		        catch (IOException e) {
		            e.printStackTrace();
		            
		            Bukkit.getLogger().info("Could not read the_end_protector.dat, stopping rollback.");
		            return;
		        }
				
				// Convert the time into an Integer to be able to calculate with it.
				Integer dragonSpawnTime = 0;
				try {
					dragonSpawnTime = Integer.parseInt(dragonSpawnTimeString);
				}
				catch (Exception e) {
					Bukkit.getLogger().info("Could not convert the value in the_end_protector to an Integer, stopping rollback.");
					return;
				}

				// Check that the dragonSpawnTime value is a plausible value.
				if (dragonSpawnTime < 1577836800) { // 1577836800 = 1 Jan 2020
					Bukkit.getLogger().info("The dragon spawn time read from the_end_protector.dat was before 1 Jan 2020, this is probably not right, so stopping rollback.");
					return;
				}
				
				
				// Get the current unix timestamp into an Integer.
				Integer currentTime = (int) Instant.now().getEpochSecond();
				
				// Substract the current time from the dragonSpawnTime
				Integer rollbackSeconds = currentTime - dragonSpawnTime;
				
				//Bukkit.getLogger().info("Rolling back " + rollbackSeconds + " seconds.");
				
				// Rollback one hour, 150 blocks radius from the middle of The End.
				World theEnd = Bukkit.getServer().getWorld("world_the_end");
				CoreProtect.performRollback(rollbackSeconds, null, null, null, null, null, 150, new Location(theEnd, 00, 80, 0));
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
