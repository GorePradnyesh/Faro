package com.zik.faro.applogic;

import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;
import com.zik.faro.data.FaroImageBase;
import com.zik.faro.data.FbImage;
import com.zik.faro.data.Group;
import com.zik.faro.data.ImageProvider;
import com.zik.faro.data.Poll;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.LargeProfileImage;
import com.zik.faro.data.user.SmallProfileImage;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.FaroImageDo;
import com.zik.faro.persistence.datastore.data.GroupDo;
import com.zik.faro.persistence.datastore.data.PollDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;
import com.zik.faro.persistence.datastore.data.user.LargeProfileImageDo;
import com.zik.faro.persistence.datastore.data.user.SmallProfileImageDo;

public class ConversionUtils {
	public static Event fromDo(EventDo eventDo) {
		Event event = new Event();
		event.setAssignment(eventDo.getAssignment());
		event.setControlFlag(eventDo.isControlFlag());
		event.setEndDate(eventDo.getEndDate());
		event.setStartDate(eventDo.getStartDate());
		event.setExpenseGroup(eventDo.getExpenseGroup());
		event.setLocation(eventDo.getLocation());
		event.setId(eventDo.getId());
		event.setEventName(eventDo.getEventName());
		event.setEventCreatorId(eventDo.getEventCreatorId());
		event.setEventDescription(eventDo.getEventDescription());
		event.setStatus(eventDo.getStatus());
		event.setVersion(eventDo.getVersion());
		return event;
	}
	
	public static EventDo toDo(Event event) {
		EventDo eventDo = new EventDo();
		eventDo.setAssignment(event.getAssignment());
		eventDo.setControlFlag(event.getControlFlag());
		eventDo.setEndDate(event.getEndDate());
		eventDo.setStartDate(event.getStartDate());
		eventDo.setExpenseGroup(event.getExpenseGroup());
		eventDo.setLocation(event.getLocation());
		eventDo.setId(event.getId());
		eventDo.setVersion(event.getVersion());
		eventDo.setEventName(event.getEventName());
		eventDo.setEventCreatorId(event.getEventCreatorId());
		eventDo.setEventDescription(event.getEventDescription());
		eventDo.setStatus(event.getStatus());
		return eventDo;
	}
	
	public static Group fromDo(GroupDo groupDo) {
		Group group = new Group();
		group.setId(groupDo.getId());
		group.setGroupName(groupDo.getGroupName());
		group.setAdmins(groupDo.getAdmins());
		group.setParticipants(groupDo.getParticipants());
		group.setImage(groupDo.getImage());
		group.setVersion(groupDo.getVersion());
		return group;
	}
	
	public static GroupDo toDo(Group group) {
		GroupDo groupDo = new GroupDo();
		groupDo.setId(group.getId());
		groupDo.setGroupName(group.getGroupName());
		groupDo.setAdmins(group.getAdmins());
		groupDo.setParticipants(group.getParticipants());
		groupDo.setImage(group.getImage());
		groupDo.setVersion(group.getVersion());
		return groupDo;
	}
	
	public static ActivityDo toDo(Activity activity) {
		ActivityDo activityDo = new ActivityDo();
		activityDo.setAssignment(activity.getAssignment());
		activityDo.setStartDate(activity.getStartDate());
		activityDo.setEndDate(activity.getEndDate());
		activityDo.setDescription(activity.getDescription());
		activityDo.setLocation(activity.getLocation());
		activityDo.setId(activity.getId());
		activityDo.setEventId(activity.getEventId());
		activityDo.setName(activity.getName());
		activityDo.setVersion(activity.getVersion());
		return activityDo;
	}
	
	public static Activity fromDo(ActivityDo activityDo) {
		Activity activity = new Activity();
		activity.setAssignment(activityDo.getAssignment());
		activity.setStartDate(activityDo.getStartDate());
		activity.setEndDate(activityDo.getEndDate());
		activity.setDescription(activityDo.getDescription());
		activity.setLocation(activityDo.getLocation());
		activity.setId(activityDo.getId());
		activity.setEventId(activityDo.getEventId());
		activity.setName(activityDo.getName());
		activity.setVersion(activityDo.getVersion());
		return activity;
	}
	
	public static Poll fromDo(PollDo pollDo) {
		Poll poll = new Poll();
		poll.setCreatorId(pollDo.getCreatorId());
		poll.setDeadline(pollDo.getDeadline());
		poll.setEventId(pollDo.getEventId());
		poll.setDescription(pollDo.getDescription());
		poll.setId(pollDo.getId());
		poll.setOwner(pollDo.getOwner());
		poll.setStatus(pollDo.getStatus());
		poll.setWinnerId(pollDo.getWinnerId());
		poll.setPollOptions(pollDo.getPollOptions());
		poll.setMultiChoice(pollDo.getMultiChoice());
		poll.setVersion(pollDo.getVersion());
		return poll;
	}
	
