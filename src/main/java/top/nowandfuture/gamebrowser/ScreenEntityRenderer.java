package top.nowandfuture.gamebrowser;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import top.nowandfuture.gamebrowser.gui.InWorldRenderer;
import top.nowandfuture.gamebrowser.screens.MainScreen;
import top.nowandfuture.mygui.GUIRenderer;
import top.nowandfuture.mygui.MyScreen;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiConsumer;

import static top.nowandfuture.gamebrowser.utils.RenderHelper.colorInt;

public class ScreenEntityRenderer extends EntityRenderer<ScreenEntity> {

    protected ScreenEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);
        GUIRenderer.getInstance().setRenderer(new InWorldRenderer());
    }

    private static final Map<ScreenEntity, Long> unfreezeTimeMap = new HashMap<>();
    private static final int MAX_WARMUP_TIME_UNIT = 100;//wait at least 5 seconds

    private static long check2UnfreezeMap(ScreenEntity entity){
        if(!unfreezeTimeMap.containsKey(entity)){
            unfreezeTimeMap.put(entity, System.currentTimeMillis());
        }
        return System.currentTimeMillis() - unfreezeTimeMap.get(entity);
    }

    private static void update(ScreenEntity entity){
        unfreezeTimeMap.put(entity, System.currentTimeMillis());
    }

    public static void clearUnfreezeMap(){
        List<ScreenEntity> removeSet = new LinkedList<>();
        unfreezeTimeMap.forEach(new BiConsumer<ScreenEntity, Long>() {
            @Override
            public void accept(ScreenEntity entity, Long aLong) {
                if(!entity.isAlive()){
                    removeSet.add(entity);
                }
            }
        });
        for (ScreenEntity e :
                removeSet) {
            unfreezeTimeMap.remove(e);
        }
    }

    public static Long removeUnfreezeEntity(ScreenEntity screenEntity){
        return unfreezeTimeMap.remove(screenEntity);
    }

    @Override
    public boolean shouldRender(ScreenEntity entity, ClippingHelper camera, double camX, double camY, double camZ) {
        double d0 = entity.getPosX() - camX;
        double d1 = entity.getPosY() - camY;
        double d2 = entity.getPosZ() - camZ;
        double distance = d0 * d0 + d1 * d1 + d2 * d2;
        boolean inDistance = entity.isInRangeToRenderDist(distance);

        if(!inDistance){
            //freeze is immediately
            if(!entity.isFreeze()) {
                entity.freezeScreen();
            }
            return false;
        }else{
            //unfreeze need the minimal  warmup time
            if(entity.isFreeze()) {
                d0 = entity.getBoundingBox().getAverageEdgeLength();
                if (Double.isNaN(d0)) {
                    d0 = 1.0D;
                }

                d0 = d0 * 64.0D * Entity.getRenderDistanceWeight();
                double waitTime = Math.max(0, Math.sqrt(distance) - Math.sqrt(d0) * 0.8) * MAX_WARMUP_TIME_UNIT;

                if(check2UnfreezeMap(entity) > MAX_WARMUP_TIME_UNIT * waitTime) {
                    entity.unfreezeScreen();
                    //update unfreeze time
                    update(entity);
                }
            }

            AxisAlignedBB axisalignedbb = entity.getRenderBoundingBox().grow(0.5D);
            if (axisalignedbb.hasNaN() || axisalignedbb.getAverageEdgeLength() == 0.0D) {
                axisalignedbb = new AxisAlignedBB(entity.getPosX() - 2.0D, entity.getPosY() - 2.0D, entity.getPosZ() - 2.0D, entity.getPosX() + 2.0D, entity.getPosY() + 2.0D, entity.getPosZ() + 2.0D);
            }
            return camera.isBoundingBoxInFrustum(axisalignedbb);
        }
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
        float scale = ScreenManager.BASE_SCALE / sc.getScale();

        double dot = sc.getLookVec().dotProduct(getRenderManager().info.getProjectedView().subtract(sc.getPositionVec()));
        if(dot <= 0) return;

        screen.ifPresent(screen1 -> {

            if(screen1 instanceof MainScreen){
                ((MainScreen) screen1).setOutSideLight(packedLightIn);
            }

            stack.push();
            stack.translate(0,  sc.getScreenHeight(), 0);
            float yaw = 180 - sc.rotationYaw;

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

            if (sc == ScreenManager.getInstance().getFocusedScreen()) {
                //render point
                mx = (int) ScreenManager.getInstance().getLoc().x;
                my = (int) ScreenManager.getInstance().getLoc().y;
            }

            screen1.render(stack, mx, my, partialTicks);

            if (sc == ScreenManager.getInstance().getFocusedScreen()) {
                //render point
                GUIRenderer.getInstance().fill(stack, mx - 1, my + 1, mx + 1, my - 1, colorInt(255, 0, 0, 255));
            }

            stack.pop();
            stack.pop();
        });
    }
}
