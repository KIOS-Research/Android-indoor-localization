package dev.kios.airplace.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Toast {
    public static final float LENGTH_LONG = 3.5f;
    public static final float LENGTH_SHORT = 2.0f;

    private static List<Toast> toasts;

    private final int toastWidth;
    private final int toastHeight;

    private final float fontX;
    private final float fontY;
    private final float positionX;
    private final float positionY;
    private final float fadingDuration;

    private final String text;
    private final BitmapFont font;
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;

    private int fontWidth;
    private float opacity;
    private float duration;

    private Toast(String text, float duration, Skin skin) {
        this.text = text;
        this.duration = duration;
        this.font = skin.getFont("default-font");

        opacity = 1.0f;
        fadingDuration = 0.5f;

        float bottomGap = 100;
        float maxRelativeWidth = 0.65f;
        float screenHeight = Gdx.graphics.getHeight();
        positionY = bottomGap + ((screenHeight - bottomGap) / 10.0f);

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setColor(new Color(55.0f / 256.0f, 55.0f / 256.0f, 55.0f / 256.0f, 1.0f));

        GlyphLayout layoutSimple = new GlyphLayout();
        layoutSimple.setText(font, text);

        fontWidth = (int) layoutSimple.width;
        int lineHeight = (int) layoutSimple.height;
        int fontHeight = (int) layoutSimple.height;

        int margin = lineHeight * 2;

        float screenWidth = Gdx.graphics.getWidth();
        float maxTextWidth = screenWidth * maxRelativeWidth;

        if (fontWidth > maxTextWidth) {
            BitmapFontCache cache = new BitmapFontCache(font, true);
            GlyphLayout layout = cache.addText(text, 0, 0, maxTextWidth, Align.center, true);
            fontWidth = (int) layout.width;
            fontHeight = (int) layout.height;
        }

        toastHeight = fontHeight + 2 * margin;
        toastWidth = fontWidth + 2 * margin;

        positionX = (screenWidth / 2.0f) - toastWidth / 2.0f;

        fontX = positionX + margin;
        fontY = positionY + margin + fontHeight;
    }

    public static void show() {
        if (toasts == null)
            return;
        Iterator<Toast> it = toasts.iterator();
        while (it.hasNext()) {
            Toast t = it.next();
            if (!t.render(Gdx.graphics.getDeltaTime())) {
                it.remove();
            } else {
                break;
            }
        }
    }

    public static void makeToast(String text, float duration, Skin skin) {
        if (toasts == null)
            toasts = new LinkedList<>();
        toasts.add(new Toast(text, duration, skin));
    }

    private boolean render(float delta) {
        duration -= delta;
        if (duration < 0.0f)
            return false;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle(positionX, positionY + toastHeight / 2.0f, toastHeight / 2.0f);
        shapeRenderer.rect(positionX, positionY, toastWidth, toastHeight);
        shapeRenderer.circle(positionX + toastWidth, positionY + toastHeight / 2.0f, toastHeight / 2.0f);
        shapeRenderer.end();

        spriteBatch.begin();
        if (duration > 0.0f && opacity > 0.15f) {
            if (duration < fadingDuration) {
                opacity = duration / fadingDuration;
            }

            font.setColor(1.0f, 1.0f, 1.0f, opacity);
            font.draw(spriteBatch, text, fontX, fontY, fontWidth, Align.center, true);
        }
        spriteBatch.end();

        return true;
    }
}
