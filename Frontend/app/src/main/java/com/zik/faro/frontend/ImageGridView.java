package com.zik.faro.frontend;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

/**
 * Created by granganathan on 7/9/16.
 */
public class ImageGridView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        String accessTokenString = "EAACEdEose0cBAARRS6q7Qs5GLEtHyzffEoqw1RKGkTo7kqAwdaXxtbkdDbCDOKY3Vz1OHv1JRr" +
                "uZCg1dAKe3cvLuJCgpPXAhxgvsLh1OCZCH5dq6iubbf9ccKKhXpS11ZCWLXuDzT7undtsLYgTj4lfdHYMS5pAk9nPcDagJgZDZD";
        String userId = "10155071787680006";

        GridView gridView = (GridView) findViewById(R.id.gridview);
        FbGraphApiService fbGraphApiService = new FbGraphApiService(accessTokenString, userId);
        fbGraphApiService.obtainUserData();
        fbGraphApiService.downloadImagesIntoGridView("test album001", gridView, this);
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
