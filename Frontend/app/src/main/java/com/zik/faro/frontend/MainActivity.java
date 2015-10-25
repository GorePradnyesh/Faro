package com.zik.faro.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.squareup.okhttp.Request;
import com.zik.faro.frontend.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import junit.framework.Assert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MainActivity extends ActionBarActivity {


    protected static final String baseUrl = "http://10.0.2.2:8080/v1/";
    public static String uuidEmail = null;
    public static String password = null;
    public static FaroServiceHandler serviceHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final Button AppLandingPageBtn = (Button)findViewById(R.id.AppLandingPageBtn);
        try {
            serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        uuidEmail = UUID.randomUUID().toString()+ "@gmail.com";
        password = UUID.randomUUID().toString();
        try {
            getTokenForNewUser(uuidEmail, password);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if(AppLandingPageBtn != null) {
            AppLandingPageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent I = new Intent(MainActivity.this, EventListPage.class);
                    startActivity(I);
                }
            });
        }
    }

    final static class TestSignupCallback
            implements BaseFaroRequestCallback<String> {
        Semaphore semaphore;
        String token;
        TestSignupCallback(Semaphore semaphore){
            this.semaphore = semaphore;
        }

        @Override
        public void onFailure(Request request, IOException e) {
            semaphore.release();
        }

        @Override
        public void onResponse(String token, HttpError error){
            if(error != null){
                //Display message that signup failed.
            }
            this.token = token;
            semaphore.release();
        }
    }

    protected String getTokenForNewUser(final String username, final String password) throws InterruptedException, MalformedURLException {
        final Semaphore waitSem = new Semaphore(0);
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));

        TestSignupCallback callback = new TestSignupCallback(waitSem);
        serviceHandler.getSignupHandler().signup(callback, new FaroUser(username), password);
        boolean timeout;
        timeout = !waitSem.tryAcquire(30000, TimeUnit.MILLISECONDS);
        return callback.token;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
