package me.flyingtaco725.theplayershuffler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;

import java.util.Collections;
import java.util.UUID;

import java.util.ArrayList;
import java.util.List;

public final class ThePlayerShuffler extends JavaPlugin implements Listener {

    private int teleportInterval;

    @Override
    public void onEnable() {
        // Save the default config file if it doesn't exist
        saveDefaultConfig();
        teleportInterval = getConfig().getInt("teleport-interval");
        getServer().getPluginManager().registerEvents(this, this);
        startLocationShuffle(teleportInterval);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Delay the task to ensure the player has been removed from the list
        Bukkit.getScheduler().runTaskLater(this, () -> {
            // Get the remaining players after the quit event
            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            // If there is exactly one player left
            if (onlinePlayers.size() == 1) {
                Player remainingPlayer = onlinePlayers.get(0);
                freezePlayer(remainingPlayer);
                getServer().broadcastMessage("§e[§lThe Player Shuffler§l] §aWe have immobilized you until at least two players join the server");
            }
        }, 20L); // 20 ticks = 1 second delay to ensure the player list is updated
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        // get the joined player info and active player list size
        Player player = event.getPlayer();
        int onlinePlayers = Bukkit.getOnlinePlayers().size();


        if(onlinePlayers == 1) {
            freezePlayer(player);
            getServer().broadcastMessage("§e[§lThe Player Shuffler§l] §aWe have immobilized you until at least two players join the server");
        } else if(onlinePlayers >= 2 ) {
            unfreezePlayers();
        }
    }

    @EventHandler
        public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player targetPlayer = (Player) event.getTarget();
            int onlinePlayers = Bukkit.getOnlinePlayers().size();
            // prevent mobs from targeting player while defenseless
            if(onlinePlayers == 1)
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        // if only one player cancel, if two or more allow
        if(Bukkit.getOnlinePlayers().size() == 1){
            event.setCancelled(true); // disables any block breaks
            getServer().broadcastMessage("§e[§lThe Player Shuffler§l] §aYou cannot eat or drink until another player joins the server");
        }
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        // if only one player cancel, if two or more allow
        if(Bukkit.getOnlinePlayers().size() == 1){
            event.setCancelled(true); // disables any block breaks
            getServer().broadcastMessage("§e[§lThe Player Shuffler§l] §aYou cannot break blocks until another player joins the server");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        // if only one player cancel, if two or more allow
        if(Bukkit.getOnlinePlayers().size() == 1){
            event.setCancelled(true); // disables any block placements
            getServer().broadcastMessage("§e[§lThe Player Shuffler§l] §aYou cannot place blocks until another player joins the server");
        }
    }

    private void freezePlayer(Player player) {
        // Define various potion effects
        PotionEffect slowEffect = new PotionEffect(PotionEffectType.SLOWNESS,Integer.MAX_VALUE, 255);
        PotionEffect jumpEffect = new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 128);
        PotionEffect blindEffect = new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 2);
        PotionEffect godEffect = new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 255);
        // Apply various potion effect
        player.addPotionEffect(godEffect);
        player.addPotionEffect(blindEffect);
        player.addPotionEffect(jumpEffect);
        player.addPotionEffect(slowEffect);
    }

    public void unfreezePlayers() {
        // Iterate through all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Iterate over all active potion effects
            for (PotionEffect effect : player.getActivePotionEffects()) {
                // Remove each potion effect
                player.removePotionEffect(effect.getType());
            }
        }
    }

    private void startLocationShuffle(int timer){
        new BukkitRunnable() {
            @Override
            public void run() {
                if(Bukkit.getOnlinePlayers().size() >= 2){
                    List<MCPlayer> playerDataList = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        UUID playerId = player.getUniqueId();
                        Location location = player.getLocation();
                        double yaw = location.getYaw(); // Heading
                        double pitch = location.getPitch(); // Pitch

                        playerDataList.add(new MCPlayer(playerId, location, yaw, pitch));
                    }
                    List<MCPlayer> shuffledList = shuffle(playerDataList);
                    for(int i = 0; i < playerDataList.size(); i++){
                        // get their original info and shuffled info
                        MCPlayer originalData = playerDataList.get(i);
                        MCPlayer shuffledData = shuffledList.get(i);
                        // teleport the original uuids to the shuffled locations
                        Player player = Bukkit.getPlayer(originalData.getId());
                        if(player != null){
                            Location shuffledLocation = shuffledData.getLocation();
                            shuffledLocation.setYaw((float) shuffledData.getYaw());
                            shuffledLocation.setPitch((float) shuffledData.getPitch());
                            player.teleport(shuffledLocation);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, timer*20L);
    }

    private List<MCPlayer> shuffle(List<MCPlayer> originalList){
        // take the original list and shuffle it
        List<MCPlayer> shuffledList = new ArrayList<>(originalList);
        int size = shuffledList.size();

        // flag for while loop
        boolean isShuffled = false;

        while(!isShuffled){
            Collections.shuffle(shuffledList);
            isShuffled = true;

            for (int i = 0; i < size; i++){
                if(originalList.get(i).getId().equals(shuffledList.get(i).getId())){
                    isShuffled = false;
                    break;
                }
            }
        }
        return shuffledList;
    }
}
