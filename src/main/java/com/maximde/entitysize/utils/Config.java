package com.maximde.entitysize.utils;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;


@Getter
public class Config {

    private final File file = new File("plugins/EntitySize", "config.yml");
    private YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
    private boolean bStats;
    private boolean transition;
    private boolean reachMultiplier;
    private boolean stepHeightMultiplier;
    private boolean speedMultiplier;
    private boolean jumpMultiplier;
    private boolean saveFallDistanceMultiplier;
    private int transitionSteps;

    public Config() {
        setDefault("General.bStats", true);
        setDefault("Size.Transition", true);
        setDefault("Size.TransitionSteps", 30);
        setDefault("Size.ReachMultiplier", true);
        setDefault("Size.StepHeightMultiplier", true);
        setDefault("Size.SpeedMultiplier", true);
        setDefault("Size.JumpMultiplier", true);
        setDefault("Size.SaveFallDistanceMultiplier", true);
        saveConfig();
        initValues();
    }

    private void initValues() {
        this.bStats = cfg.getBoolean("General.bStats");
        this.transition = cfg.getBoolean("Size.Transition");
        this.reachMultiplier = cfg.getBoolean("Size.ReachMultiplier");
        this.stepHeightMultiplier = cfg.getBoolean("Size.StepHeightMultiplier");
        this.speedMultiplier = cfg.getBoolean("Size.SpeedMultiplier");
        this.jumpMultiplier = cfg.getBoolean("Size.JumpMultiplier");
        this.saveFallDistanceMultiplier = cfg.getBoolean("Size.SaveFallDistanceMultiplier");
        this.transitionSteps = cfg.getInt("Size.TransitionSteps");
    }

    public void reload() {
        this.cfg = YamlConfiguration.loadConfiguration(file);
        initValues();
    }

    @SneakyThrows
    public void saveConfig() {
        cfg.save(file);
    }

    private void setDefault(String path, Object value) {
        if(!cfg.isSet(path)) setValue(path, value);
    }

    public void setValue(String path, Object value) {
        this.cfg.set(path, value);
        saveConfig();
        reload();
    }

    public Object getValue(String path) {
        return this.cfg.get(path);
    }

}

