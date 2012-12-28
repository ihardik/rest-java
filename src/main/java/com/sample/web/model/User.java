package com.sample.web.model;

import com.sample.web.api.ExternalUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.security.Principal;
import java.util.*;


/**
* Created by IntelliJ IDEA.
* User: porter
* Date: 09/03/2012
* Time: 18:56
* To change this template use File | Settings | File Templates.
*/
@Entity
@Table(name="loyalty_user")
public class User extends BaseEntity implements Principal {

     /**
     * Add additional salt to password hashing
     */
    private static final String HASH_SALT = "d8a8e885-ecce-42bb-8332-894f20f0d8ed";

    private String firstName;
    private String lastName;
    private String emailAddress;
    private String hashedPassword;
    private String sessionToken;
    private boolean isVerified;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(cascade={CascadeType.ALL}, mappedBy="user", fetch=FetchType.EAGER)
    private Set<SocialUser> socialUsers = new HashSet<SocialUser>();

    @OneToMany(mappedBy="user",
                 targetEntity=VerificationToken.class,
                 cascade= CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<VerificationToken> verificationTokens = new ArrayList<VerificationToken>();

    public User() {
        this(UUID.randomUUID());
    }

    public User(UUID uuid) {
        super(uuid);
        setRole(Role.anonymous); //all users are anonymous until credentials are proved
    }

    public User(ExternalUser externalUser) {
        this();
        this.firstName = externalUser.getFirstName();
        this.lastName = externalUser.getLastName();
        this.emailAddress = externalUser.getEmailAddress();
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getHashedPassword() {
        return this.hashedPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean hasRole(Role role) {
        return role.equals(this.role);
    }


    public boolean equals(Object otherUser) {
        boolean response = false;

        if(otherUser == null) {
            response = false;
        }
        else if(! (otherUser instanceof User)) {
            response = false;
        }
        else {
            if(((User)otherUser).getUuid().equals(this.getUuid())) {
                response = true;
            }
        }

        return response;
    }

    public int hashCode() {
        return getUuid().hashCode();
    }

    public String getName() {
        if(StringUtils.hasText(getFirstName())) {
           return getFirstName() + " " + getLastName();
        }
        return "";
    }

	public Set<SocialUser> getSocialUsers() {
		return socialUsers;
	}


	public void setSocialUsers(Set<SocialUser> socialUsers) {
		this.socialUsers = socialUsers;
	}

    public void addSocialUser(SocialUser socialUser) {
        getSocialUsers().add(socialUser);
    }

    public synchronized void addVerificationToken(VerificationToken token) {
        verificationTokens.add(token);
    }

    public synchronized List<VerificationToken> getVerificationTokens() {
        return Collections.unmodifiableList(this.verificationTokens);
    }

    /**
     * If the user has a VerificationToken of type VerificationTokenType.lostPassword
     * that is active return it otherwise return null
     *
     * @return verificationToken
     */
    public VerificationToken getActiveLostPasswordToken() {
        return getActiveToken(VerificationToken.VerificationTokenType.lostPassword);
    }

    /**
     * If the user has a VerificationToken of type VerificationTokenType.emailVerification
     * that is active return it otherwise return null
     *
     * @return verificationToken
     */
    public VerificationToken getActiveEmailVerificationToken() {
        return getActiveToken(VerificationToken.VerificationTokenType.emailVerification);
    }

    /**
     * If the user has a VerificationToken of type VerificationTokenType.emailRegistration
     * that is active return it otherwise return null
     *
     * @return verificationToken
     */
    public VerificationToken getActiveEmailRegistrationToken() {
        return getActiveToken(VerificationToken.VerificationTokenType.emailRegistration);
    }

    private VerificationToken getActiveToken(VerificationToken.VerificationTokenType tokenType) {
         VerificationToken activeToken = null;
        for (VerificationToken token : getVerificationTokens()) {
            if (token.getTokenType().equals(tokenType)
                    && !token.hasExpired() && !token.isVerified()) {
                activeToken = token;
                break;
            }
        }
        return activeToken;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    /**
     * Hash the password using salt values
     *
     * @param passwordToHash
     * @return hashed password
     */
    public String hashPassword(String passwordToHash) {
        return hashToken(passwordToHash, getUuid().toString() + HASH_SALT );
    }


    private String hashToken(String token, String salt) {
        return DigestUtils.sha256Hex(token + salt);
    }

}
