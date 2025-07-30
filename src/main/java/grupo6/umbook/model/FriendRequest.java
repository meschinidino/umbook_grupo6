package grupo6.umbook.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "friend_requests")
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    // For backward compatibility with existing code
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FriendRequestStatus legacyStatus = FriendRequestStatus.PENDING;

    @Transient // This field is not persisted directly
    private FriendRequestState state = new PendingState(); // Default state is PENDING

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // Enum for request status (kept for backward compatibility)
    public enum FriendRequestStatus {
        PENDING, ACCEPTED, REJECTED
    }

    // Constructors
    public FriendRequest() {
    }

    public FriendRequest(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    // State pattern methods
    public void setState(FriendRequestState state) {
        this.state = state;
        // Update the legacy status for backward compatibility
        if (state.isPending()) {
            this.legacyStatus = FriendRequestStatus.PENDING;
        } else if (state.isAccepted()) {
            this.legacyStatus = FriendRequestStatus.ACCEPTED;
        } else if (state.isRejected()) {
            this.legacyStatus = FriendRequestStatus.REJECTED;
        }
    }

    public FriendRequestState getState() {
        return state;
    }

    // State-specific actions
    public void send() {
        setState(state.send(this));
    }

    public void accept() {
        setState(state.accept(this));
    }

    public void reject() {
        setState(state.reject(this));
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    // For backward compatibility
    public FriendRequestStatus getStatus() {
        return legacyStatus;
    }

    // For backward compatibility
    public void setStatus(FriendRequestStatus status) {
        this.legacyStatus = status;
        this.updatedAt = LocalDateTime.now();
        
        // Update the state object to match the status
        if (status == FriendRequestStatus.PENDING) {
            this.state = new PendingState();
        } else if (status == FriendRequestStatus.ACCEPTED) {
            this.state = new AcceptedState();
        } else if (status == FriendRequestStatus.REJECTED) {
            this.state = new RejectedState();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendRequest that = (FriendRequest) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    // Helper methods for state checking
    public boolean isPending() {
        return state.isPending();
    }
    
    public boolean isAccepted() {
        return state.isAccepted();
    }
    
    public boolean isRejected() {
        return state.isRejected();
    }
}