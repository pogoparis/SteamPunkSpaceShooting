# Engineering Rules

## Project Architecture
- core/src/main/java/com/spaceshooter/game
  - GameConfig: central config (world size, gameplay constants, asset paths)
  - FirstScreen: main gameplay loop and rendering
  - assets/AssetService: lazy AssetManager wrapper
  - input/InputService: world-cursor conversion and input helpers
  - player/PlayerShip: player ship rendering and sizing
- platform launchers (Android, LWJGL3) configure window/orientation only.

## Viewport and Resolution
- Portrait as default: WORLD_WIDTH=1080, WORLD_HEIGHT=1920 (FitViewport).
- Desktop window uses 1080x1920 to match gameplay.
- All positions/logic use world units. Never rely on pixel screen size directly.

## Assets
- All asset paths defined in GameConfig.Assets.
- Textures fetched via AssetService.getTexture(path):
  - Uses AssetManager lazily.
  - Enforces Linear filtering.
  - AssetService is disposed once in Game.dispose().

## Input
- Always use InputService.getWorldCursor(viewport) to convert pointer to world coordinates.
- Keep input polling inside screen update methods.

## Player Ship
- Default width target is GameConfig.SHIP_TARGET_DRAW_WIDTH (scales height by aspect).
- Runtime tuning with keyboard: '+'/'='/'NUMPAD_ADD' to grow, '-'/'NUMPAD_SUBTRACT' to shrink.
- HUD shows current width when rendering FirstScreen.

## Gameplay Tuning
- Centralize constants in GameConfig (fire rate, bullet size/speed, enemy spawn/speed).
- Avoid magic numbers in screens/entities.

## Code Style
- Java naming: UpperCamelCase for types, lowerCamelCase for fields/methods.
- Keep SpriteBatch.begin()/end() minimal (ideally once per frame per layer pass).
- Avoid allocations in render loop (reuse objects/arrays).

## Commits (Conventional Commits)
- feat(scope): new feature
- fix(scope): bug fix
- refactor(scope): code change that neither fixes a bug nor adds a feature
- chore(scope): tooling and maintenance (build, CI, docs)

## Testing
- Desktop portrait window used for rapid iteration.
- Android: orientation locked to portrait in AndroidManifest.
- Validate performance with VSync ON; consider removing ANGLE if GL30 is needed later.
