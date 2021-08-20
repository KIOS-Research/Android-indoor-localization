package dev.kios.airplace.file;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import java.io.FileFilter;
import java.util.Comparator;

import static com.badlogic.gdx.Application.ApplicationType.Android;

public class FileChooser extends Dialog {
    private final Comparator<FileListItem> dirListComparator = new Comparator<FileListItem>() {
        @Override
        public int compare(FileListItem file1, FileListItem file2) {
            if (file1.file.isDirectory() && !file2.file.isDirectory()) {
                return -1;
            }
            if (file1.file.isDirectory() && file2.file.isDirectory()) {
                return 0;
            }
            if (!file1.file.isDirectory() && !file2.file.isDirectory()) {
                return 0;
            }
            return 1;
        }
    };

    private final Skin skin;
    private final TextField fileNameInput;
    private final Label fileNameLabel;
    private final TextButton newFolderButton;
    private final FileHandle baseDir;
    private final Label fileListLabel;
    private final List<FileListItem> fileList;
    private final TextButton ok;
    protected String result;
    protected ResultListener resultListener;
    ScrollPane scrollPane;
    private boolean fileNameEnabled;
    private boolean newFolderEnabled;
    private FileHandle currentDir;
    private FileFilter filter;
    private Stage stage;

    private FileChooser(String title, final Skin skin, FileHandle baseDir) {
        super(title, skin);
        this.skin = skin;
        this.baseDir = baseDir;

        Table content = getContentTable();
        content.top().left();

        fileList = new List<>(skin);
        fileListLabel = new Label("", skin);
        fileListLabel.setAlignment(Align.left);

        fileNameLabel = new Label("File name:", skin);
        fileNameInput = new TextField("routeFile.rf", skin);
        fileNameInput.setTextFieldListener(new TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                result = textField.getText();
            }
        });

        newFolderButton = new TextButton("New Folder", skin);
        newFolderButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (newFolderButton.isChecked()) {
                    newFolderButton.setChecked(false);
                    new NewFileDialog("New Folder", skin) {
                        @Override
                        protected void result(Object object) {
                            boolean success = (Boolean) object;
                            if (success) {
                                FileHandle newFolder = new FileHandle(currentDir.path() + "/" + getResult());
                                newFolder.mkdirs();
                                changeDirectory(currentDir);
                            }
                        }
                    }.show(stage);
                }
            }
        });

        ok = new TextButton("Ok", skin);
        button(ok, true);
        button(new TextButton("Cancel", skin), false);

        fileList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                FileListItem selected = fileList.getSelected();
                if (!selected.file.isDirectory()) {
                    result = selected.file.name();
                    fileNameInput.setText(result);
                }
            }
        });
    }

    public static FileChooser createSaveDialog(String title, Skin skin, FileHandle path) {
        return new FileChooser(title, skin, path) {
            @Override
            protected void result(Object object) {
                if (resultListener == null) {
                    return;
                }

                boolean success = (Boolean) object;
                if (!resultListener.result(success, getResult())) {
                    this.cancel();
                }
            }
        }.setFileNameEnabled(true).setNewFolderEnabled(true).setOkButtonText("Save");
    }

    public static FileChooser createLoadDialog(String title, Skin skin, FileHandle path) {
        return new FileChooser(title, skin, path) {
            @Override
            protected void result(Object object) {
                if (resultListener == null) {
                    return;
                }

                boolean success = (Boolean) object;
                resultListener.result(success, getResult());
            }
        }.setNewFolderEnabled(false).setFileNameEnabled(false).setOkButtonText("Load");
    }

    public static FileChooser createPickDialog(String title, Skin skin, FileHandle path) {
        return new FileChooser(title, skin, path) {
            @Override
            protected void result(Object object) {
                if (resultListener == null) {
                    return;
                }

                boolean success = (Boolean) object;
                resultListener.result(success, getResult());
            }
        }.setOkButtonText("Select");
    }

    public FileHandle getResult() {
        String path = currentDir.path() + "/";
        if (result != null && result.length() > 0) {
            path += result;
        }
        return new FileHandle(path);
    }

    public FileChooser setOkButtonText(String text) {
        this.ok.setText(text);
        return this;
    }

    public void setFilter(FileFilter filter) {
        this.filter = filter;
    }

    public FileChooser setFileNameEnabled(boolean fileNameEnabled) {
        this.fileNameEnabled = fileNameEnabled;
        return this;
    }

    public FileChooser setNewFolderEnabled(boolean newFolderEnabled) {
        this.newFolderEnabled = newFolderEnabled;
        return this;
    }

    public void setResultListener(ResultListener result) {
        this.resultListener = result;
    }

    private void changeDirectory(FileHandle directory) {
        currentDir = directory;

        if (Gdx.app.getType() == Android) {
            if (currentDir.path().equals("/storage/emulated")) {
                return;
            } else if (currentDir.path().contains("/storage/emulated/0")) {
                fileListLabel.setText(currentDir.path().replace("/storage/emulated/0", "Internal Storage"));
            }
        } else {
            fileListLabel.setText(currentDir.path());
        }

        Array<FileListItem> items = new Array<>();

        FileHandle[] list = directory.list(filter);
        for (FileHandle handle : list) {
            items.add(new FileListItem(handle));
        }

        items.sort(dirListComparator);

        if (directory.file().getParentFile() != null) {
            items.insert(0, new FileListItem("..", directory.parent()));
        }

        fileList.setSelected(null);
        fileList.setItems(items);
    }

    @Override
    public Dialog show(final Stage stage, final Action action) {
        Table content = getContentTable();
        content.add(fileListLabel).top().left().expandX().fillX().row();

        scrollPane = new ScrollPane(fileList, skin);
        scrollPane.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                stage.setScrollFocus(scrollPane);
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                stage.setScrollFocus(scrollPane);
                return true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                stage.setScrollFocus(null);
            }
        });
        content.add(scrollPane).size(300, 150).fill().expand().row();

        if (fileNameEnabled) {
            content.add(fileNameLabel).fillX().expandX().row();
            content.add(fileNameInput).fillX().expandX().row();
        }

        if (newFolderEnabled) {
            content.add(newFolderButton).fillX().expandX().row();
        }

        fileList.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                FileListItem selected = fileList.getSelected();
                if (selected.file.isDirectory()) {
                    changeDirectory(selected.file);
                }
            }
        });

        this.stage = stage;
        changeDirectory(baseDir);
        return super.show(stage, action);
    }

    public interface ResultListener {
        boolean result(boolean success, FileHandle result);
    }
}