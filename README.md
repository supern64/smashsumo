# SmashSumo
Sumo but... Smash?

## Download
Available from the [Releases](https://github.com/supern64/smashsumo/releases) section.

> [!WARNING]
> This plugin is only compatible with Minecraft 1.8.8 (v1_8_R3) due to hardcoded NMS calls and other differences.  
> A version compatible with the latest version can be found in the `modern` branch, but updates are not guaranteed.

## Usage
You must install [ktlibs-kotlin-stdlib](https://modrinth.com/plugin/ktlibs-kotlin-stdlib) first.  
After downloading this plugin and `ktlibs`, put both JARs in the `plugins` folder of your server.  

Build an arena, and register it by
1. Stand at the center of your arena.
2. Running `/ss arena create [name]`, with a name of your choice.
3. Move yourself to where you want players to spawn, then run `/ss arena spawn`
   (Players spawn in a circle around the center)
4. Move yourself to the height you want players to respawn, then run `/ss arena spawn`
5. Move yourself to where you want the side barrier to be, then run `/ss arena side`
   (The barrier is a cylinder around the center)
6. Move yourself to where you want the bottom barrier to be, then run `/ss arena bottom`
   (This is the bottom of the cylinder)
7. (Optional) Move yourself to where you want the top barrier to be, then run `/ss arena top`
   (This is the top of the cylinder)
8. Save the arena with `/ss arena save`  

After you have an arena set up, you should be able to join the arena with `/ss join [name]`.  
Then, after at least 2 players have joined, you can start the game by typing `/ss start`.  
Players can leave at any time by typing `/ss leave`.

## Configuration
See [here](https://github.com/supern64/smashsumo/blob/master/src/main/resources/config.yml).

## Permissions
```
smashsumo.use - all use of the plugin
smashsumo.gm - starting games (/ss start)
smashsumo.admin - manage arenas and reload configuration
```

## Build Instructions
After you have cloned the repository, you must acquire a full Spigot 1.8.8 JAR via [BuildTools](https://www.spigotmc.org/wiki/buildtools/).  
Place the JAR into the `libs` folder, then running `./gradlew shadowJar` should build the plugin.  
The output will be in the `build/libs` folder.

## Acknowledgements
This project uses
- TheLuca98's TextAPI, available [here](https://github.com/TheLuca98/TextAPI)
- dejvokep's BoostedYAML, available [here](https://github.com/dejvokep/boosted-yaml)