package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by Richa on 10/27/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = WidgetRemoteViewsService.class.getSimpleName();
    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.MATCH_DAY
    };
    // these indices must match the projection
    private static final int INDEX_TIME_COL = 0;
    private static final int INDEX_HOME_COL = 1;
    private static final int INDEX_AWAY_COL = 2;
    private static final int INDEX_HOME_GOALS_COL = 3;
    private static final int INDEX_AWAY_GOALS_COL = 4;
    private static final int LEAGUE_COL = 5;
    private static final int MATCH_DAY = 6;
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Date currentDate = new Date(System.currentTimeMillis());
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");

                String[] todayDate = new String[1];
                todayDate[0]= mformat.format(currentDate);

                data = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                        SCORE_COLUMNS,
                        null,
                        todayDate,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);



                views.setTextViewText(R.id.widget_home_name,data.getString(INDEX_HOME_COL));
                views.setTextViewText(R.id.widget_away_name,data.getString(INDEX_AWAY_COL));
                views.setTextViewText(R.id.widget_score_textview, Utilies.getScores(data.getInt(INDEX_HOME_GOALS_COL), data.getInt(INDEX_AWAY_GOALS_COL)));
                views.setTextViewText(R.id.widget_data_textview,data.getString(INDEX_TIME_COL));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, R.id.widget_home_name,getString(R.string.team_label)+ data.getString(INDEX_HOME_COL));
                    setRemoteContentDescription(views, R.id.widget_away_name, getString(R.string.team_label)+data.getString(INDEX_AWAY_COL));

                    setRemoteContentDescription(views, R.id.widget_data_textview, getString(R.string.match_today)+getString(R.string.at)+data.getString(INDEX_TIME_COL));
                    if (data.getInt(INDEX_HOME_GOALS_COL) <0 || data.getInt(INDEX_AWAY_GOALS_COL) <0){
                        setRemoteContentDescription(views, R.id.widget_score_textview, "");
                    }else{
                        setRemoteContentDescription(views, R.id.widget_score_textview, getString(R.string.scores)+Utilies.getScores(data.getInt(INDEX_HOME_GOALS_COL), data.getInt(INDEX_AWAY_GOALS_COL)));
                    }


                }

                final Intent fillInIntent = new Intent();
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);


                return views;
            }

           @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, int viewId, String description) {
                views.setContentDescription(viewId, description);
            }


            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {

                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

