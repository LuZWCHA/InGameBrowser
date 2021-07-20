package top.nowandfuture.gamebrowser.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import top.nowandfuture.gamebrowser.utils.RenderHelper;
import top.nowandfuture.mygui.GUIRenderer;
import top.nowandfuture.mygui.tipevents.TextTipEvent;

public class InWorldTextTipEvent extends TextTipEvent {
    public InWorldTextTipEvent(String text) {
        super(text);
    }

    public InWorldTextTipEvent(String text, long lifeTime) {
        super(text, lifeTime);
    }

    public InWorldTextTipEvent(String text, long lifeTime, Position position) {
        super(text, lifeTime, position);
    }

    @Override
    protected void onDrawData(MatrixStack stack, int posX, int posY, int mouseX, int mouseY, float partialTicks) {
        int w = rootView.getFontRenderer().getStringWidth(data);
        stack.push();
        stack.translate(0,0,-.5);
        GUIRenderer.getInstance().drawString(stack, rootView.getFontRenderer(), data, posX, posY, RenderHelper.colorInt(255, 255, 255, 0));
        stack.pop();
    }
}
