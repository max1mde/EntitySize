<div align="center">
  <img src="https://imgur.com/yMuZdvu.gif">
</div>

With this plugin you can
Change the size of any Living entity (bigger & smaller)
with an optional steps for a transition

Modify it for entities or players with a specific name, uuid, entity id, scoreboard tag or even the entity you are looking at or entities in a specific range around you!

There area also some other optional modifiers like:
Movement speed, jump strenth, step height etc. (Look into the config)
To make it more playable for a player with a different scale.​

# Commands

```
/size reload
/size <size> (Change your own size)
/size player <player> <size>
/size entity looking <size> (The entity you are looking at)
/size entity tag <size> (All entities with a specific scoreboard tag)
/size entity name <size> (All entities with a specific name)
/size entity uuid <size> (Entity with that uuid)
/size entity range <blocks> <size> (Entities in a specific range from your location)
```

# Config
```yml
General:
  bStats: true
Size:
  Transition: true
  ReachMultiplier: true
  StepHeightMultiplier: true
  SpeedMultiplier: true
  JumpMultiplier: true
  SaveFallDistanceMultiplier: true
```

# Permissions
`EntitySize.commands`

# Support
https://discord.com/invite/4pA7VUeQs4
