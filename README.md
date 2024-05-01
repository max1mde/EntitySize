<div align="center">

  <p><b>Supported versions: </b>1.20.5, 1.20.6</p>
  
  <img width="600" src="https://github.com/max1mde/EntitySize/assets/114857048/cdbf1c35-155c-4481-8f44-4f097c2c7a86"> <br>
  <img src="https://imgur.com/yMuZdvu.gif">
  
<br>
<br>

<img src="https://github.com/max1mde/EntitySize/assets/114857048/2288ecc1-2ed8-4e8e-814e-d4923d57bb0e">

<br>

<p>
With this plugin, you can:
Change the size of any living entity (bigger & smaller)
with optional steps for a transition.

Modify it for entities or players with a specific name, UUID, entity ID, scoreboard tag, the entity you are looking at, or entities in a specific range around you!

There are also some other optional modifiers like:
Movement speed, jump strength, step height, etc. (Look into the config)
To make it more playable for a player with a different scale.
</p>

  
</div>

# Commands

```
/entitysize reload
/entitysize <size> (Change your own size)
/entitysize player <player> <size>
/entitysize entity looking <size> (The entity you are looking at)
/entitysize entity tag <size> (All entities with a specific scoreboard tag)
/entitysize entity name <size> (All entities with a specific name)
/entitysize entity uuid <size> (Entity with that uuid)
/entitysize entity range <blocks> <size> (Entities in a specific range from your location)
```

# Config
```yml
General:
  bStats: true
Size:
  Transition: true
  TransitionSteps: 30
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
