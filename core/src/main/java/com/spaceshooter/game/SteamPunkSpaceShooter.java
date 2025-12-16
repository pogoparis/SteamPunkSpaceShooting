package com.spaceshooter.game;
import com.spaceshooter.game.assets.AssetService;
import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class SteamPunkSpaceShooter extends Game {
    @Override
    public void create() {
      setScreen(new LoadingScreen(this));
    }
}
@Override
public void dispose() {
    super.dispose();
    AssetService.get().dispose();
}