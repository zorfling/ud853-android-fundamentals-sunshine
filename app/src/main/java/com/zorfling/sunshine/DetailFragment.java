package com.zorfling.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import com.zorfling.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String SUNSHINE_APP_HASHTAG = " #SunshineApp";
    private static final int DETAIL_LOADER_ID = 2;
    private ShareActionProvider mShareActionProvider;
    private Uri mUri;
    private View rootView;
    private String mForecast;

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_WIND_SPEED = 6;
    static final int COL_WEATHER_DEGREES = 7;
    static final int COL_WEATHER_PRESSURE = 8;
    static final int COL_WEATHER_CONDITION_ID = 9;

    private TextView mDetailDateDayTextView;
    private TextView mDetailDateTextView;
    private TextView mDetailHighTextView;
    private TextView mDetailLowTextView;
    private TextView mDetailForecastTextView;
    private TextView mDetailHumidityTextView;
    private TextView mDetailWindTextView;
    private TextView mDetailPressureTextView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    public static DetailFragment newInstance(Uri uri) {
        DetailFragment detailFragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString("uri", uri.toString());
        detailFragment.setArguments(args);
        return detailFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != getArguments()) {
            String uriString = getArguments().getString("uri");
            if (null != uriString) {
                mUri = Uri.parse(uriString);
            }
        }

        View rootView =  inflater.inflate(R.layout.fragment_detail, container, false);

        mDetailDateDayTextView = (TextView) rootView.findViewById(R.id.detail_date_day_textview);
        mDetailDateTextView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mDetailHighTextView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mDetailLowTextView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mDetailForecastTextView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mDetailHumidityTextView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mDetailWindTextView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mDetailPressureTextView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + SUNSHINE_APP_HASHTAG);

        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        if (null == mUri) {
            return null;
        }
        return new CursorLoader(
                getActivity(),
                mUri,
                FORECAST_COLUMNS,
                null,
                null,
                null
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!cursor.moveToFirst()) {
            return;
        }
        final long dateInMillis = cursor.getLong(COL_WEATHER_DATE);
        boolean isMetric = Utility.isMetric(getActivity());

        String dayName = Utility.getDayName(getActivity(), dateInMillis);
        mDetailDateDayTextView.setText(dayName);

        String monthDayString = Utility.getFormattedMonthDay(getActivity(), dateInMillis);
        mDetailDateTextView.setText(monthDayString);

        String high = Utility.formatTemperature(getActivity(), cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        mDetailHighTextView.setText(high);

        String low = Utility.formatTemperature(getActivity(), cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        mDetailLowTextView.setText(low);

        String weatherDescription = cursor.getString(COL_WEATHER_DESC);
        mDetailForecastTextView.setText(weatherDescription);

        float humidity = cursor.getFloat(COL_WEATHER_HUMIDITY);
        mDetailHumidityTextView.setText(getString(R.string.format_humidity, humidity));


        float windSpeed = cursor.getFloat(COL_WEATHER_WIND_SPEED);
        float windDegrees = cursor.getFloat(COL_WEATHER_DEGREES);
        mDetailWindTextView.setText(Utility.getFormattedWind(getActivity(), windSpeed, windDegrees));

        float pressure = cursor.getFloat(COL_WEATHER_PRESSURE);
        mDetailPressureTextView.setText(getString(R.string.format_pressure, pressure));

        int weatherConditionId = cursor.getInt(COL_WEATHER_CONDITION_ID);
        ImageView iconImageView = (ImageView) getView().findViewById(R.id.detail_icon);
        iconImageView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherConditionId));

        // If oncreateoptionsmenu has happened, update share provider
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(Context context, double high, double low) {
        boolean isMetric = Utility.isMetric(getActivity());

        String highLowStr = Utility.formatTemperature(context , high, isMetric) + "/" + Utility.formatTemperature(context, low, isMetric);
        return highLowStr;
    }

    /*
    This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
    string.
 */
    private String convertCursorRowToUXFormat(Context context, Cursor cursor) {

        String highAndLow = formatHighLows(
                context,
                cursor.getDouble(COL_WEATHER_MAX_TEMP),
                cursor.getDouble(COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(COL_WEATHER_DATE)) +
                " - " + cursor.getString(COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    public void onLocationChanged(String newLocation) {
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER_ID, null, this);
        }

    }
}
