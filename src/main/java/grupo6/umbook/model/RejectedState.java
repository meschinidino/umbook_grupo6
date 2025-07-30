package grupo6.umbook.model;

/**
 * Concrete implementation of the FriendRequestState for the REJECTED state.
 */
public class RejectedState implements FriendRequestState {

    @Override
    public String getStateName() {
        return "REJECTED";
    }

    @Override
    public FriendRequestState send(FriendRequest context) {
        // A rejected request can be sent again, which puts it back in the pending state
        return new PendingState();
    }

    @Override
    public FriendRequestState accept(FriendRequest context) {
        // Cannot accept a request that has been rejected
        throw new IllegalStateException("Cannot accept a rejected friend request");
    }

    @Override
    public FriendRequestState reject(FriendRequest context) {
        // Request is already rejected, no state change
        throw new IllegalStateException("Friend request has already been rejected");
    }

    @Override
    public boolean isPending() {
        return false;
    }

    @Override
    public boolean isAccepted() {
        return false;
    }

    @Override
    public boolean isRejected() {
        return true;
    }
}