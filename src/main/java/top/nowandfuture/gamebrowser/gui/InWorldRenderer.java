package top.nowandfuture.gamebrowser.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL20;
import top.nowandfuture.gamebrowser.InGameBrowser;
import top.nowandfuture.mygui.IRenderer;

import java.util.function.BiConsumer;

public class InWorldRenderer extends IRenderer.DefaultRenderer {
    public static final ResourceLocation WITHE = new ResourceLocation(InGameBrowser.ID, "textures/gui/white_backgroud.png");

    //To enable renderer's depthtest and disable the depthmask to draw the GUI in the world.
    //This renderer will reuse the  gui components but need to disable the alpha channel to avoid the depth test drop the
    //transparency fragments, besides, the blend function may also cause the wrong render because of the order of the GUI
    //components.

    //To solve all the render issue with the alpha channel, we need to reorder the render in the WorldRenderer:
    // 0. render sky, cloud, and so on...
    // 1. render all the solid and cutout block layers (and the GUI components' layers).
    // 2. render all entities (follow the 1st rules).
    // 3. render all tile entities (follow the 1st rules).
    // 4. render all transparency entities, water mask, and lines(debug and high light block selection box).
    // 5. render transparency block layers, string(Tripwire), and particles after sorted by the distance between camera and
    // the objects without depth mask (not write into the buffer).

    //We need to insert the GUI renderer into the step5 (transparency block layers) for all the transparency components' layers
    // but the others into the begin of step5 or the end of step4. It's not easy to modify the API designed for 2D GUI.
    public InWorldRenderer() {
        super();
    }

    private int blitOffset;

    public void hLine(MatrixStack matrixStack, int minX, int maxX, int y, int color) {
        if (maxX < minX) {
            int i = minX;
            minX = maxX;
            maxX = i;
        }

        fill(matrixStack, minX, y, maxX + 1, y + 1, color);
    }

    public void vLine(MatrixStack matrixStack, int x, int minY, int maxY, int color) {
        if (maxY < minY) {
            int i = minY;
            minY = maxY;
            maxY = i;
        }

        fill(matrixStack, x, minY + 1, x + 1, maxY, color);
    }

    public void fill(MatrixStack matrixStack, int minX, int minY, int maxX, int maxY, int color) {
        fill(matrixStack.getLast().getMatrix(), minX, minY, maxX, maxY, color);
    }

    private void fill(Matrix4f matrix, int minX, int minY, int maxX, int maxY, int color) {
        if (minX < maxX) {
            int i = minX;
            minX = maxX;
            maxX = i;
        }

        if (minY < maxY) {
            int j = minY;
            minY = maxY;
            maxY = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

//        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
//        RenderSystem.enableBlend();
//        RenderSystem.disableTexture();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.enableDepthTest();
//        RenderSystem.depthMask(false);
//        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
//        bufferbuilder.pos(matrix, (float) minX, (float) maxY, 0.0F).color(f, f1, f2, f3).endVertex();
//        bufferbuilder.pos(matrix, (float) maxX, (float) maxY, 0.0F).color(f, f1, f2, f3).endVertex();
//        bufferbuilder.pos(matrix, (float) maxX, (float) minY, 0.0F).color(f, f1, f2, f3).endVertex();
//        bufferbuilder.pos(matrix, (float) minX, (float) minY, 0.0F).color(f, f1, f2, f3).endVertex();
//        Tessellator.getInstance().draw();
//        RenderSystem.depthMask(true);
//        RenderSystem.enableTexture();
//        RenderSystem.disableBlend();

        IRenderTypeBuffer.Impl impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        RenderType renderType = RenderType.getText(WITHE);
        IVertexBuilder bufferbuilder = impl.getBuffer(renderType);
        bufferbuilder.pos(matrix, (float) minX, (float) maxY, 0.0F).color(f, f1, f2, f3).tex(0, 1).lightmap(15728640).endVertex();
        bufferbuilder.pos(matrix, (float) maxX, (float) maxY, 0.0F).color(f, f1, f2, f3).tex(1, 1).lightmap(15728640).endVertex();
        bufferbuilder.pos(matrix, (float) maxX, (float) minY, 0.0F).color(f, f1, f2, f3).tex(1, 0).lightmap(15728640).endVertex();
        bufferbuilder.pos(matrix, (float) minX, (float) minY, 0.0F).color(f, f1, f2, f3).tex(0, 0).lightmap(15728640).endVertex();

//        RenderSystem.defaultBlendFunc();
//        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        impl.finish(renderType);
        RenderSystem.depthMask(true);
//        RenderSystem.enableTexture();
//        RenderSystem.disableBlend();

    }

    public void fillGradient(MatrixStack matrixStack, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        fillGradient(matrixStack.getLast().getMatrix(), bufferbuilder, x1, y1, x2, y2, this.blitOffset, colorFrom, colorTo);
        tessellator.draw();
        RenderSystem.depthMask(true);
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    private void fillGradient(Matrix4f matrix, IVertexBuilder builder, int x1, int y1, int x2, int y2, int z, int colorA, int colorB) {
        float f = (float) (colorA >> 24 & 255) / 255.0F;
        float f1 = (float) (colorA >> 16 & 255) / 255.0F;
        float f2 = (float) (colorA >> 8 & 255) / 255.0F;
        float f3 = (float) (colorA & 255) / 255.0F;
        float f4 = (float) (colorB >> 24 & 255) / 255.0F;
        float f5 = (float) (colorB >> 16 & 255) / 255.0F;
        float f6 = (float) (colorB >> 8 & 255) / 255.0F;
        float f7 = (float) (colorB & 255) / 255.0F;
        builder.pos(matrix, (float) x2, (float) y1, (float) z).color(f1, f2, f3, f).endVertex();
        builder.pos(matrix, (float) x1, (float) y1, (float) z).color(f1, f2, f3, f).endVertex();
        builder.pos(matrix, (float) x1, (float) y2, (float) z).color(f5, f6, f7, f4).endVertex();
        builder.pos(matrix, (float) x2, (float) y2, (float) z).color(f5, f6, f7, f4).endVertex();
    }

    public void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, String font, int text, int x, int y) {
        fontRenderer.drawStringWithShadow(matrixStack, font, (float) (text - fontRenderer.getStringWidth(font) / 2), (float) x, y);
    }

    public void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent font, int text, int x, int y) {
        IReorderingProcessor ireorderingprocessor = font.func_241878_f();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        fontRenderer.drawTextWithShadow(matrixStack, ireorderingprocessor, (float) (text - fontRenderer.func_243245_a(ireorderingprocessor) / 2), (float) x, y);
        RenderSystem.depthMask(true);
    }

