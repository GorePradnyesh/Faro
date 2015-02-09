package com.example.nakulshah.listviewyoutube;

import java.util.ArrayList;

/**
 * Created by nakulshah on 2/3/15.
 */
public class Poll {
    private String _pollQuestion = null;
    ArrayList <String> _pollOptions = new ArrayList<>();
    private boolean _multiChoice = false;

    public String get_pollQuestion() {
        return _pollQuestion;
    }

    public void set_pollQuestion(String _pollQuestion) {
        this._pollQuestion = _pollQuestion;
    }

    public boolean isMultiChoice() {
        return _multiChoice;
    }

    public void setMultiChoice(boolean multiChoice) {
        this._multiChoice = multiChoice;
    }

    public void addNewPollOption(String pollOption){
        _pollOptions.add(pollOption);
    }

    public void removePollOptionAtPosition(int position){
        _pollOptions.remove(position);
    }


}
