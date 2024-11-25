package kz.app.appstore.repository;

import kz.app.appstore.entity.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CatalogRepository extends JpaRepository<Catalog, Long> {
    void delete(Optional<Catalog> catalog);

    @Query("SELECT c FROM Catalog c WHERE c.parentCatalog = :catalog")
    List<Catalog> findByParentCatalog(@Param("catalog") Catalog catalog);
}