package dev.kios.airplace.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;

public class UtilsUI {
    public static Label createLabel(String text, Color color, Skin skin) {
        Label label = new Label(text, skin);
        label.setColor(color);
        return label;
    }

    public static Image createImage(String drawableName, Skin skin) {
        return new Image(skin, drawableName);
    }

    public static TextField createTextField(String text, String msgTxt, int align, TextField.TextFieldFilter textFieldFilter, Skin skin) {
        TextField textField = new TextField(text, skin);
        textField.setTextFieldFilter(textFieldFilter);
        textField.setMessageText(msgTxt);
        textField.setAlignment(align);

        return textField;
    }

    public static CheckBox createCheckBox(String text, Skin skin) {
        CheckBox checkBox = new CheckBox(text, skin);
        checkBox.getLabelCell().padLeft(5.0f);
        return checkBox;
    }

    public static Window createWindow(String title, Table table, Skin skin, Stage stage) {
        final Window window = new Window(title, skin);
        window.add(table);
        window.setName(title);

        Image imgClose = createImage("icon-close", skin);
        imgClose.setName("imgClose");

        window.getTitleTable().add(imgClose);
        window.getTitleLabel().pack();
        window.pack();

        window.setPosition((stage.getWidth() - window.getWidth()) / 2.0f, (stage.getHeight() - window.getHeight()) / 2.0f);
        return window;
    }

    public static TextButton createTextButton(String text, Skin skin) {
        return new TextButton(text, skin);
    }

    public static void addEventListener(Actor actor, EventListener eventListener) {
        actor.addListener(eventListener);
    }
}
