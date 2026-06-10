# Kazhi — Runtime-Injectable Util Client (MC 1.21.8 / NetEase PC)

A utility client that is **injected into an already-running** Minecraft 1.21.8 NeoForge instance (NetEase PC). It is **not a mod**: nothing goes in the `mods/` folder and the game does **not** need to restart. The injector attaches to the live JVM and rewrites the loaded Minecraft classes in memory (Java's `Instrumentation.retransformClasses` + ASM), then defines the client classes directly into the game's class loader.

**Singleplayer use only.** Disable modules before joining multiplayer servers. On multiplayer, all modules except **Client** (HUD/settings) are auto-disabled.

## How it works

```
KazhiInjector.exe ──attach──> live Minecraft JVM
        │
        ├─ defines payload classes into the game class loader
        │     (KazhiHooks, modules, ClickGUI, config)
        └─ retransforms loaded classes with ASM:
              Minecraft#runTick       -> KazhiHooks.onClientTick()
              Gui#render              -> KazhiHooks.onHudRender()        (HUD overlays)
              GameRenderer#getFov     -> *= KazhiHooks.fovMul()         (Zoom)
              GameRenderer#bobHurt    -> skipped when noHurtCam()        (NoHurtCam)
              Block#shouldRenderFace  -> KazhiHooks.xrayOverride()       (XRay)
              LightTexture#getBrightness -> KazhiHooks.applyFullbright() (Fullbright)
```

The features go live immediately. Movement, weather, and build tools run from the per-frame `onClientTick` hook.

## Requirements

- A running **NetEase PC** instance of **Minecraft 1.21.8** with **NeoForge 21.8.x**.
- To build: **JDK 21** (64-bit). To run the EXE: nothing (it bundles its own runtime).

The payload is compiled against MC `1.21.8` (NeoForge `21.8.53`, used only for the vanilla Minecraft classes).

## Easiest: the auto-inject EXE

Build the native launcher (bundles its own Java runtime):

```bat
gradlew :injector:jpackageExe
```

Output: `injector\build\exe\KazhiInjector\KazhiInjector.exe`

To use it:

1. Start Minecraft from the NetEase launcher and wait at the **main menu** (or in a singleplayer world).
2. Double-click **`KazhiInjector.exe`**.

It auto-finds the running Minecraft process and injects live. **No restart.** Switch back to the game and press **Insert** (default) to open the ClickGUI.

If the game isn't open yet, the EXE waits up to 90 seconds for it to appear.

> Keep `KazhiInjector.exe`, `app/`, and `runtime/` together if you move the folder. The agent reads `kazhi-client*.jar` from `app/`.

## Build everything

```bat
gradlew build
gradlew :injector:dist
```

`:injector:dist` gathers the three JARs into `injector/build/dist/`.

## Controls

| Action | Default key |
|--------|-------------|
| Open ClickGUI | **Insert** |
| Zoom (hold, when Zoom is ON) | **C** |
| Panic (disable all modules) | **End** |
| BoxFill region pos1 / pos2 | **[** / **]** |
| Undo last BoxFill | **U** |
| Module keybinds | Set in the ClickGUI |

Rebind menu, zoom, panic, and BoxFill keys in **Client → Settings** (right-click the Settings module).

In the ClickGUI: category tabs, search box, click a module to toggle, click the key box to bind, right-click `*` modules for settings. Settings save to `<gameDir>/config/kazhi/settings.json`.

## Features

### Movement
- **Flight** — creative-style flight for building
- **NoFall** — no fall damage (integrated singleplayer)
- **AutoSprint** — sprint while moving forward

### Render
- **XRay** — ores through walls (per-ore toggles in settings)
- **Fullbright** — adjustable brightness (slider in Settings)
- **Zoom** — hold zoom key; strength in Settings
- **NoHurtCam** — removes hurt camera shake
- **NoFog** — disables distance fog
- **NoWeather** — clears rain/thunder client-side
- **ChunkBounds** — chunk coordinates and distance to borders (HUD)

### Build
- **BoxFill** — creative box/region fill GUI; undo stack; corner preview particles; pos1/pos2 hotkeys

### Client
- **CoordsHUD** — XYZ, dimension, biome overlay
- **Settings** — global keys, zoom/fullbright strength, box preview toggle

### Safety
- **Multiplayer guard** — disables all modules except Client category on remote servers
- **Panic key** — disables every module instantly

## Project layout

```
client/     Payload: modules, ClickGUI, config, runtime hooks (compiled vs Minecraft, no NeoForge)
agent/      Attach agent: ASM transformer + class-loader injection
injector/   Attach CLI + native EXE packaging
```

## Notes & limitations

- This is **not a mod** and uses no mixins. It relies on NeoForge running with Mojang-official names at runtime (true for 1.21.x).
- Inject from the **main menu or a loaded world**. Toggling XRay forces a chunk re-render automatically.
- **New ASM hook sites** (e.g. after updating the agent) require **re-injecting**; the game does not need a restart.
- Targets **MC 1.21.8** exactly. Method signatures differ between patch versions.
- Diagnostics are written to `<user home>/kazhi-debug.log`.

## Disclaimer

For educational and singleplayer use only. Using utility clients on multiplayer servers may violate server rules or terms of service.
