package com.spaceshooter.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.spaceshooter.game.player.PlayerShip;
import com.spaceshooter.game.input.InputService;
import com.spaceshooter.game.background.ScrollingBackground;
import com.spaceshooter.game.background.TexturedParallaxBackground;
import com.spaceshooter.game.assets.AssetService;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {
    private static final int VIRTUAL_WIDTH = GameConfig.WORLD_WIDTH;
    private static final int VIRTUAL_HEIGHT = GameConfig.WORLD_HEIGHT;

    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapes;
    private GlyphLayout layout;

    private PlayerShip playerShip;
    private Rectangle player;
    private float shipTargetWidth;
    private Array<Rectangle> bullets;
    private Array<Rectangle> enemies;
    private float enemySpawnTimer;
    private float enemySpawnInterval = GameConfig.ENEMY_SPAWN_INTERVAL;
    private float fireCooldown;
    private float fireRate = GameConfig.FIRE_RATE_SECONDS;
    private float health = 1f;
    private int score = 0;
    private boolean showHud = true;
    private boolean showTouchOverlay = true;
    private ScrollingBackground background;
    private TexturedParallaxBackground parallax;
    private float prevCenterX;
    private float prevCenterY;
    private float tiltAngleDeg;
    private Texture enemyTexture;

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        batch = new SpriteBatch();
        font = new BitmapFont();
        shapes = new ShapeRenderer();
        layout = new GlyphLayout();

        // Crée le vaisseau joueur (charge la texture si présente) et récupère son rectangle
        playerShip = new PlayerShip(viewport);
        player = playerShip.getBounds();
        shipTargetWidth = GameConfig.SHIP_TARGET_DRAW_WIDTH;
        bullets = new Array<>();
        enemies = new Array<>();
        // Fond défilant évolutif
        background = new ScrollingBackground(viewport);
        // Parallaxe texturée (optionnelle si assets présents)
        parallax = new TexturedParallaxBackground(viewport);
        // Init historique pour le calcul de vitesse et tilt
        prevCenterX = player.x + player.width * 0.5f;
        prevCenterY = player.y + player.height * 0.5f;
        tiltAngleDeg = 0f;
        // Charge texture ennemi si disponible
        if (Gdx.files.internal(GameConfig.Assets.ENEMY_1).exists()) {
            enemyTexture = AssetService.get().getTexture(GameConfig.Assets.ENEMY_1);
        } else {
            enemyTexture = null;
        }
    }

    @Override
    public void render(float delta) {
        updateSimulation(delta);
        draw(delta);
    }

    @Override
    public void resize(int width, int height) {
        if(width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
        // Recrée le fond pour s'adapter à la nouvelle taille étendue
        background = new ScrollingBackground(viewport);
        // Recrée la parallaxe texturée pour matcher la nouvelle taille
        parallax = new TexturedParallaxBackground(viewport);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (shapes != null) shapes.dispose();
        if (playerShip != null) playerShip.dispose();
    }

    private void updateSimulation(float delta) {
        // Background évolutif
        if (background != null) background.update(delta);
        if (parallax != null) parallax.update(delta);
        // Toggle HUD visibility
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            showHud = !showHud;
        }
        // Suivi instantané de la souris / touch, sur X et Y
        Vector2 target = InputService.getWorldCursor(viewport);

        // Coller parfaitement à la souris/touch
        player.setCenter(target.x, target.y);

        // Garde dans les limites
        player.x = MathUtils.clamp(player.x, 0, viewport.getWorldWidth() - player.width);
        player.y = MathUtils.clamp(player.y, 0, viewport.getWorldHeight() - player.height);

        // Calcul vitesse et tilt
        float centerX = player.x + player.width * 0.5f;
        float centerY = player.y + player.height * 0.5f;
        float vx = (centerX - prevCenterX) / Math.max(1e-4f, delta);
        float vy = (centerY - prevCenterY) / Math.max(1e-4f, delta);
        float maxAngle = 18f;
        float vxNorm = MathUtils.clamp(vx / 900f, -1f, 1f);
        float desiredTilt = vxNorm * maxAngle; // droite => angle positif
        tiltAngleDeg = MathUtils.lerp(tiltAngleDeg, desiredTilt, 0.15f);
        prevCenterX = centerX;
        prevCenterY = centerY;

        // Debug Tuning: ajuster la largeur cible du vaisseau en jeu (+ / -)
        boolean inc = Gdx.input.isKeyJustPressed(Input.Keys.PLUS)
                || Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)
                || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ADD);
        boolean dec = Gdx.input.isKeyJustPressed(Input.Keys.MINUS)
                || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_SUBTRACT);
        if (inc || dec) {
            float step = 16f;
            shipTargetWidth += inc ? step : -step;
            shipTargetWidth = MathUtils.clamp(shipTargetWidth, 96f, 512f);
            playerShip.resizeToWidth(shipTargetWidth);
        }

        fireCooldown -= delta;
        boolean fireRequested = false;
        // Multi-touch Android: tir si un doigt est dans la zone droite basse; Desktop: tir si clic maintenu
        if (Gdx.input.isTouched()) {
            float worldW = viewport.getWorldWidth();
            float worldH = viewport.getWorldHeight();
            float zoneH = worldH * 0.22f;
            int pointers = 10; // plupart des devices < 10
            for (int i = 0; i < pointers; i++) {
                if (!Gdx.input.isTouched(i)) continue;
                Vector2 tp = new Vector2(Gdx.input.getX(i), Gdx.input.getY(i));
                viewport.unproject(tp);
                // Zone droite basse pour tir
                if (tp.y <= zoneH && tp.x >= worldW * 0.5f) {
                    fireRequested = true;
                    break;
                }
            }
            // Si aucune zone tactile match (ex: Desktop), alors tout touch/click déclenche
            if (!fireRequested) fireRequested = true;
        }
        // Auto-fire quand le vaisseau est en mouvement (seuil de vitesse)
        float speed = (float)Math.sqrt(vx * vx + vy * vy);
        if (speed > 80f) fireRequested = true;
        if (fireRequested && fireCooldown <= 0f) {
            Rectangle b = new Rectangle(
                    player.x + player.width * 0.5f - GameConfig.BULLET_WIDTH * 0.5f,
                    player.y + player.height - 6,
                    GameConfig.BULLET_WIDTH, GameConfig.BULLET_HEIGHT);
            bullets.add(b);
            fireCooldown = fireRate;
        }

        for (int i = bullets.size - 1; i >= 0; i--) {
            Rectangle b = bullets.get(i);
            b.y += GameConfig.BULLET_SPEED * delta;
            if (b.y > viewport.getWorldHeight()) bullets.removeIndex(i);
        }

        enemySpawnTimer += delta;
        if (enemySpawnTimer >= enemySpawnInterval) {
            enemySpawnTimer = 0f;
            float w;
            float h;
            if (enemyTexture != null) {
                float texW = enemyTexture.getWidth();
                float texH = enemyTexture.getHeight();
                float aspect = texH / Math.max(1f, texW);
                w = MathUtils.random(120f, 200f);
                h = w * aspect;
            } else {
                w = MathUtils.random(100f, 180f);
                h = MathUtils.random(60f, 120f);
            }
            float maxX = Math.max(0f, viewport.getWorldWidth() - w);
            float x = MathUtils.random(0f, maxX);
            Rectangle e = new Rectangle(x, viewport.getWorldHeight() + h, w, h);
            enemies.add(e);
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            Rectangle e = enemies.get(i);
            e.y -= GameConfig.ENEMY_SPEED * delta;
            if (e.y + e.height < 0) enemies.removeIndex(i);
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            Rectangle e = enemies.get(i);
            boolean hit = false;
            for (int j = bullets.size - 1; j >= 0; j--) {
                Rectangle b = bullets.get(j);
                if (e.overlaps(b)) {
                    enemies.removeIndex(i);
                    bullets.removeIndex(j);
                    score += 100;
                    hit = true;
                    break;
                }
            }
            if (!hit && e.overlaps(player)) {
                enemies.removeIndex(i);
                health -= 0.25f;
                if (health <= 0f) {
                    health = 1f;
                    score = 0;
                    bullets.clear();
                    enemies.clear();
                }
            }
        }
    }

    private void draw(float delta) {
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        shapes.setProjectionMatrix(camera.combined);
        // Pass 1: starfield en arrière-plan (shapes)
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (background != null) background.render(shapes);
        shapes.end();

        // Pass 2: couches de parallaxe texturées (batch)
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (parallax != null && parallax.hasAnyLayer()) {
            parallax.render(batch);
        }
        batch.end();

        // Pass 3: entités vectorielles (shapes): fallback joueur, bullets, ennemis, overlay
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (!playerShip.hasTexture()) {
            shapes.setColor(new Color(0.93f, 0.69f, 0.22f, 1f));
            shapes.rect(player.x, player.y, player.width, player.height);
        }
        shapes.setColor(new Color(1f, 0.88f, 0.35f, 1f));
        for (Rectangle b : bullets) shapes.rect(b.x, b.y, b.width, b.height);
        if (enemyTexture == null) {
            shapes.setColor(new Color(0.55f, 0.55f, 0.60f, 1f));
            for (Rectangle e : enemies) shapes.rect(e.x, e.y, e.width, e.height);
        }

        // Overlay tactile Android (visuel zones)
        if (showTouchOverlay && (Gdx.app != null && Gdx.app.getType().name().equalsIgnoreCase("Android"))) {
            float w = viewport.getWorldWidth();
            float h = viewport.getWorldHeight();
            float zoneH = h * 0.22f;
            shapes.setColor(0f, 0.7f, 1f, 0.12f);
            shapes.rect(0, 0, w * 0.5f, zoneH);
            shapes.setColor(1f, 0.4f, 0f, 0.12f);
            shapes.rect(w * 0.5f, 0, w * 0.5f, zoneH);
        }
        shapes.end();

        // Pass 4: sprites (batch): vaisseau + HUD
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        // Ennemis texturés si dispo
        if (enemyTexture != null) {
            for (Rectangle e : enemies) {
                batch.draw(enemyTexture, e.x, e.y, e.width, e.height);
            }
        }
        playerShip.render(batch, tiltAngleDeg);
        if (showHud) {
            font.setColor(Color.GOLD);
            font.getData().setScale(1.5f);
            font.draw(batch, "Score: " + score, 24, VIRTUAL_HEIGHT - 24);
            font.draw(batch, "HP: " + (int)(health * 100) + "%", 24, VIRTUAL_HEIGHT - 64);
            font.getData().setScale(1.0f);
            font.draw(batch, "ShipW: " + (int)shipTargetWidth + "  [+/-]  [H] HUD", 24, VIRTUAL_HEIGHT - 104);
            if (showTouchOverlay && (Gdx.app != null && Gdx.app.getType().name().equalsIgnoreCase("Android"))) {
                font.draw(batch, "[Gauche] Move", 24, 48);
                String s = "[Droite] Fire (auto)";
                layout.setText(font, s);
                font.draw(batch, s, VIRTUAL_WIDTH - 24 - layout.width, 48);
            }
        }
        batch.end();
    }
}
