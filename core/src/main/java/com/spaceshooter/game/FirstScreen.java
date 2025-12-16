package com.spaceshooter.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.spaceshooter.game.player.PlayerShip;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {
    private static final int VIRTUAL_WIDTH = GameConfig.WORLD_WIDTH;
    private static final int VIRTUAL_HEIGHT = GameConfig.WORLD_HEIGHT;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapes;

    private PlayerShip playerShip;
    private Rectangle player;
    private Array<Rectangle> bullets;
    private Array<Rectangle> enemies;
    private float enemySpawnTimer;
    private float enemySpawnInterval = 1.1f;
    private float fireCooldown;
    private float fireRate = 0.18f;
    private float health = 1f;
    private int score = 0;

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        batch = new SpriteBatch();
        font = new BitmapFont();
        shapes = new ShapeRenderer();

        // Crée le vaisseau joueur (charge la texture si présente) et récupère son rectangle
        playerShip = new PlayerShip(viewport);
        player = playerShip.getBounds();
        bullets = new Array<>();
        enemies = new Array<>();
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
        // Suivi rapide de la souris / touch, sur X et Y
        // Conversion écran -> monde
        Vector2 target = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(target);

        // Coller parfaitement à la souris/touch
        player.setCenter(target.x, target.y);

        // Garde dans les limites
        player.x = MathUtils.clamp(player.x, 0, viewport.getWorldWidth() - player.width);
        player.y = MathUtils.clamp(player.y, 0, viewport.getWorldHeight() - player.height);

        fireCooldown -= delta;
        if (Gdx.input.isTouched() && fireCooldown <= 0f) {
            Rectangle b = new Rectangle(player.x + player.width * 0.5f - 8,
                    player.y + player.height - 6,
                    16, 34);
            bullets.add(b);
            fireCooldown = fireRate;
        }

        for (int i = bullets.size - 1; i >= 0; i--) {
            Rectangle b = bullets.get(i);
            b.y += 950f * delta;
            if (b.y > viewport.getWorldHeight()) bullets.removeIndex(i);
        }

        enemySpawnTimer += delta;
        if (enemySpawnTimer >= enemySpawnInterval) {
            enemySpawnTimer = 0f;
            float w = MathUtils.random(100f, 180f);
            float h = MathUtils.random(60f, 120f);
            float x = MathUtils.random(0f, viewport.getWorldWidth() - w);
            Rectangle e = new Rectangle(x, viewport.getWorldHeight() + h, w, h);
            enemies.add(e);
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            Rectangle e = enemies.get(i);
            e.y -= 260f * delta;
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
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // Le joueur est dessiné avec une texture ci-dessous; on ne dessine le rect que si la texture n'est pas trouvée
        if (!playerShip.hasTexture()) {
            shapes.setColor(new Color(0.93f, 0.69f, 0.22f, 1f));
            shapes.rect(player.x, player.y, player.width, player.height);
        }
        shapes.setColor(new Color(1f, 0.88f, 0.35f, 1f));
        for (Rectangle b : bullets) shapes.rect(b.x, b.y, b.width, b.height);
        shapes.setColor(new Color(0.55f, 0.55f, 0.60f, 1f));
        for (Rectangle e : enemies) shapes.rect(e.x, e.y, e.width, e.height);
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        // Dessine le vaisseau si disponible, par-dessus le décor
        playerShip.render(batch);
        font.setColor(Color.GOLD);
        font.getData().setScale(1.5f);
        font.draw(batch, "Score: " + score, 24, VIRTUAL_HEIGHT - 24);
        font.draw(batch, "HP: " + (int)(health * 100) + "%", 24, VIRTUAL_HEIGHT - 64);
        batch.end();
    }
}
