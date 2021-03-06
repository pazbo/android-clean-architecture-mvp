/*
 * Copyright (C) 2015 Pau Picas Sans <pau.picas@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package cat.ppicas.cleanarch.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cat.ppicas.cleanarch.R;
import cat.ppicas.cleanarch.app.ServiceContainer;
import cat.ppicas.cleanarch.app.ServiceContainers;
import cat.ppicas.cleanarch.model.City;
import cat.ppicas.cleanarch.ui.activity.ActivityNavigator;
import cat.ppicas.cleanarch.ui.activity.ActivityNavigatorImpl;
import cat.ppicas.cleanarch.ui.adapter.CityAdapter;
import cat.ppicas.cleanarch.ui.vista.SearchCitiesVista;
import cat.ppicas.framework.ui.PresenterFactory;
import cat.ppicas.framework.ui.PresenterHolder;
import cat.ppicas.cleanarch.ui.presenter.SearchCitiesPresenter;

public class SearchCitiesFragment extends Fragment implements SearchCitiesVista,
        PresenterFactory<SearchCitiesPresenter> {

    private static final String STATE_LIST_GROUP_VISIBILITY = "listGroupVisibility";
    private static final String STATE_LIST_VISIBILITY = "listVisibility";
    private static final String STATE_EMPTY_VISIBILITY = "emptyVisibility";
    private static final String STATE_PROGRESS_VISIBILITY = "progressVisibility";

    private SearchCitiesPresenter mPresenter;

    private CityAdapter mAdapter;

    private SearchView mSearch;
    private View mListGroup;
    private ListView mList;
    private TextView mEmpty;
    private ProgressBar mProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = ((PresenterHolder) getActivity()).getOrCreatePresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        final View view = inflater.inflate(R.layout.fragment_search_cities, container, false);

        bindViews(view);

        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(view.getWindowToken(), 0);
                mPresenter.onCitySearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mAdapter = new CityAdapter(getActivity());
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city = mAdapter.getItem(position);
                mPresenter.onCitySelected(city.getId());
            }
        });

        mProgress.setVisibility(View.GONE);
        mEmpty.setVisibility(View.GONE);

        if (state != null) {
            restoreViewsState(state);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.start(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.stop();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(STATE_LIST_GROUP_VISIBILITY, mListGroup.getVisibility());
        state.putInt(STATE_LIST_VISIBILITY, mList.getVisibility());
        state.putInt(STATE_EMPTY_VISIBILITY, mEmpty.getVisibility());
        state.putInt(STATE_PROGRESS_VISIBILITY, mProgress.getVisibility());
    }

    @SuppressWarnings("ResourceType")
    public void restoreViewsState(Bundle state) {
        mListGroup.setVisibility(state.getInt(STATE_LIST_GROUP_VISIBILITY));
        mList.setVisibility(state.getInt(STATE_LIST_VISIBILITY));
        mEmpty.setVisibility(state.getInt(STATE_EMPTY_VISIBILITY));
        mProgress.setVisibility(state.getInt(STATE_PROGRESS_VISIBILITY));
    }

    @Override
    public void setTitle(int stringResId, Object... args) {
        getActivity().setTitle(getString(stringResId, args));
    }

    @Override
    public void setCities(List<City> cities) {
        mList.setVisibility(View.VISIBLE);
        mEmpty.setVisibility(View.GONE);
        mAdapter.clear();
        mAdapter.addAll(cities);
    }

    @Override
    public void displayCitiesNotFound() {
        mList.setVisibility(View.GONE);
        mEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayLoading(boolean show) {
        mListGroup.setVisibility(show ? View.GONE : View.VISIBLE);
        mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void displayError(int stringResId, Object... args) {
        Toast.makeText(getActivity().getApplicationContext(), stringResId, Toast.LENGTH_LONG).show();
    }

    @Override
    public SearchCitiesPresenter createPresenter() {
        ServiceContainer sc = ServiceContainers.getFromApp(getActivity());
        ActivityNavigator activityNavigator =  new ActivityNavigatorImpl(getActivity());
        return new SearchCitiesPresenter(sc.getTaskExecutor(), activityNavigator,
                sc.getCityRepository());
    }

    @Override
    public String getPresenterTag() {
        return SearchCitiesFragment.class.getName();
    }

    private void bindViews(View view) {
        mSearch = (SearchView) view.findViewById(R.id.search_cities__search);
        mListGroup = view.findViewById(R.id.search_cities__list_group);
        mList = (ListView) view.findViewById(R.id.search_cities__list);
        mEmpty = (TextView) view.findViewById(R.id.search_cities__empty);
        mProgress = (ProgressBar) view.findViewById(R.id.search_cities__progress);
    }
}
