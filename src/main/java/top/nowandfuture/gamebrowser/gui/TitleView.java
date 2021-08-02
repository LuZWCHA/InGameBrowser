package top.nowandfuture.gamebrowser.gui;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import top.nowandfuture.gamebrowser.InGameBrowser;
import top.nowandfuture.gamebrowser.ScreenManager;
import top.nowandfuture.gamebrowser.utils.RenderHelper;
import top.nowandfuture.mygui.*;
import top.nowandfuture.mygui.api.IAction;
import top.nowandfuture.mygui.api.IMyGui;
import top.nowandfuture.mygui.components.Button;
import top.nowandfuture.mygui.components.ImageView;
import top.nowandfuture.mygui.components.TextView;
import top.nowandfuture.mygui.layouts.FrameLayout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class TitleView extends FrameLayout {
    private TextView titleTv;
    private Button closeBtn;
    private ImageView focusImv;
    private Button settingBtn;
    private View.ActionListener actionListener;

    private MyScreen screen;

    private final ResourceLocation NO_FOCUSED_LOCATION =
            new ResourceLocation(InGameBrowser.ID, "textures/gui/no_focused.png");
    private final ResourceLocation FOCUSED_LOCATION =
            new ResourceLocation(InGameBrowser.ID, "textures/gui/focused.png");

    public TitleView(@Nonnull RootView rootView) {
        super(rootView);
    }

    public TitleView(@Nonnull RootView rootView, ViewGroup parent, @Nonnull FrameLayoutParameter layoutParameter) {
        super(rootView, parent, layoutParameter);
    }

    public TitleView(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    public TitleView(@Nonnull ViewGroup parent) {
        super(parent);
    }

    @Override
    protected void onCreate(RootView rootView, @Nullable ViewGroup parent) {
        super.onCreate(rootView, parent);
        titleTv = new TextView(this);
        closeBtn = new Button(this);
        focusImv = new InWorldImageView(this);
        settingBtn = new Button(this);
        setBackgroundColor(new Color(71, 71, 71));
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        closeBtn.setVanillaStyle(false);
        closeBtn.setText("X");
        closeBtn.setButtonColor(new Color(255, 0, 0));
        closeBtn.setButtonHoverColor(new Color(220, 20, 20));
        closeBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v, int mouseX, int mouseY, int btn) {
                onClose(v, mouseX, mouseY, btn);
            }
        });
        ScreenManager screenManager = ScreenManager.getInstance();
        settingBtn.setVanillaStyle(false);
        settingBtn.setText("S");
        closeBtn.setButtonColor(new Color(255, 0, 0));
        settingBtn.setButtonHoverColor(new Color(20, 20, 20));
        settingBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v, int mouseX, int mouseY, int btn) {
                SettingView view = new SettingView(getRoot());
                view.setHeight(120);
                view.setWidth(240);
                screenManager.findBy(screen).ifPresent(screenEntity -> {
                    view.setScale((int) (screenEntity.getScale() * 20));
                    //progress is from 0-100, and means scaling from 0% to 500%
                    view.setScaChangedListener(new Consumer<Float>() {
                        @Override
                        public void accept(Float aFloat) {
                            float sc = aFloat * .05f;
                            screenEntity.setScale(sc);
                        }
                    });
                    view.setBrightness(RenderHelper.decodeCombineLight(RenderHelper.light)[1]);
                    view.setBrChangingListener(aFloat -> RenderHelper.light = RenderHelper.getCombineLight(15, 0, (int)(0.15 * aFloat)));
                });


                Dialog dialog = getRoot().createDialogBuilder(view)
                        .hideDialog()
                        .build();
                dialog.setCenter()
                        .show();
            }
        });

        titleTv.setEnableTextShadow(false);
        titleTv.setBackgroundColor(new Color(50, 50, 50, 255));
        titleTv.setPadLeft(6);
        titleTv.setPadTop(2);
        titleTv.setPadBottom(2);

        focusImv.setImageLocation(NO_FOCUSED_LOCATION);
        focusImv.setClickable(true);
        focusImv.setPadBottom(4);
        focusImv.setPadTop(4);
        focusImv.setPadLeft(4);
        focusImv.setPadRight(4);
        focusImv.setActionClick(new IAction.ActionClick() {
            @Override
            public void clicked(IMyGui gui, int button) {
                screenManager.findBy(screen)
                        .ifPresent(screenEntity -> {
                            screenManager.setFollowScreen(screenEntity);
                            screenManager.setRotation4FollowingScreen(new Quaternion(0, 30, 0, true));
                        });
            }
        });
    }

    private void onClose(View v, int mouseX, int mouseY, int btn) {
        if (actionListener != null) {
            actionListener.onClicked(v, mouseX, mouseY, btn);
        }
    }

    public void setTile(String title) {
        titleTv.setText(title);
    }

    public String getTitle() {
        return titleTv.getText();
    }

    @Override
    protected void onChildrenLayout() {

        int w = getHeight();

        titleTv.setWidthWithoutLayout(Math.max(0, getWidth() - 3 * w));
        titleTv.setHeightWithoutLayout(Math.max(0, getHeight() - getPadBottom() - getPadTop()));
        int startW = titleTv.getWidth();

        focusImv.setX(startW);
        settingBtn.setX(startW + w);
        closeBtn.setX(startW + 2 * w);

        focusImv.setWidthWithoutLayout(w);
        focusImv.setHeightWithoutLayout(w);
        closeBtn.setWidthWithoutLayout(w);
        closeBtn.setHeightWithoutLayout(w);
        settingBtn.setWidthWithoutLayout(w);
        settingBtn.setHeightWithoutLayout(w);

        super.onChildrenLayout();
    }

    public void setCloseListener(View.ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setFocus(boolean focus) {
        focusImv.setImageLocation(focus ? FOCUSED_LOCATION : NO_FOCUSED_LOCATION);
    }

    public MyScreen getScreen() {
        return screen;
    }

    public void setScreen(MyScreen screen) {
        this.screen = screen;
    }
}
