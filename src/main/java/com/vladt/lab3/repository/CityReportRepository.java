package com.vladt.lab3.repository;

import com.vladt.lab3.model.CityReport;
import com.vladt.lab3.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CityReportRepository extends JpaRepository<CityReport, Long> {

    List<CityReport> findAllByOrderByIdAsc();

    List<CityReport> findByCityObject_IdOrderByIdAsc(Long cityObjectId);

    List<CityReport> findByAuthor_IdOrderByIdAsc(Long authorId);

    List<CityReport> findByCityObject_IdAndStatusInOrderByIdAsc(Long cityObjectId, Collection<ReportStatus> statuses);
}
