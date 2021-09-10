package top.nowandfuture.gamebrowser.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.montoyo.mcef.api.*;
import org.jline.utils.Log;
import top.nowandfuture.gamebrowser.InGameBrowser;
import top.nowandfuture.gamebrowser.utils.RenderHelper;
import top.nowandfuture.mygui.RootView;
import top.nowandfuture.mygui.ViewGroup;

import javax.annotation.Nullable;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class BrowserView extends ViewGroup {

    private String browserRenderId;
    private IBrowser browser;
    private String urlToLoad;
    // TODO: 2021/8/8 create the html home page for this mod
    private String home;

    private IDisplayHandler displayHandler;
    private IFocusListener focusListener;

    private int outsideLight = RenderHelper.SKY_LIGHT;

    public void setFocusListener(IFocusListener focusListener) {
        this.focusListener = focusListener;
    }

    public String getBrowserRenderId() {
        return browserRenderId;
    }

    public void setBrowserRenderId(String browserRenderId) {
        this.browserRenderId = browserRenderId;
    }

    public int getOutsideLight() {
        return outsideLight;
    }

    public void setOutsideLight(int outsideLight) {
        this.outsideLight = outsideLight;
    }

    public interface IFocusListener {
        void onFocusChanged(boolean f);
    }

    public BrowserView(RootView rootView) {
        super(rootView);
    }

    @Override
    public void onCreate(RootView rootView, @Nullable ViewGroup parent) {
        home = InGameBrowser.getHomePage();
    }

    public void setUrl(String urlToLoad) {
        Optional.ofNullable(browser)
                .ifPresent(iBrowser -> {
                    if(iBrowser.isActivate())
                        iBrowser.loadURL(urlToLoad);
                });
    }

    public void setUrlToLoad(String urlToLoad) {
        this.urlToLoad = urlToLoad;
    }

    public BrowserView(ViewGroup parent) {
        super(parent);
    }

    public IBrowser getBrowser() {
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
                    Optional.ofNullable(getDisplayHandler())
                            .ifPresent(iDisplayHandler -> {
                                if (getBrowser() == browser) {
                                    iDisplayHandler.onAddressChange(browser, url);
                                }
                            });
                }

                @Override
                public void onTitleChange(IBrowser browser, String title) {

                    Optional.ofNullable(getDisplayHandler())
                            .ifPresent(iDisplayHandler -> {
                                if (getBrowser() == browser) {
                                    iDisplayHandler.onTitleChange(browser, title);
                                }
                            });
                }

                @Override
                public void onTooltip(IBrowser browser, String text) {

                    Optional.ofNullable(getDisplayHandler())
                            .ifPresent(iDisplayHandler -> {
                                if (getBrowser() == browser) {
                                    iDisplayHandler.onTooltip(browser, text);
                                }
                            });
                }

                @Override
                public void onStatusMessage(IBrowser browser, String value) {

                    Optional.ofNullable(getDisplayHandler())
                            .ifPresent(iDisplayHandler -> {
                                if (getBrowser() == browser) {
                                    iDisplayHandler.onStatusMessage(browser, value);
                                }
                            });
                }
            });
            browser = api.createBrowser(urlToLoad == null ? home : urlToLoad);
            Optional.ofNullable(browser)
                    .ifPresent(iBrowser -> iBrowser.visitSource(
                            str -> {
                                //do nothing
                            }));
        } else {
            browser = null;
        }
        urlToLoad = null;
    }

    @Override
    protected void onLayout(int parentWidth, int parentHeight) {
        Optional.ofNullable(browser)
                .ifPresent((IBrowser iBrowser) -> {
                    if (iBrowser.isActivate() && getWidth() > 0 && getHeight() > 0) {
                        iBrowser.resize(getWidth(), getHeight());
                    }
                });
    }

    @Override
    public void destroy() {
        super.destroy();
        Log.info("close browser!");
        Optional.ofNullable(browser)
                .ifPresent(IBrowser::close);
    }

    @Override
    protected void onDraw(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        int[] packetLight = RenderHelper.decodeCombineLight(outsideLight);
        int light = RenderHelper.getCombineLight(packetLight[0], packetLight[1], RenderHelper.decodeCombineLight(RenderHelper.light)[1]);

        Optional.ofNullable(browser)
                .ifPresent(iBrowser -> {
                    ResourceLocation location = iBrowser.getTextureLocation();
                    Optional.ofNullable(location)
                            .ifPresent(resourceLocation -> RenderHelper.blit2(stack, 0, 0, 0, 0, 0f, getWidth(), getHeight(), getHeight(), getWidth(), light, location));

                });
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        Optional.ofNullable(urlToLoad)
                .ifPresent(s -> {
                    setUrl(urlToLoad);
                    if(browser != null && browser.isActivate() && !browser.isPageLoading())
                        urlToLoad = null;
                });

    }

    public void goBack() {
        Optional.ofNullable(browser)
                .ifPresent(IBrowser::goBack);
    }

    public void goForward() {
        Optional.ofNullable(browser)
                .ifPresent(IBrowser::goForward);
    }

    public boolean isActivate() {
        return Optional.ofNullable(browser)
                .map(IBrowser::isActivate)
                .orElse(false);
    }

    public boolean isPageLoading() {
        return Optional.ofNullable(browser)
                .map(IBrowser::isPageLoading)
                .orElse(false);
    }

    public String getUrlLoaded() {
        return Optional.ofNullable(browser)
                .map(IBrowser::getURL)
                .orElse(home);
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
        Optional.ofNullable(browser).ifPresent(iBrowser -> iBrowser.injectMouseButton(mouseX, mouseY, getMask(), remapBtn(state), false, 1));
    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {

        Optional<IBrowser> browserOptional = Optional.ofNullable(browser);

        return browserOptional
                .map(iBrowser -> {
                    iBrowser.injectMouseButton(mouseX, mouseY, getMask(), remapBtn(state), true, 1);
                    return true;
                })
                .orElse(false);


        //int x, int y, int mods, int btn, boolean pressed, int ccnt

    }

    @Override
    protected boolean onMouseScrolled(int mouseX, int mouseY, float delta) {
        Optional<IBrowser> browserOptional = Optional.ofNullable(browser);

        return browserOptional
                .map(iBrowser -> {
                    iBrowser.injectMouseWheel(mouseX, mouseY, getMask(), 1, ((int) delta * 100));
                    return true;
                })
                .orElseGet(() -> BrowserView.super.onMouseScrolled(mouseX, mouseY, delta));

    }

    int lastKeyCode;

    @Override
    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        //int x, int y, int mods, int btn, boolean pressed, int ccnt
        Optional<IBrowser> browserOptional = Optional.ofNullable(browser);
        return browserOptional
                .map(iBrowser -> {
                    iBrowser.injectKeyPressedByKeyCode(keyCode, (char) keyCode, getMask());
                    return true;
                })
                .orElseGet(() -> BrowserView.super.keyPressed(keyCode, scanCode, modifiers));
    }

    @Override
    protected boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        //int x, int y, int mods, int btn, boolean pressed, int ccnt
        Optional<IBrowser> browserOptional = Optional.ofNullable(browser);

        return browserOptional
                .map(iBrowser -> {
                    iBrowser.injectKeyReleasedByKeyCode(keyCode, (char) keyCode, getMask());
                    return true;
                })
                .orElseGet(() -> BrowserView.super.onKeyReleased(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean onKeyType(char typedChar, int mod) {
        //int x, int y, int mods, int btn, boolean pressed, int ccnt
        Optional<IBrowser> browserOptional = Optional.ofNullable(browser);

        return browserOptional
                .map(iBrowser -> {
                    iBrowser.injectKeyTyped(typedChar, lastKeyCode, mod);
                    return true;
                })
                .orElseGet(() -> BrowserView.super.onKeyType(typedChar, mod));
    }

    @Override
    protected boolean onMouseMoved(int mouseX, int mouseY) {
        //check the mouse btn was pressed or not
        Optional<IBrowser> browserOptional = Optional.ofNullable(browser);

        return browserOptional
                .map(iBrowser -> {
                    iBrowser.injectMouseMove(mouseX, mouseY, getMask(), mouseY < 0);
                    return true;
                })
                .orElseGet(() -> BrowserView.super.onMouseMoved(mouseX, mouseY));
    }

    @Override
    protected boolean onMouseDragged(int mouseX, int mouseY, int state, int dx, int dy) {
        Optional<IBrowser> browserOptional = Optional.ofNullable(browser);
        return browserOptional
                .map(iBrowser -> {
                    iBrowser.injectMouseDrag(mouseX, mouseY, remapBtn(state), dx, dy);
                    return true;
                })
                .orElseGet(() -> BrowserView.super.onMouseDragged(mouseX, mouseY, state, dx, dy));
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

    public IDisplayHandler getDisplayHandler() {
        return displayHandler;
    }

    public void setDisplayHandler(final IDisplayHandler displayHandler) {
        this.displayHandler = displayHandler;
    }

    @Override
    public void focused() {
        super.focused();
        Optional.ofNullable(focusListener)
                .ifPresent(iFocusListener -> iFocusListener.onFocusChanged(true));
    }

    @Override
    public void loseFocus() {
        super.loseFocus();
        Optional.ofNullable(focusListener)
                .ifPresent(iFocusListener -> iFocusListener.onFocusChanged(false));
    }
}
