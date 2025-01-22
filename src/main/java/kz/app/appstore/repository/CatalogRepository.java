package kz.app.appstore.repository;

import kz.app.appstore.entity.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CatalogRepository extends JpaRepository<Catalog, Long> {
    @Query("SELECT c FROM Catalog c WHERE c.parentCatalog.id = :parentCatalogId")
    List<Catalog> findByParentCatalogId(@Param("parentCatalogId") Long parentCatalogId);

}