package top.nowandfuture.gamebrowser.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.montoyo.mcef.api.*;
import top.nowandfuture.gamebrowser.screens.MainScreen;
import top.nowandfuture.gamebrowser.utils.RenderHelper;
import top.nowandfuture.mygui.GUIRenderer;
import top.nowandfuture.mygui.RootView;
import top.nowandfuture.mygui.ViewGroup;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BrowserView extends ViewGroup {

    private String browserRenderId;
    private Optional<IBrowser> browser;
    private String urlToLoad;
    private String home = "http://baidu.com";

    private Optional<IDisplayHandler> displayHandler;
    private Optional<IFocusListener> focusListener;

    public void setFocusListener(Optional<IFocusListener> focusListener) {
        this.focusListener = focusListener;
    }

    public String getBrowserRenderId() {
        return browserRenderId;
    }

    public void setBrowserRenderId(String browserRenderId) {
        this.browserRenderId = browserRenderId;
    }

    public interface IFocusListener{
        void onFocusChanged(boolean f);
    }

    public BrowserView(RootView rootView) {
        super(rootView);
    }

    public void setUrlToLoad(String urlToLoad) {
        this.urlToLoad = urlToLoad;
        browser.ifPresent(new Consumer<IBrowser>() {
            @Override
            public void accept(IBrowser iBrowser) {
                iBrowser.loadURL(urlToLoad);
                BrowserView.this.urlToLoad = null;
            }
        });
    }

    public BrowserView(ViewGroup parent){
        super(parent);
    }

    public Optional<IBrowser> getBrowser() {
        return browser;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        API api = MCEFApi.getAPI();
        if (api != null) {
            api.registerDisplayHandler(new IDisplayHandler() {
                @Override
                public void onAddressChange(IBrowser browser, String url) {
                    //ugliness codes...
                    if(getBrowser().isPresent() && getDisplayHandler().isPresent()){
                        if(getBrowser().get() == browser){
                            getDisplayHandler().get().onAddressChange(browser, url);
                        }
                    }
                }

                @Override
                public void onTitleChange(IBrowser browser, String title) {
                    if(getBrowser().isPresent() && getDisplayHandler().isPresent()){
                        if(getBrowser().get() == browser){
                            getDisplayHandler().get().onTitleChange(browser, title);
                        }
                    }
                }

                @Override
                public void onTooltip(IBrowser browser, String text) {
                    if(getBrowser().isPresent() && getDisplayHandler().isPresent()){
                        if(getBrowser().get() == browser){
                            getDisplayHandler().get().onTooltip(browser, text);
                        }
                    }
                }

                @Override
                public void onStatusMessage(IBrowser browser, String value) {
                    if(getBrowser().isPresent() && getDisplayHandler().isPresent()){
                        if(getBrowser().get() == browser){
                            getDisplayHandler().get().onStatusMessage(browser, value);
                        }
                    }
                }
            });
            browser = Optional.ofNullable(api.createBrowser(urlToLoad == null ? home : urlToLoad));
            browser.ifPresent(new Consumer<IBrowser>() {
                @Override
                public void accept(IBrowser iBrowser) {
                    iBrowser.visitSource(new IStringVisitor() {
                        @Override
                        public void visit(String str) {

                        }
                    });
                }
            });
        }else{
            browser = Optional.empty();

        }
        urlToLoad = null;
     }

    @Override
    protected void onLayout(int parentWidth, int parentHeight) {
        if(flowParentWidth){
            setWidthWithoutLayout(parentWidth);
        }
        if(flowParentHeight){
            setHeightWithoutLayout(parentHeight);
        }
        browser.ifPresent((IBrowser iBrowser) -> {
            if(iBrowser.isActivate() && getWidth() > 0 && getHeight() > 0) {
                iBrowser.resize(getWidth(), getHeight());
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();
        browser.ifPresent(IBrowser::close);
    }

    @Override
    protected void onDraw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        browser.ifPresent(iBrowser -> {
            ResourceLocation location = iBrowser.getTextureLocation();

            if(location != null) RenderHelper.blit1(stack, 0, 0, 0,0, 0, getWidth(), getHeight(), getHeight(), getWidth(), location);

        });

        if (!isHovering()) {
            isPressed = false;
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if(urlToLoad != null){
            setUrlToLoad(urlToLoad);
        }
    }

    public void goBack() {
        browser.ifPresent(IBrowser::goBack);
    }

    public void goForward() {
        browser.ifPresent(IBrowser::goForward);
    }

    public boolean isActivate() {
        return browser.isPresent();
    }

    public boolean isPageLoading() {
        return browser.isPresent() && browser.get().isPageLoading();
    }

    public String getUrlLoaded() {
        return browser.isPresent() ? browser.get().getURL() : Strings.EMPTY;
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {
        //int x, int y, int mods, int btn, boolean pressed, int ccnt
        if(isPressed)
            browser.ifPresent(iBrowser -> iBrowser.injectMouseButton(mouseX, mouseY, getMask(), remapBtn(state), false, 1));

        isPressed = false;
    }

    boolean isPressed = false;
    int btn = -1;
    int lastX = -1, lastY = -1;

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {

        isPressed = true;
        btn = state;
        //int x, int y, int mods, int btn, boolean pressed, int ccnt
        browser.ifPresent(iBrowser -> iBrowser.injectMouseButton(mouseX, mouseY, getMask(), remapBtn(state), true, 1));
        System.out.println(state);
        return browser.isPresent();
    }

    @Override
    protected boolean onMouseScrolled(int mouseX, int mouseY, float delta) {
        browser.ifPresent(iBrowser -> iBrowser.injectMouseWheel(mouseX, mouseY, getMask(), 1, ((int) delta * 100)));

        return browser.isPresent() || super.onMouseScrolled(mouseX, mouseY, delta);
    }

    int lastKeyCode;
    @Override
    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        //int x, int y, int mods, int btn, boolean pressed, int ccnt
        browser.ifPresent(iBrowser -> iBrowser.injectKeyPressedByKeyCode(keyCode, (char) keyCode, getMask()));
        return browser.isPresent() || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        //int x, int y, int mods, int btn, boolean pressed, int ccnt
        browser.ifPresent(iBrowser -> iBrowser.injectKeyReleasedByKeyCode(keyCode, (char) keyCode, getMask()));

        return browser.isPresent() || super.onKeyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onKeyType(char typedChar, int mod) {
        //int x, int y, int mods, int btn, boolean pressed, int ccnt
        browser.ifPresent(new Consumer<IBrowser>() {
            @Override
            public void accept(IBrowser iBrowser) {
                int mod = getMask();
                iBrowser.injectKeyTyped(typedChar, lastKeyCode, mod);
            }
        });
        return browser.isPresent() || super.onKeyType(typedChar, mod);
    }

    @Override
    protected boolean onMouseMoved(int mouseX, int mouseY) {
        //check the mouse btn was pressed or not
        if (isPressed) {
            if (lastX < 0 || lastY < 0) {
                lastX = mouseX;
                lastY = mouseY;
            }
            this.onMouseDragged(mouseX, mouseY, btn, mouseX - lastX, mouseY - lastY);
            lastX = mouseX;
            lastY = mouseY;
        } else {
            browser.ifPresent(iBrowser -> iBrowser.injectMouseMove(mouseX, mouseY, getMask(), mouseY < 0));

            return browser.isPresent();
        }
        return super.onMouseMoved(mouseX, mouseY);
    }

    @Override
    protected boolean onMouseDragged(int mouseX, int mouseY, int state, int dx, int dy) {
        browser.ifPresent(iBrowser -> iBrowser.injectMouseDrag(mouseX, mouseY, remapBtn(state), dx, dy));

        return browser.isPresent() || super.onMouseDragged(mouseX, mouseY, state, dx, dy);
    }

    private static int getMask() {
        return (Screen.hasShiftDown() ? MouseEvent.SHIFT_DOWN_MASK : 0) |
                (Screen.hasAltDown() ? MouseEvent.ALT_DOWN_MASK : 0) |
                (Screen.hasControlDown() ? MouseEvent.CTRL_DOWN_MASK : 0);
    }

    //remap from GLFW to AWT's button ids
    private int remapBtn(int btn) {
        if (btn == 0) {
            btn = MouseEvent.BUTTON1;
        } else if (btn == 1) {
            btn = MouseEvent.BUTTON3;
        } else {
            btn = MouseEvent.BUTTON2;
        }
        return btn;
    }

    public Optional<IDisplayHandler> getDisplayHandler() {
        return displayHandler;
    }

    public void setDisplayHandler(final Optional<IDisplayHandler> displayHandler) {
        this.displayHandler = displayHandler;
    }

    @Override
    public void focused() {
        super.focused();
        focusListener.ifPresent(iFocusListener -> iFocusListener.onFocusChanged(true));
    }

    @Override
    public void loseFocus() {
        super.loseFocus();
        focusListener.ifPresent(iFocusListener -> iFocusListener.onFocusChanged(false));
    }
}
