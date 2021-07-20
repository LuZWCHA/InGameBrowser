package top.nowandfuture.gamebrowser.utils;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.montoyo.mcef.api.IBrowser;
import org.lwjgl.opengl.GL20;
import top.nowandfuture.gamebrowser.InGameBrowser;
import top.nowandfuture.gamebrowser.gui.BrowserView;

import java.util.List;
import java.util.Optional;

public class RenderHelper {
    public static final RenderType QUAD = RenderType.makeType("quad",
            DefaultVertexFormats.POSITION_COLOR,
            GL20.GL_QUADS, 256,
            RenderType.State.getBuilder().build(false));

    public static final RenderState.TransparencyState TRANSPARENCY_STATE = new RenderState.TransparencyState("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    public static final RenderType QUAD_TEX = RenderType.makeType("quad_tex",
            DefaultVertexFormats.POSITION_TEX,
            GL20.GL_QUADS, 255650,
            RenderType.State.getBuilder()
                    .texturing(new RenderState.TexturingState("texture_enable",
                            new Runnable() {
                                @Override
                                public void run() {
                                    RenderSystem.enableTexture();
                                }
                            }, new Runnable() {
                        @Override
                        public void run() {
                            RenderSystem.disableTexture();
                            RenderSystem.bindTexture(0);
                        }
                    }))
                    .build(false));


    public static Vector4f getEntityRenderPos(Entity entityIn, double partialTicks) {
        final float renderX = (float) MathHelper.lerp(partialTicks, entityIn.lastTickPosX, entityIn.getPosX());
        final float renderY = (float) MathHelper.lerp(partialTicks, entityIn.lastTickPosY, entityIn.getPosY());
        final float renderZ = (float) MathHelper.lerp(partialTicks, entityIn.lastTickPosZ, entityIn.getPosZ());
        final float renderYaw = (float) MathHelper.lerp(partialTicks, entityIn.prevRotationYaw, entityIn.rotationYaw);

        return new Vector4f(renderX, renderY, renderZ, renderYaw);
    }

