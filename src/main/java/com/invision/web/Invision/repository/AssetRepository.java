package com.invision.web.Invision.repository;

import com.invision.web.Invision.model.Asset;
import com.invision.web.Invision.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset,Long> {

    List<Asset> findByCategory(Category category);

}
