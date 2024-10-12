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
import org.bukkit.scheduler.BukkitRunnable;
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
                if(!sender.hasPermission(entitySize.getPermission("player"))) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "You don't have the permission to execute this subcommand!");
                    return false;
                }
                if(args.length < 3) return sendCommands(sender);
                Player target = Bukkit.getPlayer(args[1]);
                if(target == null || !target.isOnline()) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "Player with the name " + args[1] + " not found!");
                    return false;
                }

                try {
                    double size = Double.parseDouble(args[2]);
                    int time = -1;
                    if (args.length >= 4) {
                        time = Integer.parseInt(args[3]);
                    }
                    setSize(target, size, time);
                    sender.sendMessage(entitySize.getPrimaryColor() + "Successfully changed the size of " + target.getName()
                            + (time > 0 ? " (Resetting in " + time + "minute/s )" : ""));
                } catch (NumberFormatException e) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "Invalid number format!");
                    e.printStackTrace();
                } catch (Exception e) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "An error occurred while changing the size.");
                    e.printStackTrace();
                }
            }

            case "entity" -> {
                if(!sender.hasPermission(entitySize.getPermission("entity"))) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "You don't have the permission to execute this command!");
                    return false;
                }
                if(args.length < 2) return sendCommands(sender);
                boolean success = handleEntityArg(sender, args);
                if(!success) return false;
            }

            case "reload" -> {
                if(!sender.hasPermission(entitySize.getPermission("reload"))) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "You don't have the permission to execute this command!");
                    return false;
                }
                entitySize.getConfiguration().reload();
                sender.sendMessage(entitySize.getPrimaryColor() + "Config reloaded!");
            }

            case "reset" -> {
                if(!sender.hasPermission(entitySize.getPermission("reset"))) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "You don't have the permission to execute this command!");
                    return false;
                }
                if(args.length == 1) {
                    entitySize.getConfiguration().reload();
                    entitySize.resetSize((Player)sender);
                    sender.sendMessage(entitySize.getPrimaryColor() + "Size reset!");
                    return false;
                } else if (args.length == 2) {
                    if(args[1].equalsIgnoreCase("@a")) {
                        if(!sender.hasPermission(entitySize.getPermission("reset.all"))) {
                            sender.sendMessage(entitySize.getPrimaryColor() + "You don't have the permission to execute this command!");
                            return false;
                        }
                        Bukkit.getOnlinePlayers().forEach(entitySize::resetSize);
                        sender.sendMessage(entitySize.getPrimaryColor() + "Size reset for " + Bukkit.getOnlinePlayers().size()+" players!");
                        return true;
                    }
                    if(!sender.hasPermission(entitySize.getPermission("reset.player"))) {
                        sender.sendMessage(entitySize.getPrimaryColor() + "You don't have the permission to execute this command!");
                        return false;
                    }

                    Player target = Bukkit.getPlayer(args[1]);
                    if(target == null || !target.isOnline()) {
                        sender.sendMessage(entitySize.getPrimaryColor() + "Player with the name " + args[1] + " not found!");
                        return false;
                    }
                    entitySize.resetSize(target);
                    sender.sendMessage(entitySize.getPrimaryColor() + "Size reset for " + target.getName());
                    return true;
                }
                sendCommands(sender);
                return false;
            }

            default -> {
                try {
                    if(!sender.hasPermission(entitySize.getPermission("self"))) {
                        sender.sendMessage(entitySize.getPrimaryColor() + "You don't have the permission to execute this command!");
                        return false;
                    }
                    double size = Double.parseDouble(args[0]);
                    int time = -1;
                    if (args.length >= 2) {
                        time = Integer.parseInt(args[1]);
                    }
                    Player player = (Player) sender;
                    setSize(player, size, time);
                    sender.sendMessage(entitySize.getPrimaryColor() + "Successfully changed the size of " + player.getName()
                            + (time > 0 ? " (Resetting in " + time + " minute/s)" : ""));
                } catch (Exception exception) {
                    sendCommands(sender);
                }
            }
        }

        return false;
    }

    private boolean handleEntityArg(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendCommands(sender);
            return false;
        }

        switch (args[1].toLowerCase()) {
            case "looking" -> {
                if(!sender.hasPermission(entitySize.getPermission("entity.looking"))) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "You don't have the permission to execute this command!");
                    return false;
                }

                double size = Double.parseDouble(args[2]);
                int time = -1;
                if (args.length >= 4) {
                    time = Integer.parseInt(args[3]);
                }

                if(!(sender instanceof Player player)) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "You can only execute this command as a player!");
                    return false;
                }

                Optional<LivingEntity> optionalEntity = entitySize.getEntity(player, 30);

                if(optionalEntity.isEmpty()) {
                    player.sendMessage(entitySize.getPrimaryColor() + "No entity found!");
                    return false;
                }
                sender.sendMessage(entitySize.getPrimaryColor() + "Changing the size of the entity in front of you! (" + size + ")");
                handleEntities(entity -> optionalEntity.get() == entity, size, time, sender);

                return true;
            }
            case "tag" -> {
                if(!sender.hasPermission(entitySize.getPermission("entity.tag"))) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "You don't have the permission to execute this command!");
                    return false;
                }
                if (args.length < 4) {
                    sendCommands(sender);
                    return false;
                }
                String tag = args[2];
                double size = Double.parseDouble(args[3]);
                int time = -1;
                if (args.length >= 5) {
                    time = Integer.parseInt(args[4]);
                }
                sender.sendMessage(entitySize.getPrimaryColor() + "Changing the size of all entities with the scoreboard tag: "+tag+"!");
                handleEntities(entity -> entity.getScoreboardTags().contains(tag), size, time, sender);

                return true;
            }
            case "name" -> {
                if(!sender.hasPermission(entitySize.getPermission("entity.name"))) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "You don't have the permission to execute this command!");
                    return false;
                }
                if (args.length < 4) {
                    sendCommands(sender);
                    return false;
                }
                String name = args[2];
                double size = Double.parseDouble(args[3]);
                int time = -1;
                if (args.length >= 5) {
                    time = Integer.parseInt(args[4]);
                }
                sender.sendMessage(entitySize.getPrimaryColor() + "Changing the size of all entities with the name: "+name+"!");
                handleEntities(entity -> (entity.getCustomName() != null && entity.getCustomName().equalsIgnoreCase(name)) || entity.getName().equalsIgnoreCase(name), size, time, sender);

                return true;
            }
            case "uuid" -> {
                if(!sender.hasPermission(entitySize.getPermission("entity.uuid"))) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "You don't have the permission to execute this command!");
                    return false;
                }
                if (args.length < 4) {
                    sendCommands(sender);
                    return false;
                }
                UUID uuid = UUID.fromString(args[2]);
                double size = Double.parseDouble(args[3]);
                int time = -1;
                if (args.length >= 5) {
                    time = Integer.parseInt(args[4]);
                }
                sender.sendMessage(entitySize.getPrimaryColor() + "Changing the size of all entities with the uuid: "+uuid+"!");
                handleEntities(entity -> entity.getUniqueId().equals(uuid), size, time, sender);
                return true;
            }
            case "range" -> {
                if(!sender.hasPermission(entitySize.getPermission("entity.range"))) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "You don't have the permission to execute this command!");
                    return false;
                }
                if (args.length < 4) {
                    sendCommands(sender);
                    return false;
                }
                if(!(sender instanceof Player player)) {
                    sender.sendMessage(entitySize.getPrimaryColor() + "You can only execute this command as a player!");
                    return false;
                }

                double range = Double.parseDouble(args[2]);
                double size = Double.parseDouble(args[3]);
                int time = -1;
                if (args.length >= 5) {
                    time = Integer.parseInt(args[4]);
                }
                sender.sendMessage(entitySize.getPrimaryColor() + "Changing the size of all entities which are within the range of: "+range+" blocks!");
                handleEntities(entity -> isWithinRange(entity, player, range), size, time, sender);
                return true;
            }
            default -> {sendCommands(sender); return false;}
        }
    }

    private boolean isWithinRange(Entity entity, Player player, double range) {
        return player.getNearbyEntities(range, range, range).contains(entity) || entity == player;
    }

    private void handleEntities(Predicate<Entity> condition, double size, int time, CommandSender commandSender) {
        List<UUID> affectedEntities = new ArrayList<>();
        for(World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (condition.test(entity) && entity instanceof LivingEntity livingEntity) {
                    setSize(livingEntity, size, time);
                    if(entity instanceof Player) {
                        affectedEntities.add(entity.getUniqueId());
                    }
                }
            }
        }
        if (time > 0) {
            scheduleReset(affectedEntities, time);
            commandSender.sendMessage(this.entitySize.getPrimaryColor() + "Resetting the size of all players from " + affectedEntities.size() + " affected entities in " + time + " minute/s!");
        }
    }

    private void setSize(LivingEntity entity, double size, int time) {
        entitySize.setSize(entity, size);
        if (time > 0 && entity instanceof Player player) {
            scheduleReset(Collections.singletonList(player.getUniqueId()), time);
        }
    }

    private void scheduleReset(List<UUID> entityUUIDs, int minutes) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : entityUUIDs) {
                    Player player = Bukkit.getPlayer(uuid);
                    if(player == null || !player.isOnline()) {
                        entitySize.resetOfflinePlayerSize(uuid);
                    } else {
                        entitySize.resetSize(player);
                    }
                }
            }
        }.runTaskLater(entitySize, minutes * 60 * 20L);
    }

    private boolean sendCommands(CommandSender sender) {
        sender.sendMessage(entitySize.getPrimaryColor() +
                "/entitysize reload (Reload config)\n" +
                "/entitysize reset <optional player / @a> (Reset size to default)\n" +
                "/entitysize <size> [time] (Change your own size)\n" +
                "/entitysize player <player> <size> [time]\n" +
                "/entitysize entity looking <size> [time] (The entity you are looking at)\n" +
                "/entitysize entity tag <tag> <size> [time] (All entities with a specific scoreboard tag)\n" +
                "/entitysize entity name <name> <size> [time] (All entities with a specific name)\n" +
                "/entitysize entity uuid <uuid> <size> [time] (Entity with that uuid)\n" +
                "/entitysize entity range <blocks> <size> [time] (Entities in a specific range from your location)\n");
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
            if (sender.hasPermission(entitySize.getPermission("player"))) commands.add("player");
            if (sender.hasPermission(entitySize.getPermission("entity"))) commands.add("entity");
            if (sender.hasPermission(entitySize.getPermission("reload"))) commands.add("reload");
            if (sender.hasPermission(entitySize.getPermission("reset"))) commands.add("reset");
            if (sender.hasPermission(entitySize.getPermission("self")) && sender instanceof Player) commands.add("<size>");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        }

        if (args.length == 2) {
            if(args[0].equalsIgnoreCase("player") && sender.hasPermission(entitySize.getPermission("player"))) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    commands.add(player.getName());
                });
            }
            if(args[0].equalsIgnoreCase("entity") && sender.hasPermission(entitySize.getPermission("entity"))) {
                if (sender.hasPermission(entitySize.getPermission("entity.looking"))) commands.add("looking");
                if (sender.hasPermission(entitySize.getPermission("entity.tag"))) commands.add("tag");
                if (sender.hasPermission(entitySize.getPermission("entity.name"))) commands.add("name");
                if (sender.hasPermission(entitySize.getPermission("entity.uuid"))) commands.add("uuid");
                if (sender.hasPermission(entitySize.getPermission("entity.range"))) commands.add("range");
            }
            try {
                Integer.parseInt(args[0]);
                if (sender.hasPermission(entitySize.getPermission("entity.self"))) commands.add("<reset time in minutes>");
            } catch (NumberFormatException ignore){}
            StringUtil.copyPartialMatches(args[1], commands, completions);
        }

        if (args.length == 3) {
            if(args[0].equalsIgnoreCase("player")&&sender.hasPermission(entitySize.getPermission("player"))) commands.add("<size>");
            if(args[0].equalsIgnoreCase("entity")&&sender.hasPermission(entitySize.getPermission("entity"))) {
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

        if (args.length == 4) {
            if(args[0].equalsIgnoreCase("player") && sender.hasPermission(entitySize.getPermission("player"))) commands.add("<reset time in minutes>");
            if(args[0].equalsIgnoreCase("entity") && sender.hasPermission(entitySize.getPermission("entity"))) {
                if (args[1].toLowerCase().equals("looking")) {
                    commands.add("<reset time in minutes>");

                }
                switch (args[1].toLowerCase()) {
                    case "tag", "name", "uuid", "range" -> commands.add("<size>");
                }
            }
            StringUtil.copyPartialMatches(args[3], commands, completions);
        }

        if (args.length == 5) {
            if(args[0].equalsIgnoreCase("entity")&&sender.hasPermission(entitySize.getPermission("entity"))) {
                switch (args[1].toLowerCase()) {
                    case "tag", "name", "uuid", "range" -> commands.add("<reset time in minutes>");
                }
            }
            StringUtil.copyPartialMatches(args[4], commands, completions);
        }

        return completions;
    }
}