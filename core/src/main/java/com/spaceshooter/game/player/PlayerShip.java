package com.spaceshooter.game.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spaceshooter.game.GameConfig;
import com.spaceshooter.game.assets.AssetService;

/** Gère l'affichage et les ressources du vaisseau joueur. */
public class PlayerShip {
    private final Viewport viewport;
    private Texture texture;
    private final Rectangle bounds;

    public PlayerShip(Viewport viewport) {
        this.viewport = viewport;
        // Charge via AssetService (lazy)
        texture = AssetService.get().getTexture(GameConfig.Assets.SHIP_1);

        float w = texture != null ? texture.getWidth() : 140f;
        float h = texture != null ? texture.getHeight() : 140f;
        float scale = GameConfig.SHIP_TARGET_DRAW_WIDTH / w;
        float drawW = texture != null ? w * scale : w;
        float drawH = texture != null ? h * scale : h;
        bounds = new Rectangle(viewport.getWorldWidth() * 0.5f - drawW * 0.5f, 140, drawW, drawH);
    }

    public void render(SpriteBatch batch) {
        render(batch, 0f);
    }

    public void render(SpriteBatch batch, float rotationDeg) {
        if (texture == null) return;
        float ox = bounds.width * 0.5f;
        float oy = bounds.height * 0.5f;
        batch.draw(texture,
                bounds.x, bounds.y,
                ox, oy,
                bounds.width, bounds.height,
                1f, 1f,
                rotationDeg,
                0, 0, texture.getWidth(), texture.getHeight(),
                false, false);
    }

    public boolean hasTexture() { return texture != null; }

    public Rectangle getBounds() { return bounds; }

    public float getTextureOriginalWidth() { return texture != null ? texture.getWidth() : bounds.width; }
    public float getTextureOriginalHeight() { return texture != null ? texture.getHeight() : bounds.height; }

    /** Redimensionne le vaisseau à une largeur cible tout en conservant le centre. */
    public void resizeToWidth(float targetWidth) {
        float cx = bounds.x + bounds.width * 0.5f;
        float cy = bounds.y + bounds.height * 0.5f;
        float w0 = getTextureOriginalWidth();
        float h0 = getTextureOriginalHeight();
        float scale = targetWidth / Math.max(1f, w0);
        bounds.width = w0 * scale;
        bounds.height = h0 * scale;
        bounds.setCenter(cx, cy);
    }

    public void dispose() {
        // Textures gérées par AssetService; pas de dispose direct ici.
    }
}
