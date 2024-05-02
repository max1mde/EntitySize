package com.maximde.entitysize.commands;

import com.maximde.entitysize.EntitySize;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.function.Predicate;


public class EntitySizeCommand implements CommandExecutor, TabCompleter {
    private final EntitySize entitySize;
    public EntitySizeCommand(EntitySize entitySize) {
        this.entitySize = entitySize;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!sender.hasPermission(entitySize.getPermission("commands"))) {
            sender.sendMessage(entitySize.getPrimaryColor() + "EntitySize by MaximDe v" + entitySize.getDescription().getVersion());
            return false;
        }

        if(args.length < 1) return sendCommands(sender);

        switch (args[0].toLowerCase()) {
            case "player" -> {
                if(args.length < 3) return sendCommands(sender);
                Player target = Bukkit.getPlayer(args[1]);
                if(target == null || !target.isOnline()) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "Player with the name " + args[1] + " not found!");
                    return false;
                }

                try {
                    double size = Double.parseDouble(args[2]);
                    entitySize.setSize(target, size);
                    sender.sendMessage(entitySize.getPrimaryColor() + "Successfully changed the size of " + target.getName());
                } catch (NumberFormatException e) {
                    sender.sendMessage(entitySize.getPrimaryColor() + args[2] + " is not a valid double number!");
                    e.printStackTrace();
                } catch (Exception e) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "An error occurred while changing the size.");
                    e.printStackTrace();
                }
            }

            case "entity" -> {
                if(args.length < 2) return sendCommands(sender);
                handleEntityArg(sender, args);
            }

            case "reload" -> {
                entitySize.getConfiguration().reload();
                sender.sendMessage(entitySize.getPrimaryColor() + "Config reloaded!");
            }

            default -> {
                try {
                    double size = Double.parseDouble(args[0]);
                    Player player = (Player) sender;
                    entitySize.setSize(player, size);
                    sender.sendMessage(entitySize.getPrimaryColor() + "Successfully changed the size of " + player.getName());
                } catch (Exception exception) {
                    sendCommands(sender);
                }
            }
        }

        return false;
    }

    private void handleEntityArg(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendCommands(sender);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "looking" -> {

                double size = Double.parseDouble(args[2]);

                if(!(sender instanceof Player player)) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "You can only execute this command as a player!");
                    return;
                }

                Optional<LivingEntity> optionalEntity = entitySize.getEntity(player, 30);

                if(optionalEntity.isEmpty()) {
                    player.sendMessage(entitySize.getPrimaryColor() + "No entity found!");
                    return;
                }

                handleEntities(entity -> optionalEntity.get() == entity, size);
                sender.sendMessage(entitySize.getPrimaryColor() + "Success!");
            }
            case "tag" -> {
                if (args.length < 4) {
                    sendCommands(sender);
                    return;
                }
                String tag = args[2];
                double size = Double.parseDouble(args[3]);
                handleEntities(entity -> entity.getScoreboardTags().contains(tag), size);
                sender.sendMessage(entitySize.getPrimaryColor() + "Success!");
            }
            case "name" -> {
                if (args.length < 4) {
                    sendCommands(sender);
                    return;
                }
                String name = args[2];
                double size = Double.parseDouble(args[3]);
                handleEntities(entity -> (entity.getCustomName() != null && entity.getCustomName().equalsIgnoreCase(name)) || entity.getName().equalsIgnoreCase(name), size);
                sender.sendMessage(entitySize.getPrimaryColor() + "Success!");
            }
            case "uuid" -> {
                if (args.length < 4) {
                    sendCommands(sender);
                    return;
                }
                UUID uuid = UUID.fromString(args[2]);
                double size = Double.parseDouble(args[3]);
                handleEntities(entity -> entity.getUniqueId().equals(uuid), size);
                sender.sendMessage(entitySize.getPrimaryColor() + "Success!");
            }
            case "range" -> {
                if (args.length < 4) {
                    sendCommands(sender);
                    return;
                }
                if(!(sender instanceof Player player)) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "You can only execute this command as a player!");
                    return;
                }

                double range = Double.parseDouble(args[2]);
                double size = Double.parseDouble(args[3]);
                handleEntities(entity -> isWithinRange(entity, player, range), size);
                sender.sendMessage(entitySize.getPrimaryColor() + "Success!");
            }
            default -> sendCommands(sender);
        }
    }

    private boolean isWithinRange(Entity entity, Player player, double range) {
        return player.getNearbyEntities(range, range, range).contains(entity) || entity == player;
    }

    private void handleEntities(Predicate<Entity> condition, double size) {
        for(World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (condition.test(entity) && entity instanceof LivingEntity livingEntity) {
                    entitySize.setSize(livingEntity, size);
                }
            }
        }
    }

    private boolean sendCommands(CommandSender sender) {
        sender.sendMessage(entitySize.getPrimaryColor() +
                "/entitysize reload (Reload config)\n" +
                "/entitysize <size> (Change your own size)\n" +
                "/entitysize player <player size>\n" +
                "/entitysize entity looking (The entity you are looking at)\n" +
                "/entitysize entity tag (All entities with a specific scoreboard tag)\n" +
                "/entitysize entity name (All entities with a specific name)\n" +
                "/entitysize entity uuid (Entity with that uuid)\n" +
                "/entitysize entity range <blocks> (Entities in a specific range from your location)\n");
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player player) {
            if(!player.hasPermission(entitySize.getPermission("commands"))) {
                return new ArrayList<>();
            }
        }

        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();


        if (args.length == 1) {
            commands.addAll(Arrays.asList("<size>", "reload", "player", "entity"));
            StringUtil.copyPartialMatches(args[0], commands, completions);
        }

        if (args.length == 2) {
            if(args[0].equalsIgnoreCase("player")) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    commands.add(player.getName());
                });
            }
            if(args[0].equalsIgnoreCase("entity")) {
                commands.addAll(Arrays.asList("looking", "tag", "name", "range"));
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        }

        if (args.length == 3) {
            if(args[0].equalsIgnoreCase("player")) commands.add("<size>");
            if(args[0].equalsIgnoreCase("entity")) {
                switch (args[1].toLowerCase()) {
                    case "tag" -> commands.add("<tag>");
                    case "name" -> commands.add("<name>");
                    case "looking" -> commands.add("<size>");
                    case "uuid" -> commands.add("<uuid>");
                    case "range" -> commands.add("<range>");
                }
            }
            StringUtil.copyPartialMatches(args[2], commands, completions);
        }

        return completions;
    }
}
