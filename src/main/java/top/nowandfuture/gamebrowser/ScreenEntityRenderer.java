package top.nowandfuture.gamebrowser;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import top.nowandfuture.gamebrowser.gui.InWorldRenderer;
import top.nowandfuture.mygui.GUIRenderer;

import static top.nowandfuture.gamebrowser.utils.RenderHelper.QUAD_TEX;

public class ScreenEntityRenderer extends EntityRenderer<ScreenEntity> {


    protected ScreenEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);
        GUIRenderer.getInstance().setRenderer(new InWorldRenderer());
    }

    @Override
    public ResourceLocation getEntityTexture(ScreenEntity entity) {
        return null;
    }

    @Override
    protected boolean canRenderName(ScreenEntity entity) {
        return false;
    }

    @Override
    public void render(ScreenEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        InWorldRenderer.typeBuffer = bufferIn;
        ScreenManager.getInstance().addRender(entityIn);
    }
}
