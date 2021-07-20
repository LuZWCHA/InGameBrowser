package top.nowandfuture.mygui.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import top.nowandfuture.mygui.GUIRenderer;
import top.nowandfuture.mygui.RootView;
import top.nowandfuture.mygui.View;
import top.nowandfuture.mygui.ViewGroup;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.IOException;

public class ImageView extends View {
    private ResourceLocation location;

    private boolean initImageInfo;
    private NativeImage image;

    public ImageView(@Nonnull RootView rootView) {
        super(rootView);
    }

    public ImageView(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    public ImageView(@Nonnull ViewGroup parent){
        super(parent);
    }

    @Override
    protected void onLoad() {
        if (!initImageInfo) {
            loadImage();
            initImageInfo = true;
        }
    }

    @Override
    protected void onDraw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        if (location != null && image != null) {
            GlStateManager.color4f(1, 1, 1, 1);
            getRoot().context.getTextureManager().bindTexture(location);
            int aw = image.getWidth(), ah = image.getHeight();
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
            GUIRenderer.getInstance().blit(stack, padLeft, padTop, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        }
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    public void setImageLocation(ResourceLocation location) {
        this.location = location;
        loadImage();
    }

    private void loadImage(){
        IResource iresource;
        if(location != null)
            try {
                iresource = getRoot().context.getResourceManager().getResource(location);
                image = NativeImage.read(iresource.getInputStream());
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public NativeImage getImage() {
        return image;
    }
}
