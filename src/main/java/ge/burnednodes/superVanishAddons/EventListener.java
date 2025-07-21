package ge.burnednodes.superVanishAddons;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class EventListener implements Listener {
    private final SuperVanishAddons plugin;

    public EventListener(SuperVanishAddons plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerHide(PlayerHideEvent event) {
        Player player = event.getPlayer();
        playEffects(player, "vanish");
    }

    @EventHandler
    public void onPlayerShow(PlayerShowEvent event) {
        Player player = event.getPlayer();
        playEffects(player, "appear");
    }

    private void playEffects(Player player, String type) {
        String uuid = player.getUniqueId().toString();
        String basePath = "players." + uuid + "." + type;

        if (!plugin.getDataConfig().contains(basePath)) {
            plugin.getLogger().info("No " + type + " settings for " + player.getName());
            return;
        }

        List<String> particles = plugin.getDataConfig().getStringList(basePath + ".particles");
        for (String particleName : particles) {
            try {
                Particle particle = Particle.valueOf(particleName);
                player.getWorld().spawnParticle(particle, player.getLocation(), 40, 0.5, 1, 0.5, 0.05);
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid particle: " + particleName);
            }
        }

        List<String> sounds = plugin.getDataConfig().getStringList(basePath + ".sounds");
        float volume = (float) plugin.getDataConfig().getDouble(basePath + ".volume", 1.0);
        float pitch = (float) plugin.getDataConfig().getDouble(basePath + ".pitch", 1.0);

        for (String soundName : sounds) {
            try {
                Sound sound = Sound.valueOf(soundName);
                player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid sound: " + soundName);
            }
        }
    }
}
