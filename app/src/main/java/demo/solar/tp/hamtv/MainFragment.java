package demo.solar.tp.hamtv;

import android.app.Activity;
import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.BackgroundManager;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Random;

import demo.solar.tp.hamtv.adapter.ItemModel;
import demo.solar.tp.hamtv.adapter.SwipeStackAdapter;
import demo.solar.tp.hamtv.databinding.FragmentMainBinding;
import demo.solar.tp.hamtv.widget.stack.SwipeStack;

/**
 * Created by filiperodrigues on 12/05/17.
 */

public class MainFragment extends Fragment implements SwipeStack.SwipeStackListener {
    private static final String TAG = "MainFragment";

    public static final int AUTO_SWIPE_INTERVAL = 3000;
    public static final int SWIPE_TO_ALPHA = 1;

    private ArrayList<ItemModel> mData;
    private SwipeStack mSwipeStack;
    private SwipeStackAdapter mAdapter;

    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private BackgroundManager mBackgroundManager;
    private FragmentMainBinding mBinding;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mBinding = DataBindingUtil.bind(view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUI();
    }

    private void setupUI() {
        Activity activity = getActivity();
        if(mBinding == null || activity == null)
            return;

        mData = new ArrayList<>();
        mAdapter = new SwipeStackAdapter(activity, mData);

        mSwipeStack = mBinding.swipeStack;
        mSwipeStack.setAdapter(mAdapter);
        mSwipeStack.setListener(this);

        fillWithTestData();
        setAutoSwipeHandler();

        fillWithRandomItemsAndRemove();
    }

    private void setAutoSwipeHandler() {
        final Handler scrollPageHandler = new Handler();
        scrollPageHandler.postDelayed(new Runnable() {
            public void run() {
                mSwipeStack.swipeTopViewToRight(SWIPE_TO_ALPHA);
                scrollPageHandler.postDelayed(this, AUTO_SWIPE_INTERVAL);
            }
        }, AUTO_SWIPE_INTERVAL);
    }

    // TODO: Remove this method
    private String getRandomImageURL(){
        Random random = new Random();
        int value = random.nextInt(5);
        switch(value){
            case 0:
                return "http://www.restauranteamaren.es/wp-content/uploads/comedor-03-restaurante-amaren.jpg";
            case 1:
                return "http://www.restauranteamaren.es/wp-content/uploads/mesas-restaurante-amaren.jpg";
            case 2:
                return "http://www.laparadadebilbao.es/wp-content/uploads/cafeteria-del-restaurante-la-parada-de-bilbao-vista-4.jpg";
            case 3:
                return "http://www.restauranteamaren.es/wp-content/uploads/terraza-restaurante-amaren.jpg";
            default:
                return "http://www.faroleiro.com/images/site/home/home-slide-06.jpg";
        }
    }

    // TODO: Remove this method
    private void fillWithTestData() {
        for (int i = 0; i < 5; i++) {
            mData.add(new ItemModel(i, getRandomImageURL(), null));
        }
    }

    // TODO: Remove this method
    private void fillWithRandomItemsAndRemove() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            int i = mData.size();

            @Override
            public void run() {

                if(mAdapter.getCount() < 10){
                    mAdapter.addItem(new ItemModel(i, getRandomImageURL(), null));
                    i++;
                }

                Random r = new Random();
                int rand = r.nextInt(2);

                if(rand == 0 && mData.size() > 4){
                    int index = r.nextInt(mAdapter.getCount());
                    Log.e("REMOVED", mData.get(index).getId() + "");
                    mAdapter.removeItem(index);
                }

                handler.postDelayed(this, 5000);
            }
        }, 5000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    @Override
    public void onViewSwipedToLeft() {

    }

    @Override
    public void onViewSwipedToRight() {

    }

    @Override
    public void onStackEmpty() {

    }
}
