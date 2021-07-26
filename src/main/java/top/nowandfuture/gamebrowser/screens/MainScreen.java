package top.nowandfuture.gamebrowser.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.montoyo.mcef.api.IBrowser;
import net.montoyo.mcef.api.IDisplayHandler;
import org.lwjgl.glfw.GLFW;
import top.nowandfuture.gamebrowser.InGameBrowser;
import top.nowandfuture.gamebrowser.ScreenManager;
import top.nowandfuture.gamebrowser.gui.BrowserView;
import top.nowandfuture.gamebrowser.gui.InWorldTextTipEvent;
import top.nowandfuture.gamebrowser.gui.TitleView;
import top.nowandfuture.gamebrowser.utils.RenderHelper;
import top.nowandfuture.mygui.*;
import top.nowandfuture.mygui.components.Button;
import top.nowandfuture.mygui.components.EditorView;
import top.nowandfuture.mygui.layouts.LinearLayout;

import javax.annotation.Nonnull;

public class MainScreen extends MyScreen {
    private BrowserView browserView;
    protected String id;

    protected MainScreen(String id, ITextComponent titleIn) {
        super(titleIn);
        this.id = id;
    }

    @Override
    protected void onDrawBackgroundLayer(MatrixStack stack, int vOffset) {
        // draw the rectangle into depth buffer, color is not important
        AbstractGui.fill(stack, 0, 0, width, height, RenderHelper.colorInt(50, 50, 50, 255));
    }

    private final ResourceLocation ARROW = new ResourceLocation(InGameBrowser.ID, "textures/gui/arrow_down.png");
    @Override
    public void render(@Nonnull MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        if(getRootView().getFocusedView() != null) {
            if (left) {
                stack.push();
                Quaternion quaternion = new Quaternion(new Vector3f(0, 0, 1), 90, true);
                stack.translate(0, (height - 64) / 2f, 0);
                stack.rotate(quaternion);
                RenderHelper.blit1(stack, 0, 0, 0, 0, 0, 64, 64, 64, 64, ARROW);
                stack.pop();
            } else if (right) {
                stack.push();
                Quaternion quaternion = new Quaternion(new Vector3f(0, 0, 1), -90, true);
                stack.translate(width, (height - 64) / 2f + 64, 0);
                stack.rotate(quaternion);
                RenderHelper.blit1(stack, 0, 0, 0, 0, 0, 64, 64, 64, 64, ARROW);
                stack.pop();
            }

            if (top) {
                stack.push();
                Quaternion quaternion = new Quaternion(new Vector3f(0, 0, 1), 180, true);
                stack.translate((width - 64) / 2f + 64, 0, 0);
                stack.rotate(quaternion);
                RenderHelper.blit1(stack, 0, 0, 0, 0, 0, 64, 64, 64, 64, ARROW);
                stack.pop();
            } else if (bottom) {
                stack.push();
                stack.translate((width - 64) / 2f, height, 0);
                RenderHelper.blit1(stack, 0, 0, 0, 0, 0, 64, 64, 64, 64, ARROW);
                stack.pop();
            }
        }
    }

