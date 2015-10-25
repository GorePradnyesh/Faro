package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.frontend.data.Poll;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.util.List;
import java.util.Set;

public interface PollHandler {
    void getPolls(final BaseFaroRequestCallback<List<Poll>> callback, String eventId);
    void getPoll(final BaseFaroRequestCallback<Poll> callback, final String eventId, final String pollId);
    void createPoll(BaseFaroRequestCallback<Poll> callback, String eventId, Poll poll);
    void getUnvotedCount(final BaseFaroRequestCallback<Integer> callback, String eventId);
    void castVote(final BaseFaroRequestCallback<String> callback, final String eventId, final String pollId, Set<String> options);
    void closePoll(final BaseFaroRequestCallback<String> callback, final String eventId, final String pollId);
    void deletePoll(final BaseFaroRequestCallback<String> callback, final String eventId, final String pollId);   
}
