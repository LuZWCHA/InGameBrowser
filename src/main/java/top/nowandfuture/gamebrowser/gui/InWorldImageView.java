package top.nowandfuture.gamebrowser.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import top.nowandfuture.gamebrowser.utils.RenderHelper;
import top.nowandfuture.mygui.RootView;
import top.nowandfuture.mygui.ViewGroup;
import top.nowandfuture.mygui.components.ImageView;

import javax.annotation.Nonnull;

public class InWorldImageView extends ImageView {
    public InWorldImageView(@Nonnull RootView rootView) {
        super(rootView);
    }

    public InWorldImageView(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    public InWorldImageView(@Nonnull ViewGroup parent) {
        super(parent);
    }

    @Override
    protected void onDraw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        if (getLocation() != null && getImage() != null) {
            getRoot().context.getTextureManager().bindTexture(getLocation());
            int aw = getImage().getWidth(), ah = getImage().getHeight();
            int imageWidth = getWidth() - padRight - padLeft, imageHeight = getHeight() - padTop - padBottom;
            int padLeft = this.padLeft, padTop = this.padTop;
            if (imageWidth > aw && (padLeft != 0 || padRight != 0)) {
                padLeft = (imageWidth - aw) / 2 + this.padLeft;
                imageWidth = aw;
            }
            if (imageHeight > ah && (padTop != 0 || padBottom != 0)) {
                padTop = (imageHeight - ah) / 2 + this.padTop;
                imageHeight = ah;
            }

            RenderSystem.depthMask(false);
            RenderHelper.blit1(stack, padLeft, padTop, 0,
                    0, 0, imageWidth, imageHeight,
                    imageHeight, imageWidth, getLocation());
            RenderSystem.depthMask(true);
        }
    }
}
