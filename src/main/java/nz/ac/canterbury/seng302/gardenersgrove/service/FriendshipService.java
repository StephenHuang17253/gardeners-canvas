package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class for Friendship objects.
 */
@Service
public class FriendshipService {
    private UserService userService;
    /**
     * Interface for generic CRUD operations on a repository for Garden types.
     */
    private FriendshipRepository friendshipRepository;
    /**
     * FriendshipService constructor with repository.
     * @param friendshipRepository the repository for Gardens
     */
    @Autowired
    public FriendshipService(FriendshipRepository friendshipRepository, UserService userService) {
        this.friendshipRepository = friendshipRepository;
        this.userService = userService;
    }
    /**
     * Retrieves a Friendship by ID
     * @param id the Friendship's ID
     * @return the Friendship or Optional.empty() if none found
     */
    public Optional<Friendship> getFriendShipById(long id){
        return friendshipRepository.findById(id);
    }
    /**
     * Retrieves all Friendships from persistence where user1 or user2 id matches
     * @param id the user's ID
     * @throws IllegalArgumentException if the provided user ID is invalid
     */
    public List<Friendship> getAllUsersFriends(long id) {
        if (userService.getUserById(id) != null) {
            return friendshipRepository.findByUser1IdOrUser2Id(id, id);
        } else {
            throw new IllegalArgumentException("Invalid user ID: " + id);
        }
    }
    /**
     * Adds a new pending friendship entity to persistence
     * @param user1 the 1st half of the relation
     */
    public Friendship addFriendship(User user1, User user2) {
        if(user1.getId() == user2.getId()){
            throw new IllegalArgumentException("User cant have a friendship relation with themselves");
        }
        else if (user1.getId() == null || userService.getUserById(user1.getId()) == null) {
            throw new IllegalArgumentException("Invalid user 1 ID: " + user1.getId());
        }
        else if(user2.getId() == null || userService.getUserById(user2.getId()) == null) {
            throw new IllegalArgumentException("Invalid user 2 ID: " + user2.getId());
        }
        else{
            List<Friendship> existingFriendship1 = friendshipRepository.findByUser1IdOrUser2Id(user1.getId(), user2.getId());
            List<Friendship> existingFriendship2 = friendshipRepository.findByUser1IdOrUser2Id(user2.getId(), user1.getId());
            if(existingFriendship1.size()>0){
                Friendship existingFriendship = existingFriendship1.get(0);
                if(Objects.equals(existingFriendship.getUser1().getId(), user1.getId()) && Objects.equals(existingFriendship.getUser2().getId(), user2.getId())){
                    throw new IllegalArgumentException("Cant re-add a Friendship relation when one already exists");
                }

            }else if(existingFriendship2.size()>0){
                Friendship existingFriendship = existingFriendship2.get(0);
                if(existingFriendship.getStatus().equals(FriendshipStatus.DECLINED)){
                    //if user 1 declines user 2 friend request, user 1 can send one on their end
                    existingFriendship.setUser1(user1);
                    existingFriendship.setUser2(user2);
                    existingFriendship.setStatus(FriendshipStatus.PENDING);
                    return friendshipRepository.save(existingFriendship);
                }else if (Objects.equals(existingFriendship.getUser1().getId(), user2.getId()) && Objects.equals(existingFriendship.getUser2().getId(), user1.getId())){
                    throw new IllegalArgumentException("Cant re-add a Friendship relation when one already exists");
                }
            }
            Friendship friendship = new Friendship(user1,user2,FriendshipStatus.PENDING);
            return friendshipRepository.save(friendship);

        }
    }
    /**
     * Updates a friendship status of an existing friendship
     * @param id the id of the existing friend
     * @param friendshipStatus the status to update it to
     */
    public Friendship updateFriendShipStatus(Long id, FriendshipStatus friendshipStatus) {
        Optional<Friendship> optionalFriend = friendshipRepository.findById(id);
        if (optionalFriend.isEmpty()) {
            throw new IllegalArgumentException("Invalid Friendship ID: " + id);
        } else if(optionalFriend.get().getStatus()==FriendshipStatus.DECLINED){
            throw new IllegalArgumentException("Cant update a Friendship relation's status if its declined");
        }else{
            Friendship friendship = optionalFriend.get();
            friendship.setStatus(friendshipStatus);
            return friendshipRepository.save(friendship);
        }
    }
}
