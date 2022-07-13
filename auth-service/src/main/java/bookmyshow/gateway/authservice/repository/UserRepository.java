package bookmyshow.gateway.authservice.repository;

import bookmyshow.gateway.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select us from User us where us.userUid  =   :userUid " + "AND us.userStatus = 1 " + "AND us.oauthClient = :oauthClient ")
    List<User> findActiveUserList(@Param("userUid") Long userUid, @Param("oauthClient") String oauthClient);


}
