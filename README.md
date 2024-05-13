<div align="center">

# Provi's Health Bars
[![](https://img.shields.io/jitpack/version/com.github.Provismet/ProviHealth?style=flat-square&logo=jitpack&color=F6F6F6)](https://jitpack.io/#Provismet/ProviHealth) [![](https://img.shields.io/modrinth/dt/4wDQsby8?style=flat-square&logo=modrinth&color=F6F6F6)](https://modrinth.com/mod/provis-health-bars) [![](https://img.shields.io/curseforge/dt/945133?style=flat-square&logo=curseforge&color=F6F6F6)](https://www.curseforge.com/minecraft/mc-mods/provihealth)

</div>

Adds health bar damage indicators to Minecraft.  

Provi's Health Bars adds both a HUD health bar for your current target and in-world health bars for all mobs. The behaviour of both can be customised extensively in the settings menu.

The HUD health bar changes appearance based on the Entity Group of the mob you're targetting. Other mods can use the API entrypoint of this one to add additional HUD portraits and icons for custom Entity Groups, or even specific mobs.

## Dependencies
To access the settings menu you will need:
- [Mod Menu](https://github.com/TerraformersMC/ModMenu)
- [Cloth Config](https://github.com/shedaniel/cloth-config)

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

## Official Downloads
- Github (here)
- [Modrinth](https://modrinth.com/mod/provis-health-bars)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/provihealth)
