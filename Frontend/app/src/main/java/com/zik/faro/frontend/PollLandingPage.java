package com.zik.faro.frontend;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;


public class PollLandingPage extends Activity {

    private  static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static Poll P;
    private LinearLayout pollOptionsChecklist = null;
    private RadioGroup pollOptionsRadioGroup = null;
    private int radioId = 0;
    ArrayList<String> checklistIds = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_landing_page);

        TextView pollDesc = (TextView)findViewById(R.id.pollDescription);

        ImageButton editButton = (ImageButton)findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);

        Button statusYes = (Button)findViewById(R.id.statusYes);
        Button statusNo = (Button)findViewById(R.id.statusNo);


        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String pollID = extras.getString("pollID");
            P = pollListHandler.getPollFromMap(pollID);
            if(P != null) {
                pollDesc.setText(P.getDescription());
                //Depending on the type of poll it will be displayed differently
                if(P.isMultiChoice()) {
                    pollOptionsChecklist = (LinearLayout) findViewById(R.id.pollOptionsChecklist);
                    List<Poll.PollOption> pollOptions = P.getPollOptions();
                    for (int i = 0; i < pollOptions.size(); i++) {
                        final CheckBox checkBox = new CheckBox(this);
                        Poll.PollOption pollOption = pollOptions.get(i);
                        checkBox.setText(pollOption.getOptionDescription());
                        checkBox.setId(i*10);
                        checkBox.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CheckBox chkbox = (CheckBox)v;
                                Integer id = chkbox.getId();
                                if(chkbox.isChecked()){
                                    checklistIds.add(id.toString());
                                }else{
                                    checklistIds.remove(id.toString());
                                }
                            }
                        });
                        pollOptionsChecklist.addView(checkBox);
                    }
                }else{
                    pollOptionsRadioGroup = (RadioGroup) findViewById(R.id.pollOptionsRadioGroup);
                    List<Poll.PollOption> pollOptions = P.getPollOptions();
                    for (int i = 0; i < pollOptions.size(); i++) {
                        final RadioButton button = new RadioButton(this);
                        Poll.PollOption pollOption = pollOptions.get(i);
                        button.setText(pollOption.getOptionDescription());
                        pollOptionsRadioGroup.addView(button);
                    }
                }
            }
        }

        statusYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(P.isMultiChoice()) {
                    String ids = "Selected options are ";
                    for(int i = 0; i < checklistIds.size(); i++) {
                        String str_id = checklistIds.get(i);
                        int id = Integer.parseInt(str_id);
                        ids = ids.concat(str_id);
                        ids = ids.concat(",");
                    }
                    Toast.makeText(PollLandingPage.this, ids, LENGTH_LONG).show();
                }else {
                    radioId = pollOptionsRadioGroup.getCheckedRadioButtonId();
                    Toast.makeText(PollLandingPage.this, "Selected option is " + radioId, LENGTH_LONG).show();
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_poll_landing_page, menu);
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
