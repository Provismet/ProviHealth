# Provi's Health Bars

Adds health bar damage indicators to Minecraft.  

Provi's Health Bars adds both a HUD health bar for your current target and in-world health bars for all mobs. The behaviour of both can be customised extensively in the settings menu.

The HUD health bar changes appearance based on the Entity Group of the mob you're targetting. Other mods can use the API entrypoint of this one to add additional HUD portraits and icons for custom Entity Groups, or even specific mobs.

## Dependencies
To access the settings menu you will need:
- [Mod Menu](https://github.com/TerraformersMC/ModMenu)
- [Cloth Config](https://github.com/shedaniel/cloth-config)

## License
The mod distributables and source code are licensed Apache Version 2.0.  
The original assets (including health bar graphics and HUD portraits) are licensed All Rights Reserved.

## Dependency and API
This mod can be used as a dependency via [Jitpack](https://jitpack.io/#Provismet/ProviHealth).

Example entrypoint usage:  
This entrypoint initialiser is what the mod uses to set its own values for the vanilla Entity Groups.
```java
public class SelfApiHook implements ProviHealthApi {
    @Override
    public void onInitialize () {
        this.registerIcon(EntityGroup.AQUATIC, Items.COD);
        this.registerIcon(EntityGroup.ARTHROPOD, Items.COBWEB);
        this.registerIcon(EntityGroup.ILLAGER, Items.IRON_AXE);
        this.registerIcon(EntityGroup.UNDEAD, Items.ROTTEN_FLESH);

        this.registerPortrait(EntityGroup.AQUATIC, ProviHealthClient.identifier("textures/gui/healthbars/aquatic.png"));
        this.registerPortrait(EntityGroup.ARTHROPOD, ProviHealthClient.identifier("textures/gui/healthbars/arthropod.png"));
        this.registerPortrait(EntityGroup.ILLAGER, ProviHealthClient.identifier("textures/gui/healthbars/illager.png"));
        this.registerPortrait(EntityGroup.UNDEAD, ProviHealthClient.identifier("textures/gui/healthbars/undead.png"));
    }
    
}
```

Register your entrypoint:
```json
"entrypoints": {
  "provihealth": ["path.to.your.class"]
}
```
