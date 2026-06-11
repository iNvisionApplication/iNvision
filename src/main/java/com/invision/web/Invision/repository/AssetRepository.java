package com.invision.web.Invision.repository;

import com.invision.web.Invision.model.Asset;
import com.invision.web.Invision.enums.Category;
import com.invision.web.Invision.enums.AssetStatus;
import com.invision.web.Invision.enums.Condition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    @Query("SELECT a FROM Asset a WHERE " +
            "(:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:category IS NULL OR a.category = :category) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:location IS NULL OR LOWER(a.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:condition IS NULL OR a.condition = :condition)")
    List<Asset> searchAndFilterAssets(@Param("title") String title,
                                      @Param("category") Category category,
                                      @Param("status") AssetStatus status,
                                      @Param("location") String location,
                                      @Param("condition") Condition condition);
}