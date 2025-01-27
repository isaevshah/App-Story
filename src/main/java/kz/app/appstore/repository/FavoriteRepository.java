package kz.app.appstore.repository;

import kz.app.appstore.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId")
    List<Favorite> findAllByUserId(@Param("userId") Long userId);
}
