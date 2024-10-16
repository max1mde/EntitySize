package com.maximde.entitysize;

import com.maximde.entitysize.commands.EntitySizeCommand;
import com.maximde.entitysize.utils.Config;
import com.maximde.entitysize.utils.Metrics;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;


import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.awt.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public final class EntitySize extends JavaPlugin implements Listener {

    private Config configuration;
    private final ChatColor primaryColor = ChatColor.of(new Color(255, 157, 88));

    private final Map<UUID, Boolean> pendingResets = new HashMap<>();
    private final String PENDING_RESETS_PATH = "PendingResets";

    @Override
    public void onEnable() {
        this.configuration = new Config();
        if(configuration.isBStats()) new Metrics(this, 21739);
        Objects.requireNonNull(getCommand("entitysize")).setExecutor(new EntitySizeCommand(this));
        Objects.requireNonNull(getCommand("entitysize")).setTabCompleter(new EntitySizeCommand(this));
        getServer().getPluginManager().registerEvents(this, this);
        loadPendingResets();
    }

    public String getPermission(String permission) {
        return "EntitySize." + permission;
    }

    public void setSize(LivingEntity livingEntity, double newScale) {
        final double currentScale = Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_SCALE)).getBaseValue();
        if(currentScale == newScale) return;
        boolean bigger = newScale > currentScale;
        if(this.configuration.isTransition()) {
            double stepSize = Math.abs(newScale - currentScale) / configuration.getTransitionSteps();
            AtomicReference<Double> scale = new AtomicReference<>(currentScale);
            getServer().getScheduler().runTaskTimer(this, task -> {
                scale.updateAndGet(v -> bigger ? v + stepSize : v - stepSize);
                if(scale.get() == currentScale) return;
                if ((bigger && scale.get() >= newScale) || (!bigger && scale.get() <= newScale)) {
                    task.cancel();
                }
                Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_SCALE)).setBaseValue(scale.get());
            }, 0, 1);
        } else {
            if (livingEntity.getAttribute(Attribute.GENERIC_SCALE) != null) {
                livingEntity.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(newScale);
            }
        }

        if (this.configuration.isJumpMultiplier()) {
            if (livingEntity.getAttribute(Attribute.GENERIC_JUMP_STRENGTH) != null) {
                livingEntity.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).setBaseValue(getValidBase(0.42D, 32, newScale * configuration.getJumpMultiplier()));
                livingEntity.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).setBaseValue(Math.max(getValidBase(0.42D, 32, newScale * configuration.getJumpMultiplier()), 0.2));
            }
        }
        if (this.configuration.isReachMultiplier()) {
            if (livingEntity.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE) != null) {
                livingEntity.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE).setBaseValue(getValidBase(4.5D, 64, newScale * configuration.getReachMultiplier()));
            }
            if (livingEntity.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE) != null) {
                livingEntity.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE).setBaseValue(getValidBase(3D, 64, newScale * configuration.getReachMultiplier()));
            }
        }
        if (this.configuration.isSpeedMultiplier()) {
            if (livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
                livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(Math.max(getValidBase(0.1D, 1024, newScale * configuration.getSpeedMultiplier()), 0.03));
            }
        }
        if (this.configuration.isStepHeightMultiplier()) {
            if (livingEntity.getAttribute(Attribute.GENERIC_STEP_HEIGHT) != null) {
                livingEntity.getAttribute(Attribute.GENERIC_STEP_HEIGHT).setBaseValue(getValidBase(0.6D, 10, newScale * configuration.getStepHeightMultiplier()));
            }
        }

        if (this.configuration.isSaveFallDistanceMultiplier()) {
            if (livingEntity.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE) != null) {
                livingEntity.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE).setBaseValue(getValidBase(3.0D, 1024, newScale * configuration.getSaveFallDistanceMultiplier()));
            }
        }
    }

    private double getValidBase(double defaultValue, double maxValue, double multiplier) {
        double finalVal = defaultValue * multiplier;
        if(finalVal > maxValue) finalVal = maxValue;
        if(finalVal < 0) finalVal = 0.01F;
        return finalVal;
    }

    public Optional<LivingEntity> getEntity(Player player, int range){
        long time = System.currentTimeMillis();
        Vector playerLookDir = player.getEyeLocation().getDirection();
        Vector playerEyeLocation = player.getEyeLocation().toVector();
        LivingEntity bestEntity = null;
        float bestAngle = 0.4f;
        for(Entity e : player.getNearbyEntities(range, range, range)){
            if(!player.hasLineOfSight(e)) continue;
            if(!(e instanceof LivingEntity livingEntity)) continue;
            Vector entityLoc = e.getLocation().toVector();
            Vector playerToEntity = entityLoc.subtract(playerEyeLocation);
            if(playerLookDir.angle(playerToEntity) < bestAngle){
                bestAngle = playerLookDir.angle(playerToEntity);
                bestEntity = livingEntity;
            }
        }
        if(bestEntity == null) return Optional.empty();
        return Optional.of(bestEntity);
    }

    public void resetSize(Player player) {
        if(player.getAttribute(Attribute.GENERIC_SCALE) != null) {
            player.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1);
        }

        if (player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH) != null) {
            player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).setBaseValue(0.42D);
        }
        if (player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE) != null) {
            player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE).setBaseValue(4.5D);
        }
        if (player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE) != null) {
            player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE).setBaseValue(3D);
        }
        if (player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1D);
        }
        if (player.getAttribute(Attribute.GENERIC_STEP_HEIGHT) != null) {
            player.getAttribute(Attribute.GENERIC_STEP_HEIGHT).setBaseValue(0.6D);
        }
        if (player.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE) != null) {
            player.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE).setBaseValue(3.0D);
        }
        pendingResets.remove(player.getUniqueId());
        this.configuration.setValue(PENDING_RESETS_PATH + "." + player.getUniqueId().toString(), null);
        this.configuration.saveConfig();
    }

    public void resetOfflinePlayerSize(UUID playerUUID) {
        pendingResets.put(playerUUID, true);
        savePendingResets();
    }

    private void savePendingResets() {
        for (Map.Entry<UUID, Boolean> entry : pendingResets.entrySet()) {
            this.configuration.setValue(PENDING_RESETS_PATH + "." + entry.getKey().toString(), entry.getValue());
        }
        this.configuration.saveConfig();
    }

    private void loadPendingResets() {
        if (this.configuration.getValue(PENDING_RESETS_PATH) == null) return;

        for (String key : this.configuration.getCfg().getConfigurationSection(PENDING_RESETS_PATH).getKeys(false)) {
            pendingResets.put(UUID.fromString(key), this.configuration.getCfg().getBoolean(PENDING_RESETS_PATH + "." + key));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (pendingResets.containsKey(playerUUID) && pendingResets.get(playerUUID)) {
            resetSize(player);
            pendingResets.remove(playerUUID);
            this.configuration.setValue(PENDING_RESETS_PATH + "." + playerUUID.toString(), null);
            this.configuration.saveConfig();
        }
    }
}
