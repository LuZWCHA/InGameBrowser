package top.nowandfuture.gamebrowser;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import top.nowandfuture.gamebrowser.gui.InWorldRenderer;
import top.nowandfuture.mygui.GUIRenderer;
import top.nowandfuture.mygui.MyScreen;

import javax.annotation.Nonnull;
import java.util.Optional;

import static top.nowandfuture.gamebrowser.utils.RenderHelper.QUAD_TEX;
import static top.nowandfuture.gamebrowser.utils.RenderHelper.colorInt;

public class ScreenEntityRenderer extends EntityRenderer<ScreenEntity> {


    protected ScreenEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);
        GUIRenderer.getInstance().setRenderer(new InWorldRenderer());
    }

    @Override
    public ResourceLocation getEntityTexture(@Nonnull ScreenEntity entity) {
        return null;
    }

    @Override
    protected boolean canRenderName(@Nonnull ScreenEntity sc) {
        return false;
    }

    @Override
    public void render(@Nonnull ScreenEntity sc, float entityYaw, float partialTicks, @Nonnull MatrixStack stack, @Nonnull IRenderTypeBuffer bufferIn, int packedLightIn) {
        super.render(sc, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
        InWorldRenderer.typeBuffer = bufferIn;

        Optional<MyScreen> screen = Optional.ofNullable(sc.getScreen());
        float scale = sc.getScale();

        screen.ifPresent(screen1 -> {
            stack.push();
            stack.translate(0,  sc.getScreenHeight(), 0);
            float yaw = 180 - sc.rotationYaw;
//            Quaternion quaternion = new Quaternion(0, 0, 180, true);
//            stack.rotate(quaternion);

            stack.push();
            stack.translate(.5, 0, .5);
            Quaternion quaternion = new Quaternion(0, yaw, 180, true);
            stack.rotate(quaternion);
            stack.translate(-.5, 0, -.5);

            stack.scale(scale, scale, scale);

            RenderSystem.enableDepthTest();
            //to make a mask to do depth test for other screens
            stack.push();
            //render the background behind the views, move a little to avoid Z-Fight
            stack.translate(0, 0, 1 / 16f);
            screen1.renderBackground(stack);
            stack.pop();

            int mx = -1, my = -1;

            if (sc == ScreenManager.getInstance().getFsc()) {
                //render point
                mx = (int) ScreenManager.getInstance().getLoc().x;
                my = (int) ScreenManager.getInstance().getLoc().y;
            }

            screen1.render(stack, mx, my, partialTicks);

            if (sc == ScreenManager.getInstance().getFsc()) {
                //render point
                GUIRenderer.getInstance().fill(stack, mx - 1, my + 1, mx + 1, my - 1, colorInt(255, 0, 0, 255));
            }

            stack.pop();
            stack.pop();
        });
    }
}
