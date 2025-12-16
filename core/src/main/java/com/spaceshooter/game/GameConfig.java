package com.spaceshooter.game;

/** Centralise toutes les constantes de configuration pour faciliter l'upgrade. */
public final class GameConfig {
    private GameConfig() {}

    // Monde logique (utilisé par le FitViewport)
    public static final int WORLD_WIDTH = 1920;
    public static final int WORLD_HEIGHT = 1080;

    // Joueur / Vaisseau
    public static final float SHIP_TARGET_DRAW_WIDTH = 256f; // largeur cible en pixels; hauteur suit le ratio

    // Tir
    public static final float FIRE_RATE_SECONDS = 0.18f; // délai entre tirs
    public static final float BULLET_SPEED = 950f; // px/s
    public static final float BULLET_WIDTH = 16f;
    public static final float BULLET_HEIGHT = 34f;

    // Ennemis
    public static final float ENEMY_SPAWN_INTERVAL = 1.1f;
    public static final float ENEMY_SPEED = 260f; // px/s

    // Assets (chemins relatifs au dossier assets/)
    public static final class Assets {
        public static final String SHIP_1 = "Starships/vaisseau_1.png";
    }
}
