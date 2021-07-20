package top.nowandfuture.mygui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;

public class GUIRenderer implements IRenderer {
    private static GUIRenderer INSTANCE;

    private IRenderer renderer;

    private GUIRenderer(){
        renderer = new IRenderer.DefaultRenderer();
    }

    public static GUIRenderer getInstance() {
        if(INSTANCE == null){
            synchronized (GUIRenderer.class){
                if(INSTANCE == null)
                    INSTANCE = new GUIRenderer();
            }
        }
        return INSTANCE;
    }

    public void setRenderer(IRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void fill(MatrixStack matrixStack, int minX, int minY, int maxX, int maxY, int color) {
        renderer.fill(matrixStack, minX, minY, maxX, maxY, color);
    }

    @Override
    public void hLine(MatrixStack matrixStack, int minX, int maxX, int y, int color) {
        renderer.hLine(matrixStack, minX, maxX, y, color);
    }

    @Override
    public void vLine(MatrixStack matrixStack, int x, int minY, int maxY, int color) {
        renderer.vLine(matrixStack, x, minY, maxY, color);
    }

    @Override
    public int drawString(MatrixStack matrixStack, FontRenderer fontRenderer, String font, int x, int y, int color) {
        return renderer.drawString(matrixStack, fontRenderer, font, x, y, color);
    }

    @Override
    public int drawString(MatrixStack matrixStack, FontRenderer fontRenderer, IReorderingProcessor processor, int text, int x, int y) {
        return renderer.drawString(matrixStack, fontRenderer, processor, text, x, y);
    }

    @Override
    public void fillGradient(MatrixStack matrixStack, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        renderer.fillGradient(matrixStack, x1, y1, x2, y2, colorFrom, colorTo);
    }

    @Override
    public void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, String font, int text, int x, int y) {
        renderer.drawCenteredString(matrixStack, fontRenderer, font, text, x, y);
    }

    @Override
    public void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent font, int text, int x, int y) {
        renderer.drawCenteredString(matrixStack, fontRenderer, font, text, x, y);
    }

    @Override
    public int drawString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent font, int text, int x, int y) {
        return renderer.drawString(matrixStack, fontRenderer, font, text, x, y);
    }

    @Override
    public void blit(MatrixStack matrixStack, int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        renderer.blit(matrixStack, x, y, width, height, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
    }

    @Override
    public void blit(MatrixStack matrixStack, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight) {
        renderer.blit(matrixStack, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
    }

}