    public static int colorInt(int r, int g, int b, int a) {

        return (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
    }

    public static void blit(MatrixStack matrixStack, int x, int y, int blitOffset, int width, int height, TextureAtlasSprite sprite) {
        innerBlit(matrixStack.getLast().getMatrix(), x, x + width, y, y + height, blitOffset, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
    }

    public static void blit(MatrixStack matrixStack, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight) {
        blit(matrixStack, x, y, 0, (float) uOffset, (float) vOffset, uWidth, vHeight, 256, 256);
    }

    public static void blit(MatrixStack matrixStack, int x, int y, int blitOffset, float uOffset, float vOffset, int uWidth, int vHeight, int textureHeight, int textureWidth) {
        innerBlit(matrixStack, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    public static void blit1(MatrixStack matrixStack, int x, int y, int blitOffset, float uOffset, float vOffset, int uWidth, int vHeight, int textureHeight, int textureWidth, ResourceLocation id) {
        innerBlit(matrixStack, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight, id);
    }

    public static void blit(MatrixStack matrixStack, int x, int y, int blitOffset, float uOffset, float vOffset, int uWidth, int vHeight, int textureHeight, int textureWidth, ResourceLocation id) {
        innerBlit(matrixStack, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight, id);
    }

    public static void blit(MatrixStack matrixStack, int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        innerBlit(matrixStack, x, x + width, y, y + height, 0, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    public static void blit(MatrixStack matrixStack, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight) {
        blit(matrixStack, x, y, width, height, uOffset, vOffset, width, height, textureWidth, textureHeight);
    }

    private static void innerBlit(MatrixStack matrixStack, int x1, int x2, int y1, int y2, int blitOffset, int uWidth, int vHeight, float uOffset, float vOffset, int textureWidth, int textureHeight) {
        innerBlit(matrixStack.getLast().getMatrix(), x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / (float) textureWidth, (uOffset + (float) uWidth) / (float) textureWidth, (vOffset + 0.0F) / (float) textureHeight, (vOffset + (float) vHeight) / (float) textureHeight);
    }

    private static void innerBlit(MatrixStack matrixStack, int x1, int x2, int y1, int y2, int blitOffset, int uWidth, int vHeight, float uOffset, float vOffset, int textureWidth, int textureHeight, ResourceLocation id) {
        innerBlit(matrixStack.getLast().getMatrix(), x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / (float) textureWidth, (uOffset + (float) uWidth) / (float) textureWidth, (vOffset + 0.0F) / (float) textureHeight, (vOffset + (float) vHeight) / (float) textureHeight, id);
    }

//    private static final RenderType QUAD = RenderType.makeType("quad",
//            DefaultVertexFormats.POSITION_COLOR,
//            GL20.GL_QUADS, 256,
//            RenderType.State.getBuilder().build(false));
//    private static final RenderType QUAD_TEX = RenderType.makeType("quad_tex",
//            DefaultVertexFormats.POSITION_TEX,
//            GL20.GL_QUADS, 2097152,
//            RenderType.State.getBuilder().build(false));

    public final static int SKY_LIGHT = 15728640;
    public final static int EMMIT_BLOCK_LIGHT = 15728880;
    public static int light = SKY_LIGHT;

    public static int getCombineLight(World world, BlockPos pos){
        return WorldRenderer.getCombinedLight(world, pos);
    }

    private static void innerBlit(Matrix4f matrix, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV, ResourceLocation id) {

//        Minecraft.getInstance().getRenderTypeBuffers().getFixedBuilder().getBuilder()
        IRenderTypeBuffer.Impl renderTypeBuffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();

//        BufferBuilder builder = Tessellator.getInstance().getBuffer();
//        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        IVertexBuilder builder = renderTypeBuffer.getBuffer(RenderType.getText(id));
//        RenderSystem.enableDepthTest();
//        RenderSystem.depthMask(false);
//        RenderSystem.disableAlphaTest();
//        RenderSystem.enableTexture();
//        builder.begin(7, DefaultVertexFormats.POSITION_TEX);
//        IVertexBuilder builder = typeBuffer.getBuffer(QUAD_TEX);
        //   public static final VertexFormat POSITION_COLOR_TEX_LIGHTMAP = new VertexFormat(ImmutableList.<VertexFormatElement>builder().add(POSITION_3F).add(COLOR_4UB).add(TEX_2F).add(TEX_2SB).build());
        builder.pos(matrix, (float) x1, (float) y2, (float) blitOffset).color(255, 255, 255, 255).tex(minU, maxV).lightmap(light).endVertex();
        builder.pos(matrix, (float) x2, (float) y2, (float) blitOffset).color(255, 255, 255, 255).tex(maxU, maxV).lightmap(light).endVertex();
        builder.pos(matrix, (float) x2, (float) y1, (float) blitOffset).color(255, 255, 255, 255).tex(maxU, minV).lightmap(light).endVertex();
        builder.pos(matrix, (float) x1, (float) y1, (float) blitOffset).color(255, 255, 255, 255).tex(minU, minV).lightmap(light).endVertex();

        RenderSystem.depthMask(false);
        renderTypeBuffer.finish();
        RenderSystem.depthMask(true);
//        RenderSystem.bindTexture(id);
//        RenderSystem.enableTexture();
//        renderTypeBuffer.finish(QUAD_TEX);
//        RenderSystem.disableTexture();
//        RenderSystem.bindTexture(0);
//        RenderSystem.enableAlphaTest();
//        renderTypeBuffer.finish();
//        RenderSystem.depthMask(true);
//        net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
//        Tessellator.getInstance().draw();

    }

    private static void innerBlit(Matrix4f matrix, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV) {

        IRenderTypeBuffer.Impl renderTypeBuffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();

//        BufferBuilder builder = Tessellator.getInstance().getBuffer();
//        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        IVertexBuilder builder = renderTypeBuffer.getBuffer(QUAD_TEX);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
//        RenderSystem.disableAlphaTest();
//        RenderSystem.enableTexture();
//        builder.begin(7, DefaultVertexFormats.POSITION_TEX);
//        IVertexBuilder builder = typeBuffer.getBuffer(QUAD_TEX);
        builder.pos(matrix, (float) x1, (float) y2, (float) blitOffset).tex(minU, maxV).endVertex();
        builder.pos(matrix, (float) x2, (float) y2, (float) blitOffset).tex(maxU, maxV).endVertex();
        builder.pos(matrix, (float) x2, (float) y1, (float) blitOffset).tex(maxU, minV).endVertex();
        builder.pos(matrix, (float) x1, (float) y1, (float) blitOffset).tex(minU, minV).endVertex();
//        RenderSystem.enableAlphaTest();
        renderTypeBuffer.finish();
        RenderSystem.depthMask(true);
//        net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
//        Tessellator.getInstance().draw();

    }

//    RenderType getQuadTexType(){
//
//       final RenderType QUAD_TEX = RenderType.makeType("quad_tex",
//                DefaultVertexFormats.POSITION_TEX,
//                GL20.GL_QUADS, 256,
//                RenderType.State.getBuilder().build(false));
//
//    }

    public static RenderType curRenderType;

    public static class DynRenderState extends RenderState {

        public DynRenderState(String nameIn, Runnable setupTaskIn, Runnable clearTaskIn) {
            super(nameIn, setupTaskIn, clearTaskIn);
        }

        public static RenderType getRT(String name, int id) {
            return RenderType.makeType(name, DefaultVertexFormats.POSITION_TEX, GL20.GL_QUADS, 256,
                    RenderType.State.getBuilder()
                            .texture(TextureState.BLOCK_SHEET)
                            .texturing(new DynTextureState(id))
//                            .depthTest(RenderState.DEPTH_LEQUAL)
//                            .writeMask(RenderState.COLOR_WRITE)
                            .build(false));

        }

        public static class DynTextureState extends TexturingState {
            private int id;

            public DynTextureState(int tex_id) {
                super("dyn", new Runnable() {
                    @Override
                    public void run() {
                        RenderSystem.enableTexture();
                        RenderSystem.bindTexture(tex_id);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        RenderSystem.bindTexture(0);
                        RenderSystem.disableTexture();
                    }
                });

                this.id = tex_id;
            }
        }
    }
}
