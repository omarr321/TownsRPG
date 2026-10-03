<p align="center">
  <img src="https://raw.githubusercontent.com/omarr321/TownsRPG/main/src/main/resources/branding/logo.svg" alt="Towns RPG Engine" width="520">
</p>

<p align="center">
  A 2D Java engine for first-person, room-by-room RPGs, with pseudo-3D rooms and a pixel-accurate lighting system.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-orange" alt="Java 25">
  <img src="https://img.shields.io/badge/Build-Maven-blue" alt="Maven">
  <img src="https://img.shields.io/badge/Status-Pre--alpha-red" alt="Alpha">
  <img src="https://img.shields.io/badge/Current_Release-None-red" alt="None">
</p>

> [!NOTE]
> This project is currently under active development. No stable releases are available yet, and APIs or features may change without notice. Use at your own risk.

## What is it?

TownsRPG Engine is a small 2D Java Swing engine that draws first-person, 3D-styled rooms. You stand in the middle of a room, turn left and right to look at each of its four walls, and click on objects to interact with them. Rooms are lit in real time with colored point lights, shadows, and reflections.
 
## Features
 
- **Pseudo-3D rooms.** Each room has a floor, a ceiling, and four walls drawn as warped quadrilaterals, so a flat image looks like it is sitting in perspective. Room geometry is scaled from a handful of ratios in `RoomPoints`.
- **Look left and right.** Clickable arrows rotate the view around the room. Only the back wall is populated with objects at any time; the side walls are drawn for depth.
- **Dynamic lighting.** Point lights (circle or square falloff) are propagated pixel by pixel and are stopped, shadowed, or reflected by blockers. Blockers use the alpha of the image they follow, so a transparent lamp lets light through.
- **Interactable objects.** Any room object can be made clickable and wired to a chain of interactions.
- **Interaction chains.** Interactions can be linked with `setNextTrigger`, so one click can print a message, set a flag, then do something else.
- **Flags.** The player holds named boolean flags. Interactions can set them and swap their message once a flag is true (for example, "You open the chest" followed by "The chest is empty").
- **Resolution independent.** Everything is authored against a 1920 px wide reference screen and scaled through `GameSettings`, so fullscreen and windowed modes at 2560x1440, 1920x1080, and 1280x720 look the same.
- **Debug window.** Hold **Ctrl** while clicking Confirm on the graphics screen to open a menu of test screens for shapes, rooms, lighting, and blockers.
- **Asset pack.** Includes 8 floors, 7 walls, 6 ceilings, 28 object sprites (anvil, bookshelf, chest, door, torch, and more), and UI arrows, all under `src/main/resources/images`.

## Getting started

### Requirements

- **JDK 25**
- **Maven 3.9+**

### Build and run

```bash
git clone https://github.com/omarr321/TownsRPG.git
cd TownsRPG

mvn compile
mvn exec:java -Dexec.mainClass=GUI.DisplayMgmt
```
> [!TIP]
> If the build acts strangely, use `mvn clean compile` to start from a fresh build.

### The launcher

The program opens a **Graphical Options** window first.

Click **Confirm** to open the game window. **Hold Ctrl while clicking Confirm** to open the **Debug window** instead.
> [!NOTE]
> The Game window does nothing at the moment.

### The Debug window

The debug window is a menu of test screens, including a basic room, with and without lighting and light blockers visible.

| Key | Action |
| --- | --- |
| `Esc` | Back to the debug menu (from the menu, back to the launcher) |
| `L` | Cycle through the lit and debug versions of the current room test |

## Testing and quality

```bash
mvn clean test       # JUnit 5 tests
mvn verify           # tests + JaCoCo coverage check + Checkstyle
mvn javadoc:javadoc  # build the API docs
```

The project enforces some strict standards, and CI runs them on every pull request to `main`:

- **JUnit 5** unit tests for the engine, rendering, lighting, and helper classes.
- **JaCoCo coverage gate.** No missed lines are allowed and instruction coverage must be at least 95%. `DisplayMgmt` is excluded because it is mostly UI wiring.
- **Checkstyle** requires Javadoc on all public and protected classes, methods, and fields (see [`checkstyle.xml`](checkstyle.xml)).
- **Javadoc build** must succeed.

## Documentation

- **Project site:** <https://omarr321.github.io/TownsRPG/>
- **API docs (Javadoc):** <https://omarr321.github.io/TownsRPG/javadoc/>

The site is rebuilt and deployed to GitHub Pages on every push to `main`.

## Project structure

```
src/main/java/
├── Engine/            Player, flags, and interactions
│   └── Interactions/  Interactable, BasicInteraction, FlagInteraction, DialogInteraction
├── GUI/
│   ├── DisplayMgmt    Launcher, debug window, and game window (entry point)
│   ├── CustomPanels/  QuadrilateralPanel (warped textures), RectPanel
│   ├── Hud/           HudUI overlay (arrows and message box)
│   └── Lighting/      LightMgmt, LightPoint, LightBlocker, LightLayer, LightingLayerUI
├── Helper/            GameSettings, ImageLoader, FontWrapper, Point, QuadShapeDrawer
└── RoomClasses/
    ├── Room           Ties walls, floor, ceiling, lighting, and view direction together
    ├── RoomParts/     Wall, Floor, Ceiling, RoomPoints (room geometry)
    └── RoomObjects/   RoomObj, BasicObj, InteractableObj

src/main/resources/    Images (walls, floors, ceilings, objects, UI), fonts, branding
src/test/java/         JUnit tests mirroring the main package layout
site/                  GitHub Pages site
```

## Contributing

1. Branch from `main`.
2. Make your changes. Add Javadoc for anything public or protected, and add tests for new code.
3. Run `mvn verify` locally.
4. Open a pull request into `main`. The JUnit, JaCoCo, Checkstyle, and Javadoc checks must pass.

## Authors

- [@omarr321](https://github.com/omarr321)
- [@SirTangent](https://github.com/SirTangent)
