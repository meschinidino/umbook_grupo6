package grupo6.umbook.model;

/**
 * Interface for the State Design Pattern implementation for FriendRequest states.
 * Each concrete state will implement this interface to provide state-specific behavior.
 */
public interface FriendRequestState {
    
    /**
     * Get the name of the current state
     * @return The state name as a string
     */
    String getStateName();
    
    /**
     * Handle the action of sending a friend request
     * @param context The FriendRequest context
     * @return The resulting state after the action
     * @throws IllegalStateException if the action is not allowed in the current state
     */
    FriendRequestState send(FriendRequest context);
    
    /**
     * Handle the action of accepting a friend request
     * @param context The FriendRequest context
     * @return The resulting state after the action
     * @throws IllegalStateException if the action is not allowed in the current state
     */
    FriendRequestState accept(FriendRequest context);
    
    /**
     * Handle the action of rejecting a friend request
     * @param context The FriendRequest context
     * @return The resulting state after the action
     * @throws IllegalStateException if the action is not allowed in the current state
     */
    FriendRequestState reject(FriendRequest context);
    
    /**
     * Check if the request is in a pending state
     * @return true if the request is pending, false otherwise
     */
    boolean isPending();
    
    /**
     * Check if the request is in an accepted state
     * @return true if the request is accepted, false otherwise
     */
    boolean isAccepted();
    
    /**
     * Check if the request is in a rejected state
     * @return true if the request is rejected, false otherwise
     */
    boolean isRejected();
}