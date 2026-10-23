package com.vladt.lab3.repository;

import com.vladt.lab3.model.CityObject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityObjectRepository extends JpaRepository<CityObject, Long> {

    List<CityObject> findAllByOrderByIdAsc();
}
