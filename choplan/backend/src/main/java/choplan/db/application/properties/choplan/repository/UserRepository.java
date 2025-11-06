package choplan.db.application.properties.choplan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import choplan.db.application.properties.choplan.entity.CustomerStatus;
import choplan.db.application.properties.choplan.entity.OwnerStatus;
import choplan.db.application.properties.choplan.entity.UserRole;
import choplan.db.application.properties.choplan.entity.Users;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    List<Users> findAllByRole(UserRole role);
    List<Users> findAllByRoleAndCustomerStatus(UserRole role, CustomerStatus status);
    List<Users> findAllByRoleAndOwnerStatus(UserRole role, OwnerStatus status);
}
