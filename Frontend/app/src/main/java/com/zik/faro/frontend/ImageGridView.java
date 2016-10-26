package com.zik.faro.frontend;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import java.util.List;

/**
 * Created by granganathan on 7/9/16.
 */
public class ImageGridView extends AppCompatActivity {
    private static final String TAG = "ImageGridView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        String accessTokenString = "EAACEdEose0cBAI1NuZCFd5lG4Ms33y8fShyUv8js8m4Xz4GkgMOxTXS4Nk4Gyj1WIzpynVIkYZC5bDIlhT" +
                "7H7BZBBBOx5lece36yZAJ7g4nsMWEuW5n7KIF87IhgF13vB8CizDZAITERTz0TtcdGlRbRwAPoThNmtAnzqp9DlSAZDZD";
        String userId = "10155071787680006";

        String eventName = getIntent().getStringExtra("eventName");
        Log.i(TAG, "eventName = " + eventName);

        GridView gridView = (GridView) findViewById(R.id.gridview);
        FbGraphApiService fbGraphApiService = new FbGraphApiService();
        gridView.setAdapter(new ImageAdapter(this, fbGraphApiService.obtainImageDownloadLinks(eventName)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_grid_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
