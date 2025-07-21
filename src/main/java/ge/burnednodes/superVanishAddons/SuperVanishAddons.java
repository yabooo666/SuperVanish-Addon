package ge.burnednodes.superVanishAddons;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;

import java.io.File;
import java.io.IOException;

public final class SuperVanishAddons extends JavaPlugin {

    private FXMenu fxMenu;

    private File dataFile;
    private YamlConfiguration dataConfig;
    private YamlConfiguration soundsConfig;
    private YamlConfiguration effectsConfig;

    @Override
    public void onEnable() {
        createDataFile();

        saveResource("sounds.yml", false);
        soundsConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "sounds.yml"));

        saveResource("effects.yml", false);
        effectsConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "effects.yml"));

        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        fxMenu = new FXMenu(this);

        getCommand("vanishfx").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                fxMenu.openMainMenu((Player) sender);
                return true;
            }
            sender.sendMessage("Players only.");
            return true;
        });
    }


    public List<String> getAvailableSounds() {
        return soundsConfig.getStringList("sounds");
    }
    public List<String> getAvailableEffects() {
        return effectsConfig.getStringList("effects");
    }

    @Override
    public void onDisable() {
        saveDataConfig();
    }

    private void createDataFile() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create data.yml!");
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public YamlConfiguration getDataConfig() {
        return dataConfig;
    }

    public void saveDataConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            getLogger().warning("Could not save data.yml!");
            e.printStackTrace();
        }
    }

    public void reloadDataConfig() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
}
