package top.nowandfuture.mygui.api;

public interface IAction {
    void clicked(IMyGui gui, int button);

    void press(IMyGui gui, int button);

    void release(IMyGui gui, int button);

    void longClick(IMyGui gui, int button, long lastTime);

    abstract class ActionClick implements IAction {
        public final void press(IMyGui gui, int button) {
        }

        public final void release(IMyGui gui, int button) {
        }

        public void longClick(IMyGui gui, int button, long lastTime) {
        }
    }

    abstract class ActionPress implements IAction {
        public final void clicked(IMyGui gui, int button) {
        }

        public final void release(IMyGui gui, int button) {
        }
    }

    abstract class ActionRelease implements IAction {
        public final void clicked(IMyGui gui, int button) {
        }

        public final void press(IMyGui gui, int button) {
        }
    }
}