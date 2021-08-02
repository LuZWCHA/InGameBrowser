package top.nowandfuture.mygui;

import com.mojang.blaze3d.matrix.MatrixStack;
import top.nowandfuture.mygui.layouts.FrameLayout;

class TopView extends FrameLayout {

    TopView(RootView rootView) {
        super(rootView, null);
    }

    // TODO: 2021/8/2 wrap is not the right usage, I do this is to make dialog not to layout
    @Override
    protected void onLayoutForSelf(int suggestWidth, int suggestHeight) {
        super.onLayoutForSelf(wrapContentWidth ? this.getWidth(): suggestWidth, wrapContentHeight ? this.getHeight(): suggestHeight);
    }

    @Override
    protected void onDraw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return false;
    }
}
