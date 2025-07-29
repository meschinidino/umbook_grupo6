package grupo6.umbook.model;

import java.time.LocalDateTime;

/**
 * Concrete implementation of the FriendRequestState for the PENDING state.
 */
public class PendingState implements FriendRequestState {

    @Override
    public String getStateName() {
        return "PENDING";
    }

    @Override
    public FriendRequestState send(FriendRequest context) {
        // A request in pending state cannot be sent again
        throw new IllegalStateException("Friend request is already pending");
    }

    @Override
    public FriendRequestState accept(FriendRequest context) {
        // Update the timestamp
        context.setUpdatedAt(LocalDateTime.now());
        
        // Add users as friends
        User sender = context.getSender();
        User receiver = context.getReceiver();
        sender.addFriend(receiver);
        
        // Return the new state
        return new AcceptedState();
    }

    @Override
    public FriendRequestState reject(FriendRequest context) {
        // Update the timestamp
        context.setUpdatedAt(LocalDateTime.now());
        
        // Return the new state
        return new RejectedState();
    }

    @Override
    public boolean isPending() {
        return true;
    }

    @Override
    public boolean isAccepted() {
        return false;
    }

    @Override
    public boolean isRejected() {
        return false;
    }
}