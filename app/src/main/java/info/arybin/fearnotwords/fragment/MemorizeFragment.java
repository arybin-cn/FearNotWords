package info.arybin.fearnotwords.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.github.florent37.expectanim.ExpectAnim;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.arybin.fearnotwords.R;
import info.arybin.fearnotwords.Utils;
import info.arybin.fearnotwords.core.OperableQueue;
import info.arybin.fearnotwords.core.SimpleOperableQueue;
import info.arybin.fearnotwords.model.Memorable;
import info.arybin.fearnotwords.model.Translatable;
import info.arybin.fearnotwords.ui.view.layout.ObservableLayout;
import info.arybin.fearnotwords.ui.view.layout.SlidableLayout;

import static com.github.florent37.expectanim.core.Expectations.scale;
import static java.lang.Math.abs;

public class MemorizeFragment extends BaseFragment implements ObservableLayout.EventListener, SlidableLayout.OnSlideListener, SlidableLayout.DistanceInterpolator {

    @BindView(R.id.layoutMain)
    protected ObservableLayout layoutMain;

    @BindView(R.id.textViewBody)
    public TextView textViewBody;
    @BindView(R.id.textViewPronounce)
    public TextView textViewPronounce;
    @BindView(R.id.textViewTranslation)
    public TextView textViewTranslation;

    @BindView(R.id.layoutExample)
    public RelativeLayout layoutExample;
    @BindView(R.id.textViewExampleBody)
    public TextView textViewExampleBody;
    @BindView(R.id.textViewExampleTranslation)
    public TextView textViewExampleTranslation;

    @BindView(R.id.layoutSkip)
    protected SlidableLayout layoutSkip;
    @BindView(R.id.imageSkip)
    protected ImageView imageSkip;

    @BindView(R.id.layoutPronounce)
    protected SlidableLayout layoutPronounce;
    @BindView(R.id.imagePronounce)
    protected ImageView imagePronounce;

    @BindView(R.id.layoutPass)
    protected SlidableLayout layoutPass;
    @BindView(R.id.imagePass)
    protected ImageView imagePass;


    private OperableQueue<? extends Memorable> memorableQueue;


    private static final int LOCK_SLOP = 50;

    private static final int PRI_STATE_NORMAL = 0x1;
    private static final int PRI_STATE_LOOP = 0x2;


    private static final int MIN_STATE_TRANSLATION_HIDE = 0x1;
    private static final int MIN_STATE_TRANSLATION_SHOW = 0x2;
    private static final int MIN_STATE_TRANSLATION_WILL_SHOW = 0x4;
    private static final int MIN_STATE_TRANSLATION_WILL_HIDE = 0x8;
    private static final int MIN_STATE_TRANSLATION_LOCKED = 0x10;

    private static final int MIN_STATE_SKIP_LOCKED = 0x40;
    private static final int MIN_STATE_WILL_LOCK_SKIP = 0x100;
    private static final int MIN_STATE_WILL_UNLOCK_SKIP = 0x200;

    private static final int MIN_STATE_PRONOUNCE_LOCKED = 0x20;
    private static final int MIN_STATE_WILL_LOCK_PRONOUNCE = 0x1000;
    private static final int MIN_STATE_WILL_UNLOCK_PRONOUNCE = 0x2000;

    private static final int MIN_STATE_PASS_LOCKED = 0x80;
    private static final int MIN_STATE_WILL_LOCK_PASS = 0x400;
    private static final int MIN_STATE_WILL_UNLOCK_PASS = 0x800;


    private int primaryState = PRI_STATE_NORMAL;
    private int minorState = MIN_STATE_TRANSLATION_HIDE;
    private SlidableLayout pressedView;
    private float pressDownX;
    private float pressDownY;
    private float previousX;
    private float previousY;


    private void initialize() {
        ArrayList<? extends Memorable> tmp = getArguments().getParcelableArrayList(KEY_LOADED_MEMORABLE);
        memorableQueue = SimpleOperableQueue.buildFrom(tmp);

        initializedViews();
    }

    private void initializedViews() {


        layoutMain.setEventListener(this);

        layoutMain.addOnPressObserver(layoutSkip, layoutPronounce, layoutPass);
        layoutMain.addOnHoverObserver(layoutSkip, layoutPronounce, layoutPass
                , textViewTranslation, layoutExample);

        layoutSkip.setSlidableOffset(0, 0, 0, LOCK_SLOP);
        layoutSkip.setDistanceInterpolator(this);
        layoutPronounce.setSlidableOffset(0, 0, 0, LOCK_SLOP);
        layoutPronounce.setDistanceInterpolator(this);
        layoutPass.setSlidableOffset(0, 0, 0, LOCK_SLOP);
        layoutPass.setDistanceInterpolator(this);

        layoutSkip.setOnSlideListener(this);
        layoutPronounce.setOnSlideListener(this);
        layoutPass.setOnSlideListener(this);


        updateView(memorableQueue.current());
    }

