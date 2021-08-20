package dev.kios.airplace;

import com.badlogic.gdx.Game;

public class CoreLauncher extends Game {
    private final CoreSpecificCode mCoreSpecificCode;

    public CoreLauncher(CoreSpecificCode coreSpecificCode) {
        mCoreSpecificCode = coreSpecificCode;
    }

    @Override
    public void create() {
        setScreen(new MainScreen(mCoreSpecificCode));
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}