    @Override
    protected void onLoad() {
        LinearLayout.LinearLayoutParameter contentParas = new LinearLayout.LinearLayoutParameter(true, false, false);
        contentParas.setFlowParentHeight(true);
        contentParas.setFlowParentHeight(true);
        LinearLayout contentLayout = new LinearLayout(getRootView());
        contentLayout.pushLayoutParameter(contentParas);
        contentLayout.setBackgroundColor(new Color(50, 50, 50));

        //Tile View
        //titles
        TitleView titleView = new TitleView(contentLayout);
        titleView.setHeight(24);
        titleView.setFlowParentWidth(true);
        titleView.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v, int mouseX, int mouseY, int btn) {
                ScreenManager.getInstance().removeBy(MainScreen.this);
                MainScreen.this.closeScreen();

            }
        });

        //Search Box
        LinearLayout.LinearLayoutParameter searchParas = new LinearLayout.LinearLayoutParameter(false, true, false);
        LinearLayout searchLine = new LinearLayout(contentLayout, searchParas);
        searchLine.pushLayoutParameter(searchParas);

        searchLine.setFlowParentWidth(true);
        searchLine.setHeight(24);
        searchLine.setPadTop(2);
        searchLine.setPadBottom(2);

        Button back = new Button(searchLine);
        back.setFlowParentHeight(true);
        back.setWidth(26);
        back.setText("<");
        back.setVanillaStyle(false);
        //rgba(71, 71, 71, 1)
        back.setButtonColor(new Color(71, 71, 71));
        back.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v, int mouseX, int mouseY, int btn) {
                browserView.goBack();
            }
        });

        Button forward = new Button(searchLine);
        forward.setFlowParentHeight(true);
        forward.setWidth(26);
        forward.setText(">");
        forward.setVanillaStyle(false);
        //rgba(71, 71, 71, 1)
        forward.setButtonColor(new Color(71, 71, 71));
        forward.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v, int mouseX, int mouseY, int btn) {
                browserView.goForward();
            }
        });

        //contents
        EditorView editorView = new EditorView(searchLine);
        editorView.setDrawShadow(false);
        editorView.setDrawDecoration(false);
        editorView.setPadLeft(6);

        //rgba(143, 210, 229, 1)
        editorView.setSelectionColor(new Color(143, 240, 229));
        editorView.setEditable(true);
        editorView.setHeight(10);
        editorView.setFlowParentWidth(true);

        //search button
        Button searchBtn = new Button(searchLine);
        searchBtn.setFlowParentHeight(true);
        searchBtn.setWidth(60);
        searchBtn.setText("search");
        searchBtn.setVanillaStyle(false);
        //rgba(71, 71, 71, 1)
        searchBtn.setButtonColor(new Color(71, 71, 71));
        searchBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v, int mouseX, int mouseY, int btn) {
                browserView.setUrlToLoad(editorView.getText());
            }
        });

        //Browser View
        browserView = new BrowserView(contentLayout);
        browserView.setBrowserRenderId(this.id);
        browserView.setFlowParentWidth(true);
        browserView.setFlowParentHeight(true);
        browserView.setFocusListener(titleView::setFocus);
        browserView.setDisplayHandler(new IDisplayHandler() {
            @Override
            public void onAddressChange(IBrowser browser, String url) {
                editorView.setText(url);
            }

            @Override
            public void onTitleChange(IBrowser browser, String title) {
                titleView.setTile(title);
            }

            @Override
            public void onTooltip(IBrowser browser, String text) {
                setTooltip(text);
            }

            @Override
            public void onStatusMessage(IBrowser browser, String value) {
                post(new InWorldTextTipEvent(value, 1000, RootView.AbstractDataTipEvent.Position.LEFT_BOTTOM));
            }
        });

        contentLayout.setClipping(true);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return forceLoseFocus();
        }
        return getRootView().keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {// 0: left button, 1: right button, 2: mid button
        if (button == 1 && (left | right | top | bottom)) {
            ScreenManager.getInstance().resizeEntity(this, left, right, top, bottom);
            forceLoseFocus();
            return true;
        }else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    final int resizeArea = 50;
    final float p = .75f;

    boolean left, right, top, bottom;

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);

        left = right = top = bottom = false;
        //right area
        if (mouseX > width * p && mouseX > width - resizeArea) {
            right = true;
        }

        //left area
        if (mouseX < width * (1 - p) && mouseX < resizeArea) {
            left = true;
        }

        //top area
        if (mouseY < height * (1 - p) && mouseY < resizeArea) {
            top = true;
        }

        //bottom area
        if (mouseY > height * p && mouseY > height - resizeArea) {
            bottom = true;
        }
    }

    public static MainScreen create(String id, double width, double height) {

        MainScreen mainScreen = new MainScreen(id, ITextComponent.getTextComponentOrEmpty(id));
        double scale = ScreenManager.getInstance().getScale();
        mainScreen.setGuiLeft(0);
        mainScreen.setGuiTop(0);
        mainScreen.resize(Minecraft.getInstance(), (int) (width / scale), (int) (height / scale));

        return mainScreen;
    }
}
