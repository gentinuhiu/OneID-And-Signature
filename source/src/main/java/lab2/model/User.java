package lab2.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users") // Rename the table to "users"
@Getter
@Setter
public class User implements UserDetails{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;
    private String name;
    private String surname;
    private String ssn;
    private boolean nameVerified;
    private boolean surnameVerified;
    private boolean ssnVerified;
    @Lob
    @Column(name = "card_face", columnDefinition = "BLOB")
    private byte[] cardFace;
    @Lob
    @Column(name = "face", columnDefinition = "BLOB")
    private byte[] face;
    private boolean faceVerified;
    @Lob
    @Column(name = "card", columnDefinition = "LONGBLOB")
    private byte[] card;
    private boolean accountVerified;
    @Lob
    @Column(name = "public_key", columnDefinition = "BLOB")
    private String publicKey;

    public User(){}

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public byte[] getCardFace() {
        return cardFace;
    }

    public void setCardFace(byte[] cardFace) {
        this.cardFace = cardFace;
    }

    public Long getId() {
        return id;
    }

    public byte[] getCard() {
        return card;
    }

    public void setCard(byte[] card) {
        this.card = card;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public boolean isNameVerified() {
        return nameVerified;
    }

    public void setNameVerified(boolean nameVerified) {
        this.nameVerified = nameVerified;
    }

    public boolean isSurnameVerified() {
        return surnameVerified;
    }

    public void setSurnameVerified(boolean surnameVerified) {
        this.surnameVerified = surnameVerified;
    }

    public boolean isSsnVerified() {
        return ssnVerified;
    }

    public void setSsnVerified(boolean ssnVerified) {
        this.ssnVerified = ssnVerified;
    }

    public byte[] getFace() {
        return face;
    }

    public void setFace(byte[] face) {
        this.face = face;
    }

    public boolean isFaceVerified() {
        return faceVerified;
    }

    public void setFaceVerified(boolean faceVerified) {
        this.faceVerified = faceVerified;
    }

    public boolean isAccountVerified() {
        return accountVerified;
    }

    public void setAccountVerified(boolean accountVerified) {
        this.accountVerified = accountVerified;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

}
