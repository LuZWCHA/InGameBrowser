package top.nowandfuture.mygui.layouts;


import top.nowandfuture.mygui.LayoutParameter;
import top.nowandfuture.mygui.RootView;
import top.nowandfuture.mygui.ViewGroup;
import top.nowandfuture.mygui.api.NotNull;

import javax.annotation.Nonnull;

//not finished
public class LinearLayout extends FrameLayout {

    public LinearLayout(@Nonnull RootView rootView) {
        super(rootView);
    }

    protected LinearLayout(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    public LinearLayout(@Nonnull RootView rootView, ViewGroup parent, @Nonnull LinearLayoutParameter linearLayoutParameter) {
        super(rootView, parent, linearLayoutParameter);
    }

    public LinearLayout(ViewGroup parent, @Nonnull LinearLayoutParameter linearLayoutParameter) {
        super(parent.getRoot(), parent, linearLayoutParameter);
    }

    @Override
    protected void onChildrenLayout() {
        final boolean v = isLayoutVertical();
        final boolean centerVertical = isLayoutCenterVertical();
        final boolean centerHorizontal = isLayoutCenterHorizontal();

        //sort y
        int startY = padTop;
        //sort x
        int startX = padLeft;

        int fpvCounts = 0;
        int fixedLength = 0;
        for (ViewGroup vg :
                children) {
            if (vg.isVisible()) {
                if (v) {
                    if (vg.isFlowParentWidth()) {
                        vg.setWidthWithoutLayout(Math.max(getWidth() - padLeft - padRight, 0));
                    }
                    if (vg.isFlowParentHeight()) {
                        fpvCounts++;
                    } else {
                        fixedLength += vg.getHeight();
                    }
                } else {
                    if (vg.isFlowParentHeight()) {
                        vg.setHeightWithoutLayout(Math.max(getHeight() - padBottom - padTop, 0));
                    }
                    if (vg.isFlowParentWidth()) {
                        fpvCounts++;
                    } else {
                        fixedLength += vg.getWidth();
                    }
                }
            }
        }


        final int autoLength = fpvCounts > 0 ?
                Math.max((v ? getHeight() : getWidth()) - fixedLength, 0) / fpvCounts :
                0;


        int vLength = 0, hLength = 0;
        for (ViewGroup vg :
                children) {
            if (vg.isVisible()) {
                if (v) {
                    if (vg.isFlowParentHeight()) {
                        vg.setHeightWithoutLayout(autoLength);
                    }
                    vg.setY(startY);
                    startY += vg.getHeight();
                    if (centerHorizontal) {
                        float abw = getWidth() - padLeft - padRight - vg.getWidth();
                        abw = abw * .5f;
                        vg.setX((int) (padLeft + abw));
                    }
                    vLength += vg.getHeight();
                } else {
                    if (vg.isFlowParentWidth()) {
                        vg.setWidthWithoutLayout(autoLength);
                    }
                    vg.setX(startX);
                    startX += vg.getWidth();
                    float abw = getHeight() - padBottom - padTop - vg.getHeight();
                    abw = abw * .5f;
                    if (centerVertical) {
                        vg.setY((int) (padTop + abw));
                    }
                    hLength += vg.getHeight();
                }
            }
        }

        int xOffset = 0, yOffset = 0;

        if (!children.isEmpty()) {
            final int firstX = children.get(0).getX();
            final int firstY = children.get(0).getY();
            if (centerVertical && v) {
                yOffset = (getHeight() - padBottom - padTop - vLength) / 2;
                yOffset = yOffset - firstY + padTop;
            } else if (centerHorizontal && !v) {
                xOffset = (getWidth() - padLeft - padRight - hLength) / 2;
                xOffset = xOffset - firstX + padLeft;
            }
        }

        for (ViewGroup vg :
                children) {
            if ((xOffset | yOffset) != 0) {
                vg.moveXY(xOffset, yOffset);
            }

            vg.layout(vg.getWidth(), vg.getHeight());
        }
    }

    protected boolean isLayoutVertical() {
        return getLayoutParameter(LinearLayoutParameter.class).isVertical();
    }

    protected boolean isLayoutCenterVertical() {
        return getLayoutParameter(LinearLayoutParameter.class).isCenterVertical();
    }

    protected boolean isLayoutCenterHorizontal() {
        return getLayoutParameter(LinearLayoutParameter.class).isCenterHorizontal();
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
    public void onWidthChanged(int old, int cur) {
    }

    @Override
    public void onHeightChanged(int old, int cur) {
    }

    public static class LinearLayoutParameter extends FrameLayoutParameter {
        private boolean vertical;
        private boolean centerVertical;
        private boolean centerHorizontal;
        private boolean reverse;

        public LinearLayoutParameter(boolean vertical, boolean centerVertical, boolean centerHorizontal) {
            super();
            this.vertical = vertical;
            this.centerVertical = centerVertical;
            this.centerHorizontal = centerHorizontal;
            this.reverse = false;
        }

        public void setVertical(boolean vertical) {
            this.vertical = vertical;
        }

        public boolean isVertical() {
            return vertical;
        }

        public boolean isCenterVertical() {
            return centerVertical;
        }

        public void setCenterVertical(boolean centerVertical) {
            this.centerVertical = centerVertical;
        }

        @NotNull
        @Override
        protected LayoutParameter createDefaultParameter() {
            return new LinearLayoutParameter(false, false, false);
        }

        public boolean isCenterHorizontal() {
            return centerHorizontal;
        }

        public void setCenterHorizontal(boolean centerHorizontal) {
            this.centerHorizontal = centerHorizontal;
        }

        public boolean isReverse() {
            return reverse;
        }

        public void setReverse(boolean reverse) {
            this.reverse = reverse;
        }
    }
}
