package grupo6.umbook.model;

/**
 * Concrete implementation of the FriendRequestState for the ACCEPTED state.
 */
public class AcceptedState implements FriendRequestState {

    @Override
    public String getStateName() {
        return "ACCEPTED";
    }

    @Override
    public FriendRequestState send(FriendRequest context) {
        // Cannot send a request that has already been accepted
        throw new IllegalStateException("Friend request has already been accepted");
    }

    @Override
    public FriendRequestState accept(FriendRequest context) {
        // Request is already accepted, no state change
        throw new IllegalStateException("Friend request has already been accepted");
    }

    @Override
    public FriendRequestState reject(FriendRequest context) {
        // Cannot reject a request that has already been accepted
        throw new IllegalStateException("Cannot reject an accepted friend request");
    }

    @Override
    public boolean isPending() {
        return false;
    }

    @Override
    public boolean isAccepted() {
        return true;
    }

    @Override
    public boolean isRejected() {
        return false;
    }
}