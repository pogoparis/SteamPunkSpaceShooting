package com.spaceshooter.game.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

/**
 * AssetService: wrapper simple autour d'AssetManager (lazy) avec helpers Texture.
 */
public final class AssetService implements Disposable {
    private static AssetService INSTANCE;

    public static AssetService get() {
        if (INSTANCE == null) INSTANCE = new AssetService();
        return INSTANCE;
    }

    private final AssetManager manager = new AssetManager();

    private AssetService() {}

    public Texture getTexture(String path) {
        if (!manager.isLoaded(path, Texture.class)) {
            manager.load(path, Texture.class);
            manager.finishLoadingAsset(path);
            Texture t = manager.get(path, Texture.class);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return t;
        }
        return manager.get(path, Texture.class);
    }

    public void loadTexture(String path) {
        if (!manager.isLoaded(path, Texture.class)) {
            manager.load(path, Texture.class);
        }
    }

    public boolean update() {
        return manager.update();
    }

    public float getProgress() {
        return manager.getProgress();
    }

    public void finishLoading() {
        manager.finishLoading();
    }

    @Override
    public void dispose() {
        manager.dispose();
    }
}
