package com.zik.faro.frontend.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Sets;
import com.squareup.okhttp.Request;
import com.zik.faro.data.Item;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;
import com.zik.faro.data.UpdateCollectionRequest;
import com.zik.faro.data.UpdateRequest;
import com.zik.faro.frontend.util.FaroExceptionHandler;
import com.zik.faro.frontend.FaroIntentConstants;
import com.zik.faro.frontend.handlers.PollListHandler;
import com.zik.faro.frontend.ui.adapters.PollOptionsAdapter;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.widget.Toast.LENGTH_LONG;

public class EditPollActivity extends Activity {
    private PollListHandler pollListHandler = PollListHandler.getInstance();
    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();

    private String eventId = null;
    private String pollId;
    private Poll clonePoll;
    private Intent pollLandingPageIntent;

    private static String TAG = "EditPollActivity";

    private final List <PollOption> newPollOptionList = new LinkedList<>();
    private Context mContext;
    private TextView pollDescription = null;
    private CheckBox isMultiChoice = null;
    private EditText optionText = null;
    private ImageButton addNewOptionButton = null;
    private ListView editPollOptionsListView = null;
    private Button editPollOK = null;
    private Button deletePoll = null;
    private PollOptionsAdapter editPollOptionsAdapter = null;
    private Map<String, Object> map = null;
    private Set<String> updatedFields = Sets.newHashSet();
    private PopupWindow popupWindow = null;

