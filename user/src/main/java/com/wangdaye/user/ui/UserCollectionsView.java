package com.wangdaye.user.ui;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.wangdaye.common.base.application.MysplashApplication;
import com.wangdaye.base.i.PagerView;
import com.wangdaye.common.presenter.pager.PagerScrollablePresenter;
import com.wangdaye.base.i.PagerManageView;
import com.wangdaye.common.ui.adapter.collection.CollectionAdapter;
import com.wangdaye.common.ui.adapter.multipleState.MiniErrorStateAdapter;
import com.wangdaye.common.ui.adapter.multipleState.MiniLoadingStateAdapter;
import com.wangdaye.common.base.adapter.footerAdapter.GridMarginsItemDecoration;
import com.wangdaye.common.ui.widget.MultipleStateRecyclerView;
import com.wangdaye.common.ui.widget.swipeBackView.SwipeBackCoordinatorLayout;
import com.wangdaye.common.ui.widget.swipeRefreshView.BothWaySwipeRefreshLayout;
import com.wangdaye.common.utils.BackToTopUtils;
import com.wangdaye.common.utils.helper.RecyclerViewHelper;
import com.wangdaye.common.utils.manager.ThemeManager;
import com.wangdaye.common.presenter.pager.PagerStateManagePresenter;
import com.wangdaye.user.R;
import com.wangdaye.user.R2;
import com.wangdaye.user.UserActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * User collections view.
 *
 * This view is used to show collections for
 * {@link UserActivity}.
 *
 * */

@SuppressLint("ViewConstructor")
public class UserCollectionsView extends BothWaySwipeRefreshLayout
        implements PagerView, BothWaySwipeRefreshLayout.OnRefreshAndLoadListener,
        MiniErrorStateAdapter.OnRetryListener {

    @BindView(R2.id.container_photo_list_recyclerView) MultipleStateRecyclerView recyclerView;

    private PagerStateManagePresenter stateManagePresenter;

    private boolean selected;
    private int index;
    private PagerManageView pagerManageView;

    public UserCollectionsView(UserActivity a, CollectionAdapter adapter,
                               boolean selected, int index, PagerManageView v) {
        super(a);
        this.init(adapter, selected, index, v);
    }

    // init.

    @SuppressLint("InflateParams")
    private void init(CollectionAdapter adapter, boolean selected, int index, PagerManageView v) {
        View contentView = LayoutInflater.from(getContext())
                .inflate(R.layout.container_photo_list_2, null);
        addView(contentView);

        ButterKnife.bind(this, this);
        initData(selected, index, v);
        initView(adapter);
    }

    private void initData(boolean selected, int index, PagerManageView v) {
        this.selected = selected;
        this.index = index;
        this.pagerManageView = v;
    }

    private void initView(CollectionAdapter adapter) {
        setColorSchemeColors(ThemeManager.getContentColor(getContext()));
        setProgressBackgroundColorSchemeColor(ThemeManager.getRootColor(getContext()));
        setOnRefreshAndLoadListener(this);
        setRefreshEnabled(false);
        setLoadEnabled(false);

        post(() -> setDragTriggerDistance(
                BothWaySwipeRefreshLayout.DIRECTION_BOTTOM,
                MysplashApplication.getInstance().getWindowInsets().bottom
                        + getResources().getDimensionPixelSize(R.dimen.normal_margin)
        ));

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(
                RecyclerViewHelper.getDefaultStaggeredGridLayoutManager(getContext())
        );
        recyclerView.addItemDecoration(new GridMarginsItemDecoration(getContext()));

        recyclerView.setAdapter(new MiniLoadingStateAdapter(), MultipleStateRecyclerView.STATE_LOADING);
        recyclerView.setAdapter(new MiniErrorStateAdapter(this), MultipleStateRecyclerView.STATE_ERROR);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                PagerScrollablePresenter.onScrolled(
                        UserCollectionsView.this,
                        recyclerView,
                        adapter.getRealItemCount(),
                        pagerManageView,
                        index,
                        dy
                );
            }
        });

        recyclerView.setState(MultipleStateRecyclerView.STATE_LOADING);
        stateManagePresenter = new PagerStateManagePresenter(recyclerView);
    }

    // interface.

    // pager view.

    @Override
    public State getState() {
        return stateManagePresenter.getState();
    }

    @Override
    public boolean setState(State state) {
        return stateManagePresenter.setState(state, selected);
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void setSwipeRefreshing(boolean refreshing) {
        setRefreshing(refreshing);
    }

    @Override
    public void setSwipeLoading(boolean loading) {
        setLoading(loading);
    }

    @Override
    public void setPermitSwipeRefreshing(boolean permit) {
        // do nothing.
    }

    @Override
    public void setPermitSwipeLoading(boolean permit) {
        setLoadEnabled(permit);
    }

    @Override
    public boolean checkNeedBackToTop() {
        return recyclerView.canScrollVertically(-1)
                && stateManagePresenter.getState() == State.NORMAL;
    }

    @Override
    public void scrollToPageTop() {
        BackToTopUtils.scrollToTop(recyclerView);
    }

    @Override
    public boolean canSwipeBack(int dir) {
        return stateManagePresenter.getState() != State.NORMAL
                || SwipeBackCoordinatorLayout.canSwipeBack(recyclerView, dir);
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    // on refresh an load listener.

    @Override
    public void onRefresh() {
        pagerManageView.onRefresh(index);
    }

    @Override
    public void onLoad() {
        pagerManageView.onLoad(index);
    }

    // on retry listener.

    @Override
    public void onRetry() {
        pagerManageView.onRefresh(index);
    }
}