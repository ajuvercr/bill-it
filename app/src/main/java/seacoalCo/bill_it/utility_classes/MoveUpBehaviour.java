package seacoalCo.bill_it.utility_classes;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

public class MoveUpBehaviour extends CoordinatorLayout.Behavior<View> {

    public MoveUpBehaviour() {
        super();
    }

    public MoveUpBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float deltaY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(deltaY);
        return true;
    }

    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, View child, View dependency) {
        ViewCompat.animate(child).translationY(0).start();
    }
}