    private LinearLayout linlaHeaderProgress = null;
    private RelativeLayout editPollRelativeLayout = null;
    private Bundle extras = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_poll);

        mContext = this;

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        editPollRelativeLayout = (RelativeLayout) findViewById(R.id.editPollRelativeLayout);
        editPollRelativeLayout.setVisibility(View.GONE);

        extras = getIntent().getExtras();
        if (extras == null) return; //TODO: How to handle such conditions

        try {
            setupPageDetails();
        } catch (FaroObjectNotFoundException e) {
            //Poll has been deleted.
            Toast.makeText(this, "Poll has been deleted", LENGTH_LONG).show();
            Log.e(TAG, MessageFormat.format("Poll {0} has been deleted", pollId));
            finish();
        }
    }

    private void setupPageDetails () throws FaroObjectNotFoundException{
        linlaHeaderProgress.setVisibility(View.GONE);
        editPollRelativeLayout.setVisibility(View.VISIBLE);
        eventId = extras.getString(FaroIntentConstants.EVENT_ID);
        pollId = extras.getString(FaroIntentConstants.POLL_ID);

        pollLandingPageIntent = new Intent(EditPollActivity.this, PollLandingPage.class);

        clonePoll = pollListHandler.getCloneObject(pollId);

        pollDescription = (TextView) findViewById(R.id.pollDescription);
        isMultiChoice = (CheckBox)findViewById(R.id.multiChoiceFlag);
        optionText = (EditText)findViewById(R.id.pollOptionEditText);

        addNewOptionButton = (ImageButton)findViewById(R.id.add_new_option);
        addNewOptionButton.setImageResource(R.drawable.plus);
        addNewOptionButton.setEnabled(false);

        editPollOptionsListView = (ListView)findViewById(R.id.editPollOptionsList);

        editPollOK = (Button) findViewById(R.id.editPollOK);
        editPollOK.setEnabled(true);
        deletePoll = (Button)findViewById(R.id.deletePoll);

        editPollOptionsAdapter = new PollOptionsAdapter(this, R.layout.poll_option_can_edit_row_style);
        editPollOptionsListView.setAdapter(editPollOptionsAdapter);

        for (Integer i = 0; i < clonePoll.getPollOptions().size(); i++){
            PollOption pollOption = clonePoll.getPollOptions().get(i);
            editPollOptionsAdapter.insert(pollOption, 0);
        }

        editPollOptionsAdapter.notifyDataSetChanged();
        editPollOptionsListView.setAdapter(editPollOptionsAdapter);

        pollDescription.setText(clonePoll.getDescription());
        isMultiChoice.setChecked(clonePoll.getMultiChoice());

        //Enable the addNewOptionButton only after Users enters an Option
        optionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addNewOptionButton.setEnabled(!(optionText.getText().toString().trim().isEmpty()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        addNewOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String optionDescription = optionText.getText().toString();
                PollOption pollOption = new PollOption(optionDescription);
                editPollOptionsAdapter.insert(pollOption, 0);
                newPollOptionList.add(pollOption);
                editPollOptionsAdapter.notifyDataSetChanged();
                optionText.setText("");
            }
        });

        editPollOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!newPollOptionList.isEmpty()) {
                    clonePoll.setPollOptions(newPollOptionList);
                    map = new HashMap<>();
                    map.put("poll", clonePoll);
                    updatePollToServer();
                }
            }
        });
        deletePoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmPollDeletePopUP(v);
            }
        });
    }

    private void updatePollToServer () {
        // Update the poll with the new options added
        UpdateCollectionRequest<Poll, PollOption> pollUpdateCollectionRequest = new UpdateCollectionRequest<>();
        pollUpdateCollectionRequest.setUpdate(clonePoll);
        pollUpdateCollectionRequest.setToBeAdded(newPollOptionList);

        serviceHandler.getPollHandler().updatePollOptions(new BaseFaroRequestCallback<Poll>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to send Poll update request");
            }

            @Override
            public void onResponse(final Poll poll, HttpError error) {
                if (error == null ) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Poll update succeeded");
                            pollListHandler.addPollToListAndMap(eventId, poll, mContext);
                            FaroIntentInfoBuilder.pollIntent(pollLandingPageIntent, eventId, pollId);
                            startActivity(pollLandingPageIntent);
                            finish();
                        }
                    });
                } else {
                    Log.e(TAG, MessageFormat.format("code = {0} , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId, pollId, pollUpdateCollectionRequest);
    }

    private void deletePollFromServer () {
        serviceHandler.getPollHandler().deletePoll(new BaseFaroRequestCallback<String>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to delete clonePoll");
            }

            @Override
            public void onResponse(String s, HttpError error) {
                if (error == null ) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            pollListHandler.removePollFromListAndMap(eventId, clonePoll, mContext);
                            popupWindow.dismiss();
                            Toast.makeText(EditPollActivity.this, clonePoll.getDescription() + "is Deleted", LENGTH_LONG).show();
                            finish();
                        }
                    });
                }else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId, pollId);
    }

    private void confirmPollDeletePopUP(View v) {
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.delete_popup, null);
        popupWindow = new PopupWindow(container, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT, true);

        TextView message = (TextView)container.findViewById(R.id.questionTextView);
        message.setText("Are you sure you want to delete the Poll?");
        Button delete = (Button)container.findViewById(R.id.popUpDeleteButton);
        Button cancel = (Button)container.findViewById(R.id.popUpCancelButton);

        popupWindow.showAtLocation(editPollRelativeLayout, Gravity.CENTER, 0, 0);

        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return false;
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePollFromServer();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }


    @Override
    public void onBackPressed() {
        FaroIntentInfoBuilder.pollIntent(pollLandingPageIntent, eventId, pollId);
        startActivity(pollLandingPageIntent);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if the version is same. It can be different if this page is loaded and a notification
        // is received for this later which updates the cache but clonedata on this page remains
        // stale.

        try {
            if (!pollListHandler.checkObjectVersionIfLatest(pollId, clonePoll.getVersion())) {
                setupPageDetails();
            }
        } catch (FaroObjectNotFoundException e) {
            //Poll has been deleted.
            Toast.makeText(this, "Poll has been deleted", LENGTH_LONG).show();
            Log.e(TAG, MessageFormat.format("Poll {0} has been deleted", pollId));
            finish();
        }
    }
}

