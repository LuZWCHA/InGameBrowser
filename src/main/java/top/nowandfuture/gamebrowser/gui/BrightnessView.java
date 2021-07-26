package top.nowandfuture.gamebrowser.gui;

import org.checkerframework.common.value.qual.IntRange;
import top.nowandfuture.gamebrowser.utils.RenderHelper;
import top.nowandfuture.mygui.Color;
import top.nowandfuture.mygui.RootView;
import top.nowandfuture.mygui.ViewGroup;
import top.nowandfuture.mygui.components.SliderView;
import top.nowandfuture.mygui.layouts.LinearLayout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public class BrightnessView extends LinearLayout {

    private SliderView sliderView;
    private Consumer<Float> changingListener;

    public BrightnessView(@Nonnull RootView rootView) {
        super(rootView);
    }

    protected BrightnessView(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    public BrightnessView(@Nonnull RootView rootView, ViewGroup parent, @Nonnull LinearLayoutParameter linearLayoutParameter) {
        super(rootView, parent, linearLayoutParameter);
    }

    public BrightnessView(ViewGroup parent, @Nonnull LinearLayoutParameter linearLayoutParameter) {
        super(parent, linearLayoutParameter);
    }

    @Override
    public void onCreate(RootView rootView, @Nullable ViewGroup parent) {
        sliderView = new SliderView(this);
        sliderView.setHeight(40);
        sliderView.setSliderHalfHeight(6);
        sliderView.setSliderHalfWidth(4);
        sliderView.setFlowParentWidth(true);
        sliderView.setRange(100, 0, 0);
        sliderView.setProgressChanging(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                Optional.ofNullable(changingListener)
                        .ifPresent(new Consumer<Consumer<Float>>() {
                            @Override
                            public void accept(Consumer<Float> floatConsumer) {
                                floatConsumer.accept(aFloat);
                            }
                        });
            }
        });
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        setBackgroundColor(new Color(0,0,0));
        pushLayoutParameter(new LinearLayoutParameter(true, true, true));
        setFlowParentHeight(true);
        setFlowParentWidth(true);
        setPadLeft(8);
        setPadRight(8);
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

    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return false;
    }

    public Consumer<Float> getChangingListener() {
        return changingListener;
    }

    public void setBrightness(@IntRange(to = 15) int brightness){
        sliderView.setProgress(brightness / 15f * 100);
    }

    public void setChangingListener(Consumer<Float> changingListener) {
        this.changingListener = changingListener;
    }
}
