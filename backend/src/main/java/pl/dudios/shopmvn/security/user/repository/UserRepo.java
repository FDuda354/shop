package pl.dudios.shopmvn.security.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.dudios.shopmvn.security.user.model.AppUser;

import java.util.Optional;

public interface UserRepo extends JpaRepository<AppUser, Long> {
    boolean existsByUsername(String username);

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByHash(String hash);
}
