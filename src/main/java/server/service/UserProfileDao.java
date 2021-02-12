package server.service;

import common.dto.UserProfileStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import platform.domain.IUser;
import platform.service.UserProfileRegistry;
import server.domain.BackpackItem;
import server.domain.InventoryItem;
import server.domain.UserProfile;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserProfileDao implements UserProfileRegistry {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("#{newUserProfileConfig}")
    private UserProfileStructure newUserProfileConfig;

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public IUser createNewUserProfile(String uid) {
        Integer nextUserProfileId = namedParameterJdbcTemplate.queryForObject("select NEXT VALUE FOR user_profile_sequence", Collections.emptyMap(), Integer.class);

        namedParameterJdbcTemplate.update(
                "insert into uid_profile (uid, profile_id) values (:uid, :profile_id)",
                Map.of(
                        "uid", uid,
                        "profile_id", nextUserProfileId
                )
        );
        namedParameterJdbcTemplate.update(
                "insert into user_profile (" +
                        "id, name, level, experience, energy," +
                        " rating, money, backpack, inventory, friends) " +
                        "values (:profile_id, :name, :level, :experience, :energy," +
                        " :rating, :money, '', '', '')",
                Map.of(
                        "profile_id", nextUserProfileId,
                        "name", newUserProfileConfig.name,
                        "level", newUserProfileConfig.level,
                        "experience", newUserProfileConfig.experience,
                        "energy", newUserProfileConfig.energy,
                        "rating", newUserProfileConfig.rating,
                        "money", newUserProfileConfig.money
                )
        );

        log.info("Create new UserProfile '{}' => {}", uid, nextUserProfileId);
        return selectUserProfile(nextUserProfileId);
    }

    @Override
    public IUser findUserProfileByUid(String uid) {
        try {
            return namedParameterJdbcTemplate.queryForObject(""" 
                    select user_profile.* from user_profile  
                    inner join uid_profile on id = profile_id 
                    where uid = :uid 
                    """, Map.of(
                    "uid", uid
            ), UserProfileDao::mapRow);
        } catch (EmptyResultDataAccessException ignored) {
        }
        return null;
    }

    @Override
    public UserProfile selectUserProfile(int profileId) {
        return namedParameterJdbcTemplate.queryForObject("select * from user_profile where id = :profileId",
                Map.of("profileId", profileId),
                UserProfileDao::mapRow);
    }

    private static UserProfile mapRow(ResultSet res, int i) throws SQLException {
        return new UserProfile(
                res.getInt("id"),
                res.getString("name"),
                res.getInt("level"),
                res.getInt("experience"),
                res.getInt("energy"),
                res.getInt("rating"),
                res.getInt("money"),
                Arrays.stream(res.getString("backpack").split(",")).filter(s -> !s.isBlank()).map(BackpackItem::new).collect(Collectors.toList()),
                Arrays.stream(res.getString("inventory").split(",")).filter(s -> !s.isBlank()).map(InventoryItem::new).collect(Collectors.toList()),
                Arrays.stream(res.getString("friends").split(",")).filter(s -> !s.isBlank()).map(Integer::valueOf).collect(Collectors.toSet())
        );
    }

    @Override
    public void updateUserProfile(IUser user) {
        UserProfile userProfile = (UserProfile) user;
        namedParameterJdbcTemplate.update("""
                  update user_profile set
                    name = :name,
                    level = :level,
                    experience = :experience,
                    energy = :energy,
                    rating = :rating,
                    money = :money,
                    backpack = :backpack,
                    inventory = :inventory,
                    friends = :friends
                  where id = :profileId
                """, Map.of(
                "profileId", userProfile.id,
                "name", userProfile.getName(),
                "level", userProfile.getLevel(),
                "experience", userProfile.getExperience(),
                "energy", userProfile.getEnergy(),
                "rating", userProfile.getRating(),
                "money", userProfile.getMoney(),
                "backpack", userProfile.getBackpack().stream().map(BackpackItem::encodeAsString).collect(Collectors.joining(",")),
                "inventory", userProfile.getInventory().stream().map(InventoryItem::encodeAsString).collect(Collectors.joining(",")),
                "friends", userProfile.getFriends().stream().map(String::valueOf).collect(Collectors.joining(","))
        ));
    }

}
