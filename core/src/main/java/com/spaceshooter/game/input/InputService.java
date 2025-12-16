package com.spaceshooter.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Service d'input centralisé. */
public final class InputService {
    private InputService() {}

    /** Retourne la position du curseur (ou touch) en coordonnées monde. */
    public static Vector2 getWorldCursor(Viewport viewport) {
        Vector2 v = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(v);
        return v;
    }
}
