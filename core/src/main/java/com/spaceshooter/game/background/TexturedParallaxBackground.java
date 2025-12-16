package com.spaceshooter.game.background;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spaceshooter.game.GameConfig;
import com.spaceshooter.game.assets.AssetService;

public class TexturedParallaxBackground {
    private final Viewport viewport;
    private final Texture layer1;
    private final Texture layer2;
    private float offset1;
    private float offset2;
    private final float speed1 = 40f;
    private final float speed2 = 120f;

    public TexturedParallaxBackground(Viewport viewport) {
        this.viewport = viewport;
        Texture l1 = null;
        Texture l2 = null;
        if (Gdx.files.internal(GameConfig.Assets.BG_LAYER_1).exists()) {
            l1 = AssetService.get().getTexture(GameConfig.Assets.BG_LAYER_1);
        }
        if (Gdx.files.internal(GameConfig.Assets.BG_LAYER_2).exists()) {
            l2 = AssetService.get().getTexture(GameConfig.Assets.BG_LAYER_2);
        }
        this.layer1 = l1;
        this.layer2 = l2;
    }

    public boolean hasAnyLayer() {
        return layer1 != null || layer2 != null;
    }

    public void update(float delta) {
        if (layer1 != null) offset1 = (offset1 + speed1 * delta) % getDrawHeight(layer1);
        if (layer2 != null) offset2 = (offset2 + speed2 * delta) % getDrawHeight(layer2);
    }

    private float getDrawHeight(Texture t) {
        float scale = viewport.getWorldWidth() / (float) t.getWidth();
        return t.getHeight() * scale;
    }

    private void drawLayer(SpriteBatch batch, Texture t, float offset) {
        float worldW = viewport.getWorldWidth();
        float drawH = getDrawHeight(t);
        int repeats = (int) Math.ceil(viewport.getWorldHeight() / drawH) + 2;
        float scale = worldW / (float) t.getWidth();
        for (int i = -1; i < repeats - 1; i++) {
            float y = i * drawH - offset;
            batch.draw(t, 0, y, worldW, drawH);
        }
    }

    public void render(SpriteBatch batch) {
        if (layer1 != null) drawLayer(batch, layer1, offset1);
        if (layer2 != null) drawLayer(batch, layer2, offset2);
    }
}