    public void updateView(Memorable memorable) {
        updateView(memorable, 0);
    }

    public void updateView(Memorable memorable, int exampleIndex) {
        textViewBody.setText(memorable.getOriginal());
        textViewPronounce.setText(memorable.getPronounce());
        textViewTranslation.setText(memorable.getTranslation());
        Translatable example = memorable.getExampleAt(exampleIndex);
        textViewExampleBody.setText(example.getOriginal());
        textViewExampleTranslation.setText(example.getTranslation());
    }

    private Animation makeHoverInAnimation(boolean reverse) {
        if (reverse) {
            return AnimationUtils.loadAnimation(getContext(), R.anim.in_hover_reverse);
        }
        return AnimationUtils.loadAnimation(getContext(), R.anim.in_hover);
    }

    private Animation makeHoverOutAnimation(boolean reverse) {
        if (reverse) {
            return AnimationUtils.loadAnimation(getContext(), R.anim.out_hover_reverse);
        }
        return AnimationUtils.loadAnimation(getContext(), R.anim.out_hover);
    }

    private Animation makeFadeInAnimation(final View view) {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.in_fade);
        animation.setAnimationListener(new Utils.AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }
        });
        return animation;
    }

    private Animation makeFadeOutAnimation(final View view) {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.out_fade);
        animation.setAnimationListener(new Utils.AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }
        });
        return animation;
    }


    private void showTranslation() {


    }

    private void hideTranslation() {

    }


    private void addMinorState(int state) {
        minorState |= state;
    }

    private void removeMinorState(int state) {
        minorState &= (~state);
    }

    private boolean hasMinorState(int state) {
        return (minorState & state) != 0;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memorize, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialize();
    }

    @Override
    public boolean onBackPressed() {

        return false;
    }

    @Override
    public void onPressDown(View pressDownView, MotionEvent event) {
        pressDownX = event.getX();
        pressDownY = event.getY();
        previousX = pressDownX;
        previousY = pressDownY;

        switch (pressDownView.getId()) {
        }


        System.out.println("OnPressDown");
    }

    @Override
    public void onPressMove(View pressDownView, MotionEvent event) {
        float currentX = event.getX();
        float currentY = event.getY();
        if (pressDownView instanceof SlidableLayout) {
            //layoutSkip or layoutPronounce or layoutPass
            pressedView = (SlidableLayout) pressDownView;
            if (currentY - previousY < -1) {
                ((SlidableLayout) pressDownView).cancelSlide();
            }
        }


        previousX = currentX;
        previousY = currentY;
    }

    @Override
    public void onPressUp(final View pressDownView, MotionEvent event) {
        System.out.println("OnPressUp");
//        boolean shouldPass = false;
//        switch (pressDownView.getId()) {
//            case R.id.imagePass:
//                shouldPass = true;
//            case R.id.imageSkip:
//                updateView(next(shouldPass));
//                break;
//        }
    }

    @Override
    public void onHoverIn(View pressDownView, View viewOnHover, MotionEvent event) {
        System.out.println("HoverIn");
    }

    @Override
    public void onHoverOut(View pressDownView, View viewOnHover, MotionEvent event) {

        System.out.println("HoverOut");

    }

    @Override
    public boolean onHoverCancel(View pressDownView, View viewOnHover, MotionEvent event) {
        System.out.println("HoverCancel");

        return true;
    }

    @Override
    public void onSlide(SlidableLayout layout, float rateLeftRight, float rateUpDown) {
        System.out.println("OnSlide-" + rateLeftRight + ":" + rateUpDown);
        layout.setScaleX(1 + 0.5f * abs(rateUpDown));
        layout.setScaleY(1 + 0.5f * abs(rateUpDown));
    }

    @Override
    public void onSlideTo(SlidableLayout layout, SlidableLayout.Direction direction) {


    }

    @Override
    public void onSlideCanceled(SlidableLayout layout) {

    }

    @Override
    public void onStartSlide(SlidableLayout layout) {

    }

    @Override
    public void onCancelSlide(SlidableLayout layout) {
    }

    @Override
    public float interpolate(float offset) {
        return offset * 0.8f;
    }
}
