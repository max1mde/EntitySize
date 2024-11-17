<div align="center">
	
<a href="https://jitpack.io/#max1mde/EntitySize">
<img src="https://jitpack.io/v/max1mde/EntitySize.svg">
</a>

  <p><b>Supported versions: </b>1.20.5, 1.20.6, 1.21.x</p>
  
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

This plugin overrides the vanilla player attributes!
</p>

  
</div>

# Commands

```
/entitysize reload (Reload config)
/entitysize reset <optional player / @a> (Reset size to default)
/entitysize <size> [time] (Change your own size)
/entitysize player <player> <size> [time]
/entitysize entity looking <size> [time] (The entity you are looking at)
/entitysize entity tag <tag> <size> [time] (All entities with a specific scoreboard tag)
/entitysize entity name <name> <size> [time] (All entities with a specific name)
/entitysize entity uuid <uuid> <size> [time] (Entity with that uuid)
/entitysize entity range <blocks> <size> [time] (Entities in a specific range from your location)
```

# Config
```yml
General:
  bStats: true
Size:
  Transition: true
  TransitionSteps: 30
  IsReachMultiplier: true
  IsStepHeightMultiplier: true
  IsSpeedMultiplier: true
  IsJumpMultiplier: true
  IsSaveFallDistanceMultiplier: true
  ReachMultiplier: 1
  StepHeightMultiplier: 1
  SpeedMultiplier: 1
  JumpMultiplier: 1
  SaveFallDistanceMultiplier: 1
PendingResets: {}
```

# Permissions
```
permissions:
  entitysize.commands:
    description: Allows the use of the entitysize command
    default: true
  entitysize.player:
    description: Allows the use of the player subcommand
    default: false
  entitysize.entity:
    description: Allows the use of the entity subcommand
    default: false
  entitysize.reload:
    description: Allows the use of the reload subcommand
    default: false
  entitysize.reset:
    description: Allows the use of the reset subcommand
    default: true
  entitysize.reset.player:
    description: Allows the ability to reset any player
    default: false
  entitysize.reset.all:
    description: Allows the ability to reset all players, using @a
    default: false
  entitysize.self:
    description: Allows the use of the self subcommand
    default: true
  entitysize.entity.looking:
    description: Allows selecting the entity being looked at
    default: false
  entitysize.entity.tag:
    description: Allows selecting the entity by tag
    default: false
  entitysize.entity.name:
    description: Allows selecting the entity by name
    default: false
  entitysize.entity.uuid:
    description: Allows selecting the entity by uuid
    default: false
  entitysize.entity.range:
    description: Allows selecting the entity by range
    default: false
```

# API
Gradle
```
repositories {
	mavenCentral()
	maven { url 'https://jitpack.io' }
}

dependencies {
	 implementation 'com.github.max1mde:EntitySize:1.5.4'
}
```

Add 

```java
EntityModifierService modifierService = EntitySize.getSizeService();

modifierService.resetSize(player);
modifierService.setSize(livingEntity, newScale);
modifierService.getEntity(player, range);
```

# Support
https://discord.com/invite/4pA7VUeQs4
