package top.nowandfuture.mygui.layouts;

import top.nowandfuture.mygui.RootView;
import top.nowandfuture.mygui.ViewGroup;
import top.nowandfuture.mygui.api.NotNull;

// TODO: 2021/7/22
public class ScrollLayout extends FrameLayout {

    public ScrollLayout(@NotNull RootView rootView) {
        super(rootView);
    }

    public ScrollLayout(@NotNull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }
}
