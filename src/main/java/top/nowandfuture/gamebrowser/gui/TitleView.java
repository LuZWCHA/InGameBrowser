package top.nowandfuture.gamebrowser.gui;

import net.minecraft.util.ResourceLocation;
import org.lwjgl.system.CallbackI;
import top.nowandfuture.gamebrowser.InGameBrowser;
import top.nowandfuture.mygui.Color;
import top.nowandfuture.mygui.RootView;
import top.nowandfuture.mygui.View;
import top.nowandfuture.mygui.ViewGroup;
import top.nowandfuture.mygui.components.Button;
import top.nowandfuture.mygui.components.ImageView;
import top.nowandfuture.mygui.components.TextView;
import top.nowandfuture.mygui.layouts.FrameLayout;

import javax.annotation.Nonnull;

public class TitleView extends FrameLayout {
    private TextView titleTv;
    private Button closeBtn;
    private ImageView focusImv;
    private View.ActionListener actionListener;

    private final ResourceLocation NO_FOCUSED_LOCATION =
            new ResourceLocation(InGameBrowser.ID, "textures/gui/no_focused.png");
    private final ResourceLocation FOCUSED_LOCATION =
            new ResourceLocation(InGameBrowser.ID, "textures/gui/focused.png");

    public TitleView(@Nonnull RootView rootView) {
        super(rootView);
        init();
    }

    public TitleView(@Nonnull RootView rootView, ViewGroup parent, @Nonnull FrameLayoutParameter layoutParameter) {
        super(rootView, parent, layoutParameter);
        init();
    }

    public TitleView(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
        init();
    }

    public TitleView(@Nonnull ViewGroup parent) {
        super(parent);
        init();
    }

    private void init(){
        titleTv = new TextView(this);
        closeBtn = new Button(this);
        focusImv = new InWorldImageView(this);
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

        titleTv.setEnableTextShadow(false);
        titleTv.setBackgroundColor(new Color(50,50,50, 255));
        titleTv.setPadLeft(6);
        titleTv.setPadTop(2);
        titleTv.setPadBottom(2);

//        titleTv.setFlowParentHeight(true);
//        closeBtn.setFlowParentHeight(true);
//        focusImv.setFlowParentHeight(true);
        focusImv.setImageLocation(NO_FOCUSED_LOCATION);
        focusImv.setPadBottom(4);
        focusImv.setPadTop(4);
        focusImv.setPadLeft(4);
        focusImv.setPadRight(4);
    }

    private void onClose(View v, int mouseX, int mouseY, int btn){
        if(actionListener != null){
            actionListener.onClicked(v, mouseX, mouseY, btn);
        }
    }

    public void setTile(String title){
        titleTv.setText(title);
    }

    public String getTitle(){
        return titleTv.getText();
    }

    @Override
    protected void onChildrenLayout() {

        int w = getHeight();

        titleTv.setWidthWithoutLayout(Math.max(0, getWidth() - 2 * w));
        titleTv.setHeightWithoutLayout(Math.max(0, getHeight() - getPadBottom() - getPadTop()));
        int startW = titleTv.getWidth();

        focusImv.setX(startW);
        closeBtn.setX(startW + w);

        focusImv.setWidthWithoutLayout(w);
        focusImv.setHeightWithoutLayout(w);
        closeBtn.setWidthWithoutLayout(w);
        closeBtn.setHeightWithoutLayout(w);

        super.onChildrenLayout();
    }

    public void setActionListener(View.ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setFocus(boolean focus){
        focusImv.setImageLocation(focus ? FOCUSED_LOCATION: NO_FOCUSED_LOCATION);
    }
}
