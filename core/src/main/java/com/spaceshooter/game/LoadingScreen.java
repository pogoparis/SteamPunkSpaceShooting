package com.spaceshooter.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.files.FileHandle;
import com.spaceshooter.game.assets.AssetService;

public class LoadingScreen implements Screen {
    private final SteamPunkSpaceShooter game;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;

    public LoadingScreen(SteamPunkSpaceShooter game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

        // Queue assets to load
        AssetService assets = AssetService.get();
        assets.loadTexture(GameConfig.Assets.SHIP_1);
        // Optionally queue parallax layers if present on disk
        FileHandle l1 = Gdx.files.internal(GameConfig.Assets.BG_LAYER_1);
        if (l1.exists()) assets.loadTexture(GameConfig.Assets.BG_LAYER_1);
        FileHandle l2 = Gdx.files.internal(GameConfig.Assets.BG_LAYER_2);
        if (l2.exists()) assets.loadTexture(GameConfig.Assets.BG_LAYER_2);
    }

    @Override
    public void render(float delta) {
        AssetService assets = AssetService.get();
        boolean done = assets.update();

        Gdx.gl.glClearColor(0.02f, 0.02f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        float progress = assets.getProgress();
        float barW = w * 0.6f;
        float barH = 24f;
        float barX = (w - barW) * 0.5f;
        float barY = h * 0.5f - barH * 0.5f;

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.12f, 0.12f, 0.16f, 1f));
        shapes.rect(barX, barY, barW, barH);
        shapes.setColor(new Color(0.20f, 0.70f, 1f, 1f));
        shapes.rect(barX, barY, barW * progress, barH);
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.3f);
        font.draw(batch, "Loading... " + (int)(progress * 100) + "%", barX, barY + barH + 32);
        batch.end();

        if (done) {
            game.setScreen(new FirstScreen());
        }
    }

    @Override public void resize(int width, int height) { if (width>0 && height>0) viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (shapes != null) shapes.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}
