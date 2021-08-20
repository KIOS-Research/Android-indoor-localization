package dev.kios.airplace.file;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class NewFileDialog extends Dialog {
    private final TextField fileName;

    public NewFileDialog(String title, Skin skin) {
        super(title, skin);

        Table content = getContentTable();
        fileName = new TextField("AirPlace", skin);
        content.add(fileName).fillX().expandX();

        button("Create", true);
        button("Cancel", false);
    }

    protected String getResult() {
        return fileName.getText();
    }

    @Override
    public Dialog show(Stage stage, Action action) {
        super.show(stage, action);
        return this;
    }
}
