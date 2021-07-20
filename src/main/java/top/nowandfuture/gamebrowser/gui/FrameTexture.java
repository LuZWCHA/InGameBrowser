package top.nowandfuture.gamebrowser.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureUtil;

import javax.annotation.Nullable;

//Use this as a wrap of texture id.
//we do not allocate the memory assuredly.
public class FrameTexture extends DynamicTexture {

    int width, height;

    public FrameTexture(int width, int height, int id){
        //to allocate zero size of memory to save the ram
        super(0, 0, true);
        wrap(id);

        this.glTextureId = id;
        this.width = width;
        this.height = height;
    }

    public void wrap(int textureId){
        if(this.glTextureId != -1){
            close();
            if(this.glTextureId != -1){
                TextureUtil.releaseTextureId(this.glTextureId);
            }
        }

        this.glTextureId = textureId;
    }

    public FrameTexture(NativeImage nativeImageIn) {
        this(nativeImageIn.getWidth(), nativeImageIn.getHeight(),-1);
    }

    public FrameTexture(int widthIn, int heightIn, boolean clearIn) {
        this(widthIn, heightIn, -1);
    }

    @Override
    public int getGlTextureId() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (this.glTextureId == -1) {
            this.glTextureId = TextureUtil.generateTextureId();
        }

        return this.glTextureId;
    }

    @Override
    public void bindTexture() {
        super.bindTexture();
    }
}
