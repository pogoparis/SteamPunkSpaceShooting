package com.spaceshooter.game.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spaceshooter.game.GameConfig;

/** Gère l'affichage et les ressources du vaisseau joueur. */
public class PlayerShip {
    private final Viewport viewport;
    private Texture texture;
    private final Rectangle bounds;

    public PlayerShip(Viewport viewport) {
        this.viewport = viewport;
        if (Gdx.files.internal(GameConfig.Assets.SHIP_1).exists()) {
            texture = new Texture(Gdx.files.internal(GameConfig.Assets.SHIP_1));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        float w = texture != null ? texture.getWidth() : 140f;
        float h = texture != null ? texture.getHeight() : 140f;
        float scale = GameConfig.SHIP_TARGET_DRAW_WIDTH / w;
        float drawW = texture != null ? w * scale : w;
        float drawH = texture != null ? h * scale : h;
        bounds = new Rectangle(viewport.getWorldWidth() * 0.5f - drawW * 0.5f, 140, drawW, drawH);
    }

    public void render(SpriteBatch batch) {
        if (texture == null) return;
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public boolean hasTexture() { return texture != null; }

    public Rectangle getBounds() { return bounds; }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
