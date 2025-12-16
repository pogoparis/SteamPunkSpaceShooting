package com.spaceshooter.game.background;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Simple starfield scrolling background that evolves over time. */
public class ScrollingBackground {
    private static class Star {
        float x, y, size, speed, a; // alpha/brightness
    }

    private final Viewport viewport;
    private final Array<Star> stars = new Array<>();

    public ScrollingBackground(Viewport viewport) {
        this.viewport = viewport;
        initializeStars();
    }

    private void initializeStars() {
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();
        int count = Math.max(140, (int)(w * h / (1080f * 1920f) * 180));
        for (int i = 0; i < count; i++) {
            Star s = new Star();
            s.x = MathUtils.random(0f, w);
            s.y = MathUtils.random(0f, h);
            s.size = MathUtils.random(1.2f, 3.2f);
            // Two parallax bands: slow and fast
            boolean fast = MathUtils.randomBoolean(0.35f);
            s.speed = fast ? MathUtils.random(140f, 220f) : MathUtils.random(60f, 120f);
            s.a = fast ? MathUtils.random(0.75f, 1.0f) : MathUtils.random(0.35f, 0.7f);
            stars.add(s);
        }
    }

    public void update(float delta) {
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();
        for (int i = 0; i < stars.size; i++) {
            Star s = stars.get(i);
            s.y -= s.speed * delta;
            if (s.y < -s.size - 2f) {
                s.y = h + s.size + MathUtils.random(8f, 64f);
                s.x = MathUtils.random(0f, w);
            }
        }
    }

    public void render(ShapeRenderer shapes) {
        // Draw stars as small rectangles with varying alpha
        for (int i = 0; i < stars.size; i++) {
            Star s = stars.get(i);
            shapes.setColor(1f, 1f, 1f, s.a);
            shapes.rect(s.x, s.y, s.size, s.size);
        }
        shapes.setColor(Color.WHITE);
    }
}
