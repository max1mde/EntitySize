package com.maximde.entitysize;

import com.maximde.entitysize.commands.EntitySizeCommand;

import com.maximde.entitysize.utils.Config;
import com.maximde.entitysize.utils.Metrics;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.Color;
import java.util.*;

@Getter
public final class EntitySize extends JavaPlugin implements Listener {

    private Config configuration;
    private EntityModifierService modifierService;
    private final ChatColor primaryColor = ChatColor.of(new Color(255, 157, 88));
    private final Map<UUID, Boolean> pendingResets = new HashMap<>();
    private static final String PENDING_RESETS_PATH = "PendingResets";

    @Override
    public void onEnable() {
        this.configuration = new Config();
        this.modifierService = new EntityModifierService(this);

        if(configuration.isBStats()) {
            new Metrics(this, 21739);
        }

        var command = getCommand("entitysize");
        var commandExecutor = new EntitySizeCommand(this);
        Objects.requireNonNull(command).setExecutor(commandExecutor);
        command.setTabCompleter(commandExecutor);

        getServer().getPluginManager().registerEvents(this, this);
        loadPendingResets();
    }

    public String getPermission(String permission) {
        return "EntitySize." + permission;
    }

    public void resetOfflinePlayerSize(UUID playerUUID) {
        pendingResets.put(playerUUID, true);
        savePendingResets();
    }

    private void savePendingResets() {
        pendingResets.forEach((uuid, value) ->
                configuration.setValue(PENDING_RESETS_PATH + "." + uuid.toString(), value));
        configuration.saveConfig();
    }

    private void loadPendingResets() {
        var section = configuration.getCfg().getConfigurationSection(PENDING_RESETS_PATH);
        if (section == null) return;

        section.getKeys(false).forEach(key ->
                pendingResets.put(UUID.fromString(key),
                        configuration.getCfg().getBoolean(PENDING_RESETS_PATH + "." + key)));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var playerUUID = player.getUniqueId();

        if (pendingResets.getOrDefault(playerUUID, false)) {
            modifierService.resetSize(player);
            pendingResets.remove(playerUUID);
            configuration.setValue(PENDING_RESETS_PATH + "." + playerUUID.toString(), null);
            configuration.saveConfig();
        }
    }

    public void resetSize(Player player) {
        modifierService.resetSize(player);
    }

    public void setSize(LivingEntity livingEntity, double newScale) {
        modifierService.setSize(livingEntity, newScale);
    }

    public Optional<LivingEntity> getEntity(Player player, int range) {
       return modifierService.getEntity(player, range);
    }
}