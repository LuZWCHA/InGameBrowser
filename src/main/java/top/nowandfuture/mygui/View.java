package top.nowandfuture.mygui;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public abstract class View extends ViewGroup {
    //To disable viewgroup's children
    private final List<ViewGroup> children;

    public View(@Nonnull RootView rootView) {
        this(rootView, rootView.getTopView());
    }

    protected View(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
        children = null;
    }

    public View(@Nonnull ViewGroup parent) {
        this(parent.getRoot(), parent);
    }

    @Override
    public final void layout(int suggestWidth, int suggestHeight) {
        super.layout(suggestWidth, suggestHeight);
    }

    @Override
    protected final void onChildrenLayout() {
        //do nothing
    }

    @Override
    public void onLayout(int parentWidth, int parentHeight) {

    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return true;
    }

    @Override
    protected boolean onInterceptClickAction(int mouseX, int mouseY, int button) {
        return true;
    }

    @Override
    public boolean onKeyType(char typedChar, int mod) {
        return false;
    }

    /**
     * this method would execute at the first time of the view been load (at parent view's onLoad())
     * and it only execute one time on a view's life
     */
    @Override
    protected void onLoad() {

    }

    //------------------------------disable all children function----------------------------------

    @Override
    public final void addAll(Collection<ViewGroup> viewGroups) {

    }

    @Override
    public final void addChild(ViewGroup viewGroup) {

    }

    @Override
    public final void removeAllChildren() {

    }

    @Override
    public final void addChild(int index, ViewGroup viewGroup) {

    }

    @Override
    public final void addChildren(ViewGroup... viewGroup) {

    }

    @Override
    public final void removeChild(int index) {

    }

    @Override
    public final void removeChild(ViewGroup viewGroup) {

    }

    @Override
    public final ViewGroup getChild(int index) {
        return null;
    }

    @Override
    public final int getChildrenSize() {
        return 0;
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {

    }

    interface ClickListener {
        void onClicked(View v, int mouseX, int mouseY, int btn);

        void onLongClicked(View v, int mouseX, int mouseY, int btn);
    }

    public static abstract class ActionListener implements ClickListener {
        @Override
        public void onClicked(View v, int mouseX, int mouseY, int btn) {

        }

        @Override
        public void onLongClicked(View v, int mouseX, int mouseY, int btn) {

        }
    }
}
