package pl.dudios.shopmvn.security.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.dudios.shopmvn.common.model.Review;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "users")
public class AppUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private boolean enabled;
    @ElementCollection
    @CollectionTable(name = "authorities", joinColumns = @JoinColumn(name = "username", referencedColumnName = "username"))
    @Column(name = "authority")
    @Enumerated(EnumType.STRING)
    private List<Role> authorities;
    private String image;
    private String hash;
    private LocalDateTime HashDate;
    @OneToMany
    @JoinColumn(name = "userId")
    private List<Review> reviews;

}
