package com.maximde.entitysize;

import com.maximde.entitysize.commands.EntitySizeCommand;
import com.maximde.entitysize.utils.Config;
import com.maximde.entitysize.utils.Metrics;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;


import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public final class EntitySize extends JavaPlugin {

    private Config configuration;
    private final ChatColor primaryColor = ChatColor.of(new Color(255, 157, 88));

    @Override
    public void onEnable() {
        this.configuration = new Config();
        if(configuration.isBStats()) new Metrics(this, 21739);
        Objects.requireNonNull(getCommand("entitysize")).setExecutor(new EntitySizeCommand(this));
        Objects.requireNonNull(getCommand("entitysize")).setTabCompleter(new EntitySizeCommand(this));
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
                livingEntity.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).setBaseValue(getValidBase(0.41D, 32, newScale * configuration.getJumpMultiplier()));
                livingEntity.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).setBaseValue(Math.max(getValidBase(0.41D, 32, newScale * configuration.getJumpMultiplier()), 0.2));
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
}
