package me.Engineer9736.TheEndProtector;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener {	
	private World theEnd;
	
	private Boolean debugMessages = false;
	
	private ArrayList<Integer> checkPlayersScheduledTaskIds;
	private int amountOfMinutesNoPlayersFound = 0;
	
	@Override
	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
		theEnd = Bukkit.getServer().getWorld("world_the_end");
		
		checkPlayersScheduledTaskIds = new ArrayList<Integer>();
		
		// Check if the dragon is alive at startup, if so, start the players check loop.
		if (dragonIsAlive()) {
			startPlayersCheckLoop();
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		
		Player p = (Player) sender;
		
		if (!p.isOp()) {
			p.sendMessage(ChatColor.RED + "You have no permissions to use this command.");
			return false;	
		}
		
		if (label.equalsIgnoreCase("removedragon")) {
			p.sendMessage(ChatColor.YELLOW + "Note that /removedragon will not trigger the rollback function. Use /killdragon for this.");
			Integer removedDragons = removeDragon();
			p.sendMessage(ChatColor.GREEN + removedDragons.toString() + " dragon(s) have been killed.");
		}
		
		if (label.equalsIgnoreCase("killdragon")) {
			Integer killedDragons = killDragon();
			p.sendMessage(ChatColor.GREEN + killedDragons.toString() + " dragon(s) have been killed.");
		}
		
		if (label.equalsIgnoreCase("spawndragon")) {
			Location l = new Location(theEnd, 0, 80, 0);
			EnderDragon dragon = theEnd.spawn(l, EnderDragon.class);
			dragon.setAI(true);
			p.sendMessage(ChatColor.GREEN + "The dragon has been spawned. Rollback timestamp has been saved and the main island is now modifiable.");
		}
		
		if (label.equalsIgnoreCase("goto_end")) {
			Location l = new Location(theEnd, 10, 80, 0); // 0,0 would be right on the Exit Portal, so spawn a little to the side of it.
			
			((Player) sender).teleport(l);
		}
		
		if (label.equalsIgnoreCase("goto_overworld")) {
			Location l = new Location(Bukkit.getServer().getWorld("world"), 0, 80, 0);
			
			((Player) sender).teleport(l);
		}
		
		if (label.equalsIgnoreCase("rollbacktest")) {
			p.sendMessage(ChatColor.YELLOW + "Rollback will start in 5 seconds.");
			performRollback();
		}
		
		return true;
	}
	
	@EventHandler
	public void onPlace(BlockBreakEvent event) {	
		Player p = event.getPlayer();
		
		//p.sendMessage("Breakevent");
		
		event.setCancelled(shouldBlockEventBeCancelled(p, event.getBlock()));
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		Player p = event.getPlayer();
		
		//p.sendMessage("Placeevent " + event.getBlock().getLocation().toString());

		event.setCancelled(shouldBlockEventBeCancelled(p, event.getBlock()));
	}
	

	private boolean shouldBlockEventBeCancelled(Player p, Block block) {
		// If the BlockEvent was outside the main island, then do not block it.
		if (!locationIsMainIsland(block.getLocation())) {
			debugMessage("BlockEvent -> shouldBlockEventBeCancelled: Event was not on the main island so not cancelled.");
			return false;
		}
		
		// If the dragon is alive, then do not block BlockEvents.
		if (dragonIsAlive()) {
			debugMessage("BlockEvent -> shouldBlockEventBeCancelled: Dragon is alive, so not cancelled.");
			return false;
		}
		
		// When placing the last End Crystal to spawn the dragon, a BlockPlaceEvent placing bedrock at 3,58,1 is triggered
		// for some reason. As it cannot be the player doing this, just allow it to happen.
		if (block.getType() == Material.BEDROCK) {
			return false;
		}
		
		debugMessage("BlockEvent -> shouldBlockEventBeCancelled: BlockEvent cancelled.");
		p.sendMessage(ChatColor.RED + "Cannot adjust blocks on the main island as the Ender Dragon is not alive.");
		return true;
	}
	
	// If End Crystals are placed on obsidian, then remove it again. Players can only place End Crystals on bedrock
	// (which is what the Exit Portal is made of)
	// Taken from https://www.spigotmc.org/threads/ender-crystal-place-event.132757/
	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {
		// If the PlayerInteract was outside the main island, then do not block it.
		if (!locationIsMainIsland(event.getPlayer().getLocation())) {
			debugMessage("onPlayerInteract: Event was not on the main island so not cancelled.");
			return;
		}
				
		// If the event is not regarding a right mouseclick on a block, then do nothing.
	    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
	    	return;
	    }
	    
	    // If the event is not regarding a click on an obsidian block, then do nothing.
	    if (event.getClickedBlock().getType() != Material.OBSIDIAN) {
	    	return;
	    }
	    
	    // If the event is not regarding placing an End Crystal, then do nothing.
        if (event.getMaterial() != Material.END_CRYSTAL) {
        	return;
        }
        
        // 
        Bukkit.getScheduler().runTask(this, new Runnable() {
            @Override
            public void run() {
            	
            	// Get all entities nearby the player.
                List<Entity> entities = event.getPlayer().getNearbyEntities(4, 4, 4);

                // Loop through these entities.
                for (Entity entity : entities) {
                	
                	// If the entity type is not an End Crystal then continue to the next iteration.
                    if (entity.getType() != EntityType.ENDER_CRYSTAL) {
                    	continue;
                    }
                    
                    EnderCrystal crystal = (EnderCrystal) entity;
                    Block belowCrystal = crystal.getLocation().getBlock().getRelative(BlockFace.DOWN);

                    if (event.getClickedBlock().equals(belowCrystal)) { // Here is your EnderCrystal entity
                    	//belowCrystal.breakNaturally();
                    	entity.remove();
                        break;
                    }
                }
            }
        });
	}
	
	// When the dragon dies, rollback 300 radius from 0,0
	@EventHandler
	public void onEnderDragonDeath(EntityDeathEvent e){
	     if(e.getEntity() instanceof EnderDragon){
	    	 debugMessage("Dragon has died");
	    	 
	    	 // Stop the players check loop.
	    	 cancelAllCheckPlayersScheduledTasks();
	    	 
	    	 // Rollback the main island.
	    	 performRollback();
	    }
	}
	
	@EventHandler
	public void onSpawn(CreatureSpawnEvent event){
		if (event.getLocation().getWorld().getEnvironment() != Environment.THE_END) {
			//debugMessage("onSpawn: Environment is not the end."); This is very spammy in the overworld.
			return;
		}
		
	   if(event.getEntity() instanceof EnderDragon) {
		   debugMessage("Dragon has been spawned.");
		   
		   // Save the current time minus 2 minutes to a data file to be able to rollback to a clean main island.
		   // Dragon takes 30 seconds to spawn, so rollback to one minute earlier.
		   String unix_timestamp = String.valueOf(Instant.now().getEpochSecond() - 60);
		   try {
			   BufferedWriter writer = new BufferedWriter(new FileWriter("the_end_protector.dat"));
			   writer.write(unix_timestamp);
			   writer.close();
		   }
		   catch (Exception e) {
			   getLogger().info("Could not save the current timestamp to file.");
		   }
		   
		   // Start a loop which runs every minute to check if there are still players on the main island.
		   startPlayersCheckLoop();
	   }
	}
	
	private boolean locationIsMainIsland(Location l) {
		
		// Check if the location is regarding The End.
		// For performance, do not combine this check with the coordinate comparisons.
		if (l.getWorld().getEnvironment() != Environment.THE_END) {
			return false;
		}
		
		return l.getX() < 150 && l.getX() > -150 && l.getZ() < 150 && l.getZ() > -150;
	}
	
	private boolean dragonIsAlive(){
        List<LivingEntity> Vivos = theEnd.getLivingEntities();
        for (LivingEntity j : Vivos) {
            if (j instanceof EnderDragon) {
            	return true;
            }
        }
        
        return false;
    }
	
	private Integer removeDragon(){
        List<LivingEntity> Vivos = theEnd.getLivingEntities();
        Integer removedDragons = 0;
        
        for (LivingEntity j : Vivos) {
            if (j instanceof EnderDragon) {
            	j.remove();
            	removedDragons++;
            };
        }
        
        return removedDragons;
    }
	
	private Integer killDragon(){
        List<LivingEntity> Vivos = theEnd.getLivingEntities();
        Integer killedDragons = 0;
        for (LivingEntity j : Vivos) {
            if (j instanceof EnderDragon) {
            	j.setHealth(0);
            	killedDragons++;
            };
        }
        
        return killedDragons;
    }

	private void debugMessage(String msg) {
		if (!debugMessages) {
			return;
		}
		
		getLogger().info(msg);
	}
	
	
	
	// Check every minute if there are still players on the main island.
	// This method is called when the Ender Dragon spawns.
	// The repeating task is removed when the Ender Dragon is killed.
	// 
	private void startPlayersCheckLoop() {
		Integer checkPlayersScheduledTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
		    public void run() {
		    	
		    	if (thereAreNoPlayersOnTheMainIsland()) {
		    		amountOfMinutesNoPlayersFound++;
		    		debugMessage("There is no player on the main island for " + amountOfMinutesNoPlayersFound + " minutes");
		    	}
		    	else { // If there are still players on the island, then set the noplayers counter to 0.
		    		debugMessage("There is a player on the main island.");
		    		amountOfMinutesNoPlayersFound = 0;
		    	}
		    	
		    	// If there are no players found for 5 minutes, then remove the Ender Dragon and perform rollback.
		    	if (amountOfMinutesNoPlayersFound >= 5) {
		    		debugMessage("No players on main island for 5 minutes, removing dragon and rolling back.");
		    		
		    		removeDragon();
		    		
		    		// The remove dragon function does not trigger the dragon killed event, so the rollback has to be triggered here.
		    		performRollback();
		    		
		    		// Stop this loop.
		    		cancelAllCheckPlayersScheduledTasks();
		    	}
		    }
		}, 1200, 1200); // 20 ticks = 1 second, 1200 ticks = 1 minute. First 1200 = initial delay, second 1200 = following delays.
		// For debugging, running this loop every 5 seconds is more practical, this is 100 ticks.
		
		// Add the task id to the array to keep track of it.
		checkPlayersScheduledTaskIds.add(checkPlayersScheduledTaskId);
	}
	
	private void cancelAllCheckPlayersScheduledTasks() {
		debugMessage("Amount of timers " + checkPlayersScheduledTaskIds.size());
		
		for (Integer i : checkPlayersScheduledTaskIds) {
			debugMessage("Stopped scheduled task id " + i);
			Bukkit.getScheduler().cancelTask(i);
		}
		
		checkPlayersScheduledTaskIds.clear();
	}
	
	private Boolean thereAreNoPlayersOnTheMainIsland() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (locationIsMainIsland(player.getLocation())) {
				return false;
			}
			
		}
		
		return true;
	}
	
	// Roll back the main island as far in history as possible.
	private void performRollback() {
		debugMessage("Rolling back the main island");

		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				Runnable runnable = new Rollback();
				Thread thread = new Thread(runnable);
				thread.start();
			}
		}, 100L); // Run after 5 seconds.
	}
	
	
}