	public static PollDo toDo(Poll poll) {
		PollDo pollDo = new PollDo();
		pollDo.setCreatorId(poll.getCreatorId());
		pollDo.setDeadline(poll.getDeadline());
		pollDo.setEventId(poll.getEventId());
		pollDo.setDescription(poll.getDescription());
		pollDo.setId(poll.getId());
		pollDo.setOwner(poll.getOwner());
		pollDo.setStatus(poll.getStatus());
		pollDo.setWinnerId(poll.getWinnerId());
		pollDo.setPollOptions(poll.getPollOptions());
		pollDo.setMultiChoice(poll.getMultiChoice());
		pollDo.setVersion(poll.getVersion());
		return pollDo;
	}

	public static FaroUserDo toDo(FaroUser faroUser) {
		FaroUserDo faroUserDo = new FaroUserDo();
		faroUserDo.setId(faroUser.getId());
		faroUserDo.setFirstName(faroUser.getFirstName());
		faroUserDo.setLastName(faroUser.getLastName());
		faroUserDo.setMiddleName(faroUser.getMiddleName());
		faroUserDo.setExternalExpenseID(faroUser.getExternalExpenseID());
		faroUserDo.setTelephone(faroUser.getTelephone());
		faroUserDo.setAddress(faroUser.getAddress());
		faroUserDo.setInviteStatus(faroUser.getInviteStatus());
		faroUserDo.setTokens(faroUser.getTokens());
		faroUserDo.setUserTopic(faroUser.getUserTopic());
		faroUserDo.setVersion(faroUser.getVersion());
		faroUserDo.setLargeProfileImage(toDo(faroUser.getLargeProfileImage()));
        faroUserDo.setSmallProfileImage(toDo(faroUser.getSmallProfileImage()));
		return faroUserDo;
	}


	public static FaroUser fromDo(FaroUserDo faroUserDo) {
		FaroUser faroUser = new FaroUser();
		faroUser.setId(faroUserDo.getId());
		faroUser.setFirstName(faroUserDo.getFirstName());
		faroUser.setLastName(faroUserDo.getLastName());
		faroUser.setMiddleName(faroUserDo.getMiddleName());
		faroUser.setExternalExpenseID(faroUserDo.getExternalExpenseID());
		faroUser.setTelephone(faroUserDo.getExternalExpenseID());
		faroUser.setAddress(faroUserDo.getAddress());
		faroUser.setInviteStatus(faroUserDo.getInviteStatus());
		faroUser.setTokens(faroUserDo.getTokens());
		faroUser.setUserTopic(faroUserDo.getUserTopic());
		faroUser.setVersion(faroUserDo.getVersion());
		faroUser.setLargeProfileImage(fromDo(faroUserDo.getLargeProfileImage()));
		faroUser.setSmallProfileImage(fromDo(faroUserDo.getSmallProfileImage()));
		return faroUser;
	}

	public static FaroImageBase fromDo(FaroImageDo imageDo) {
        FaroImageBase faroImage;

		if (ImageProvider.FACEBOOK.equals(imageDo.getImageProvider())) {
			faroImage = new FbImage()
					.withFaroUserId(imageDo.getFaroUserId())
					.withImageName(imageDo.getImageName())
					.withAlbumName(imageDo.getAlbumName())
					.withCreatedTime(imageDo.getCreatedTime())
					.withEventId(imageDo.getEventId())
					.withPublicUrl(imageDo.getPublicUrl())
					.withHeight(imageDo.getHeight())
					.withWidth(imageDo.getWidth());
		} else {
			throw new IllegalStateException("Invalid image provider");
		}

		return faroImage;
	}

	public static FaroImageDo toDo(FaroImageBase faroImage) {
        FaroImageDo faroImageDo = new FaroImageDo()
                .withImageName(faroImage.getImageName())
                .withEventId(faroImage.getEventId())
                .withFaroUserId(faroImage.getFaroUserId())
                .withAlbumName(faroImage.getAlbumName())
                .withPublicUrl(faroImage.getPublicUrl())
                .withHeight(faroImage.getHeight())
                .withWidth(faroImage.getWidth())
                .withImageProvider(faroImage.getImageProvider())
                .withCreatedTime(faroImage.getCreatedTime());

        return faroImageDo;
    }

    public static LargeProfileImageDo toDo(LargeProfileImage largeProfileImage) {
	    if (largeProfileImage != null) {
            return new LargeProfileImageDo(largeProfileImage.getPublicUrl(), largeProfileImage.getImageProvider());
        }
        return null;
    }

    public static LargeProfileImage fromDo(LargeProfileImageDo largeProfileImageDo) {
        if (largeProfileImageDo != null) {
            return new LargeProfileImage(largeProfileImageDo.getPublicUrl(), largeProfileImageDo.getImageProvider());
        }
        return null;
    }

    public static SmallProfileImageDo toDo(SmallProfileImage smallProfileImage) {
        if (smallProfileImage != null) {
            return new SmallProfileImageDo(smallProfileImage.getPublicUrl(), smallProfileImage.getImageProvider());
        }
        return null;
    }

    public static SmallProfileImage fromDo(SmallProfileImageDo smallProfileImageDo) {
        if (smallProfileImageDo != null) {
            return new SmallProfileImage(smallProfileImageDo.getPublicUrl(), smallProfileImageDo.getImageProvider());
        }
        return null;
    }
}
