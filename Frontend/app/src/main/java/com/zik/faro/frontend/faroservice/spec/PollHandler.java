package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.frontend.data.Poll;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.util.List;
import java.util.Set;

public interface PollHandler {
    void getPolls(final BaseFaroRequestCallback<List<Poll>> callback);
    void getPoll(final BaseFaroRequestCallback<Poll> callback, final String eventId, final String pollId);
    void createPoll(final BaseFaroRequestCallback<Void> callback, final Poll poll);
    void getUnvotedCount(final BaseFaroRequestCallback<Integer> callback);
    void castVote(final BaseFaroRequestCallback<String> callback, final String eventId, final String voteId, Set<String> options);
    void closePoll(final BaseFaroRequestCallback<String> callback, final String eventId, final String pollId);
    void deletePoll(final BaseFaroRequestCallback<Void> callabck, final String eventId, final String pollId);   
}
