package ge.burnednodes.superVanishAddons;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class FXMenu implements Listener {

    public enum MenuType { VANISH, APPEAR }
    public enum SubMenu { MAIN, PARTICLE, SOUND, VOLUME, PITCH }

    private static class MenuState {
        MenuType type;
        SubMenu subMenu;
        int page;
        MenuState(MenuType type, SubMenu subMenu) { this(type, subMenu, 0); }
        MenuState(MenuType type, SubMenu subMenu, int page) {
            this.type = type;
            this.subMenu = subMenu;
            this.page = page;
        }
    }

    private final SuperVanishAddons plugin;
    private final Map<UUID, MenuState> menuState = new HashMap<>();

    public FXMenu(SuperVanishAddons plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Choose FX Type");
        inv.setItem(3, makeItem(Material.ENDER_EYE, "&bEdit Vanish FX"));
        inv.setItem(5, makeItem(Material.EXPERIENCE_BOTTLE, "&aEdit Appear FX"));
        player.openInventory(inv);
        menuState.put(player.getUniqueId(), new MenuState(MenuType.VANISH, SubMenu.MAIN));
    }

    public void openFXSubMenu(Player player, MenuType type) {
        Inventory inv = Bukkit.createInventory(null, 9, (type == MenuType.VANISH ? "Vanish" : "Appear") + " FX Menu");
        inv.setItem(1, makeItem(Material.FIREWORK_STAR, "&eParticles"));
        inv.setItem(3, makeItem(Material.NOTE_BLOCK, "&bSounds"));
        inv.setItem(5, makeItem(Material.BUCKET, "&6Volume"));
        inv.setItem(7, makeItem(Material.HOPPER, "&dPitch"));
        inv.setItem(8, makeItem(Material.ARROW, "&cBack"));
        player.openInventory(inv);
        menuState.put(player.getUniqueId(), new MenuState(type, SubMenu.MAIN));
    }

    public void openParticleMenu(Player player, MenuType type, int page) {
        List<String> availableParticles = plugin.getAvailableEffects();
        final int pageSize = 45;
        int maxPage = Math.max(0, (availableParticles.size() - 1) / pageSize);
        page = Math.max(0, Math.min(page, maxPage));

        Inventory inv = Bukkit.createInventory(null, 54, (type == MenuType.VANISH ? "Vanish" : "Appear") + " Particles");
        List<String> selected = getConfigList(player, type, "particles");

        int start = page * pageSize;
        int end = Math.min(availableParticles.size(), start + pageSize);
        int i = 0;
        for (int idx = start; idx < end; idx++) {
            String effectName = availableParticles.get(idx);
            try {
                Particle particle = Particle.valueOf(effectName);
                boolean isSelected = selected.contains(particle.name());
                inv.setItem(i++, makeItem(Material.FIREWORK_STAR, (isSelected ? "&a" : "&f") + particle.name()));
            } catch (Exception ignored) {}
        }

        if (page > 0) inv.setItem(45, makeItem(Material.ARROW, "&ePrevious"));
        inv.setItem(48, makeItem(Material.ARROW, "&cBack"));
        inv.setItem(49, makeItem(Material.BARRIER, "&cReset"));
        if (page < maxPage) inv.setItem(53, makeItem(Material.ARROW, "&eNext"));
        player.openInventory(inv);
        menuState.put(player.getUniqueId(), new MenuState(type, SubMenu.PARTICLE, page));
    }

    public void openSoundMenu(Player player, MenuType type, int page) {
        List<String> availableSounds = plugin.getAvailableSounds();
        final int pageSize = 45;
        int maxPage = Math.max(0, (availableSounds.size() - 1) / pageSize);
        page = Math.max(0, Math.min(page, maxPage));

        Inventory inv = Bukkit.createInventory(null, 54, (type == MenuType.VANISH ? "Vanish" : "Appear") + " Sounds");
        List<String> selected = getConfigList(player, type, "sounds");
        String selectedSound = selected.isEmpty() ? null : selected.get(0);

        int start = page * pageSize;
        int end = Math.min(availableSounds.size(), start + pageSize);
        int i = 0;
        for (int idx = start; idx < end; idx++) {
            String soundName = availableSounds.get(idx);
            try {
                Sound sound = Sound.valueOf(soundName);
                boolean isSelected = sound.name().equals(selectedSound);
                inv.setItem(i++, makeItem(Material.NOTE_BLOCK, (isSelected ? "&a" : "&f") + sound.name()));
            } catch (Exception ignored) {}
        }

        if (page > 0) inv.setItem(45, makeItem(Material.ARROW, "&ePrevious"));
        inv.setItem(48, makeItem(Material.ARROW, "&cBack"));
        inv.setItem(49, makeItem(Material.BARRIER, "&cReset"));
        if (page < maxPage) inv.setItem(53, makeItem(Material.ARROW, "&eNext"));
        player.openInventory(inv);
        menuState.put(player.getUniqueId(), new MenuState(type, SubMenu.SOUND, page));
    }

    public void openVolumeMenu(Player player, MenuType type) {
        float volume = getConfigFloat(player, type, "volume", 1.0f);
        Inventory inv = Bukkit.createInventory(null, 9, (type == MenuType.VANISH ? "Vanish" : "Appear") + " Volume");
        inv.setItem(2, makeItem(Material.REDSTONE, "&c-0.1"));
        inv.setItem(4, makeItem(Material.BUCKET, "&eVolume: &b" + volume));
        inv.setItem(6, makeItem(Material.GLOWSTONE_DUST, "&a+0.1"));
        inv.setItem(8, makeItem(Material.ARROW, "&cBack"));
        player.openInventory(inv);
        menuState.put(player.getUniqueId(), new MenuState(type, SubMenu.VOLUME));
    }

    public void openPitchMenu(Player player, MenuType type) {
        float pitch = getConfigFloat(player, type, "pitch", 1.0f);
        Inventory inv = Bukkit.createInventory(null, 9, (type == MenuType.VANISH ? "Vanish" : "Appear") + " Pitch");
        inv.setItem(2, makeItem(Material.REDSTONE, "&c-0.1"));
        inv.setItem(4, makeItem(Material.HOPPER, "&ePitch: &b" + pitch));
        inv.setItem(6, makeItem(Material.GLOWSTONE_DUST, "&a+0.1"));
        inv.setItem(8, makeItem(Material.ARROW, "&cBack"));
        player.openInventory(inv);
        menuState.put(player.getUniqueId(), new MenuState(type, SubMenu.PITCH));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String title = e.getView().getTitle();
        UUID uuid = player.getUniqueId();
        MenuState state = menuState.getOrDefault(uuid, new MenuState(MenuType.VANISH, SubMenu.MAIN));

        if (title.equals("Choose FX Type")) {
            e.setCancelled(true);
            if (item.getType() == Material.ENDER_EYE) openFXSubMenu(player, MenuType.VANISH);
            else if (item.getType() == Material.EXPERIENCE_BOTTLE) openFXSubMenu(player, MenuType.APPEAR);
            return;
        }
        if (title.endsWith("FX Menu")) {
            e.setCancelled(true);
            if (item.getType() == Material.FIREWORK_STAR) openParticleMenu(player, state.type, 0);
            else if (item.getType() == Material.NOTE_BLOCK) openSoundMenu(player, state.type, 0);
            else if (item.getType() == Material.BUCKET) openVolumeMenu(player, state.type);
            else if (item.getType() == Material.HOPPER) openPitchMenu(player, state.type);
            else if (item.getType() == Material.ARROW) openMainMenu(player);
            return;
        }
        // Particle menu navigation & selection
        if (title.endsWith("Particles")) {
            e.setCancelled(true);
            int slot = e.getSlot();
            MenuState ms = menuState.getOrDefault(uuid, new MenuState(MenuType.VANISH, SubMenu.PARTICLE, 0));
            int page = ms.page;

            if (slot < 45 && item.getType() == Material.FIREWORK_STAR) {
                String particleName = item.getItemMeta().getDisplayName().replace("§a", "").replace("§f", "");
                toggleConfigList(player, ms.type, "particles", particleName);
                openParticleMenu(player, ms.type, page);
            }
            else if (slot == 45) openParticleMenu(player, ms.type, page - 1);
            else if (slot == 48) openFXSubMenu(player, ms.type);
            else if (slot == 49) { clearParticles(player, ms.type); openParticleMenu(player, ms.type, page);}
            else if (slot == 53) openParticleMenu(player, ms.type, page + 1);
            return;
        }
        // Sound menu navigation & selection
        if (title.endsWith("Sounds")) {
            e.setCancelled(true);
            int slot = e.getSlot();
            MenuState ms = menuState.getOrDefault(uuid, new MenuState(MenuType.VANISH, SubMenu.SOUND, 0));
            int page = ms.page;

            if (slot < 45 && item.getType() == Material.NOTE_BLOCK) {
                String soundName = item.getItemMeta().getDisplayName().replace("§a", "").replace("§f", "");
                setSingleSound(player, ms.type, soundName);
                openSoundMenu(player, ms.type, page);
            }
            else if (slot == 45) openSoundMenu(player, ms.type, page - 1);
            else if (slot == 48) openFXSubMenu(player, ms.type);
            else if (slot == 49) { setSingleSound(player, ms.type, null); openSoundMenu(player, ms.type, page);}
            else if (slot == 53) openSoundMenu(player, ms.type, page + 1);
            return;
        }
        if (title.endsWith("Volume")) {
            e.setCancelled(true);
            float current = getConfigFloat(player, state.type, "volume", 1.0f);
            if (item.getType() == Material.REDSTONE) {
                float next = Math.max(0.0f, current - 0.1f);
                setConfigFloat(player, state.type, "volume", next);
                openVolumeMenu(player, state.type);
            } else if (item.getType() == Material.GLOWSTONE_DUST) {
                float next = Math.min(10.0f, current + 0.1f);
                setConfigFloat(player, state.type, "volume", next);
                openVolumeMenu(player, state.type);
            } else if (item.getType() == Material.ARROW) {
                openFXSubMenu(player, state.type);
            }
            return;
        }
        if (title.endsWith("Pitch")) {
            e.setCancelled(true);
            float current = getConfigFloat(player, state.type, "pitch", 1.0f);
            if (item.getType() == Material.REDSTONE) {
                float next = Math.max(0.0f, current - 0.1f);
                setConfigFloat(player, state.type, "pitch", next);
                openPitchMenu(player, state.type);
            } else if (item.getType() == Material.GLOWSTONE_DUST) {
                float next = Math.min(2.0f, current + 0.1f);
                setConfigFloat(player, state.type, "pitch", next);
                openPitchMenu(player, state.type);
            } else if (item.getType() == Material.ARROW) {
                openFXSubMenu(player, state.type);
            }
            return;
        }
    }

    private List<String> getConfigList(Player player, MenuType type, String key) {
        String uuid = player.getUniqueId().toString();
        String path = "players." + uuid + "." + type.name().toLowerCase() + "." + key;
        List<String> list = plugin.getDataConfig().getStringList(path);
        return list != null ? list : new ArrayList<>();
    }
    private void toggleConfigList(Player player, MenuType type, String key, String value) {
        String uuid = player.getUniqueId().toString();
        String path = "players." + uuid + "." + type.name().toLowerCase() + "." + key;
        List<String> list = plugin.getDataConfig().getStringList(path);
        if (list == null) list = new ArrayList<>();
        if (list.contains(value)) list.remove(value);
        else list.add(value);
        plugin.getDataConfig().set(path, list);
        plugin.saveDataConfig();
    }
    private void clearParticles(Player player, MenuType type) {
        String uuid = player.getUniqueId().toString();
        String path = "players." + uuid + "." + type.name().toLowerCase() + ".particles";
        plugin.getDataConfig().set(path, new ArrayList<>());
        plugin.saveDataConfig();
    }
    private void setSingleSound(Player player, MenuType type, String soundName) {
        String uuid = player.getUniqueId().toString();
        String path = "players." + uuid + "." + type.name().toLowerCase() + ".sounds";
        List<String> newList = new ArrayList<>();
        if (soundName != null) newList.add(soundName);
        plugin.getDataConfig().set(path, newList);
        plugin.saveDataConfig();
    }
    private float getConfigFloat(Player player, MenuType type, String key, float def) {
        String uuid = player.getUniqueId().toString();
        String path = "players." + uuid + "." + type.name().toLowerCase() + "." + key;
        return (float) plugin.getDataConfig().getDouble(path, def);
    }
    private void setConfigFloat(Player player, MenuType type, String key, float value) {
        String uuid = player.getUniqueId().toString();
        String path = "players." + uuid + "." + type.name().toLowerCase() + "." + key;
        plugin.getDataConfig().set(path, value);
        plugin.saveDataConfig();
    }
    private ItemStack makeItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name.replace("&", "§"));
        item.setItemMeta(meta);
        return item;
    }
}