    public int drawString(MatrixStack matrixStack, FontRenderer fontRenderer, String font, int text, int x, int y) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        int i = fontRenderer.drawText(matrixStack, ITextComponent.getTextComponentOrEmpty(font), (float) text, (float) x, y);
        RenderSystem.depthMask(true);
        return i;
    }

    public int drawString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent font, int text, int x, int y) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        int i = fontRenderer.drawText(matrixStack, font, (float) text, (float) x, y);
        RenderSystem.depthMask(true);
        return i;
    }

    @Override
    public int drawString(MatrixStack matrixStack, FontRenderer font, IReorderingProcessor processor, int x, int y, int color) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        int i = font.func_238422_b_(matrixStack, processor, (float) x, (float) y, color);
        RenderSystem.depthMask(true);
        return i;
    }

    public void blitBlackOutline(int width, int height, BiConsumer<Integer, Integer> boxXYConsumer) {
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        boxXYConsumer.accept(width + 1, height);
        boxXYConsumer.accept(width - 1, height);
        boxXYConsumer.accept(width, height + 1);
        boxXYConsumer.accept(width, height - 1);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        boxXYConsumer.accept(width, height);
    }

    public void blit(MatrixStack matrixStack, int x, int y, int blitOffset, int width, int height, TextureAtlasSprite sprite) {
        innerBlit(matrixStack.getLast().getMatrix(), x, x + width, y, y + height, blitOffset, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
    }

    public void blit(MatrixStack matrixStack, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight) {
        blit(matrixStack, x, y, this.blitOffset, (float) uOffset, (float) vOffset, uWidth, vHeight, 256, 256);
    }

    public void blit(MatrixStack matrixStack, int x, int y, int blitOffset, float uOffset, float vOffset, int uWidth, int vHeight, int textureHeight, int textureWidth) {
        innerBlit(matrixStack, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    public void blit(MatrixStack matrixStack, int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        innerBlit(matrixStack, x, x + width, y, y + height, 0, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    public void blit(MatrixStack matrixStack, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight) {
        blit(matrixStack, x, y, width, height, uOffset, vOffset, width, height, textureWidth, textureHeight);
    }

    private void innerBlit(MatrixStack matrixStack, int x1, int x2, int y1, int y2, int blitOffset, int uWidth, int vHeight, float uOffset, float vOffset, int textureWidth, int textureHeight) {
        innerBlit(matrixStack.getLast().getMatrix(), x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / (float) textureWidth, (uOffset + (float) uWidth) / (float) textureWidth, (vOffset + 0.0F) / (float) textureHeight, (vOffset + (float) vHeight) / (float) textureHeight);
    }

    private static final RenderType QUAD = RenderType.makeType("quad",
            DefaultVertexFormats.POSITION_COLOR,
            GL20.GL_QUADS, 256,
            RenderType.State.getBuilder().build(false));
    private static final RenderType QUAD_TEX = RenderType.makeType("quad_tex",
            DefaultVertexFormats.POSITION_TEX,
            GL20.GL_QUADS, 2097152,
            RenderType.State.getBuilder().build(false));

    private void innerBlit(Matrix4f matrix, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV) {

        BufferBuilder builder = Tessellator.getInstance().getBuffer();
        RenderHelper.disableStandardItemLighting();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        builder.begin(7, DefaultVertexFormats.POSITION_TEX);
        builder.pos(matrix, (float) x1, (float) y2, (float) blitOffset).tex(minU, maxV).endVertex();
        builder.pos(matrix, (float) x2, (float) y2, (float) blitOffset).tex(maxU, maxV).endVertex();
        builder.pos(matrix, (float) x2, (float) y1, (float) blitOffset).tex(maxU, minV).endVertex();
        builder.pos(matrix, (float) x1, (float) y1, (float) blitOffset).tex(minU, minV).endVertex();
        Tessellator.getInstance().draw();

        RenderSystem.depthMask(true);
        RenderHelper.enableStandardItemLighting();

    }

    public int getBlitOffset() {
        return this.blitOffset;
    }

    public void setBlitOffset(int value) {
        this.blitOffset = value;
    }


    public static IRenderTypeBuffer typeBuffer;
}
