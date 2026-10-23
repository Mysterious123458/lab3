package com.vladt.lab3.service;

import com.vladt.lab3.exception.BadRequestException;
import com.vladt.lab3.exception.NotFoundException;
import com.vladt.lab3.model.AppUser;
import com.vladt.lab3.model.CityObject;
import com.vladt.lab3.model.CityReport;
import com.vladt.lab3.model.ReportPriority;
import com.vladt.lab3.model.ReportStatus;
import com.vladt.lab3.repository.CityObjectRepository;
import com.vladt.lab3.repository.CityReportRepository;
import com.vladt.lab3.repository.UserRepository;
import com.vladt.lab3.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class CityReportService {

    private final CityReportRepository cityReportRepository;
    private final CityObjectRepository cityObjectRepository;
    private final UserRepository userRepository;
    private final WorkOrderRepository workOrderRepository;

    public CityReportService(CityReportRepository cityReportRepository,
                             CityObjectRepository cityObjectRepository,
                             UserRepository userRepository,
                             WorkOrderRepository workOrderRepository) {
        this.cityReportRepository = cityReportRepository;
        this.cityObjectRepository = cityObjectRepository;
        this.userRepository = userRepository;
        this.workOrderRepository = workOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<CityReport> findAll() {
        return cityReportRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public CityReport findById(Long id) {
        return cityReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Обращение не найдено"));
    }

    public CityReport create(Long cityObjectId, Long authorId, String description, ReportStatus status, ReportPriority priority) {
        Instant now = Instant.now();
        CityReport report = new CityReport();
        report.setCityObject(requireObject(cityObjectId));
        report.setAuthor(requireAuthor(authorId));
        report.setDescription(description.trim());
        report.setStatus(status == null ? ReportStatus.NEW : status);
        report.setPriority(priority == null ? ReportPriority.NORMAL : priority);
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        rejectTerminalStatus(report.getStatus());
        return cityReportRepository.save(report);
    }

    public CityReport update(Long id, Long cityObjectId, Long authorId, String description, ReportStatus status, ReportPriority priority) {
        CityReport current = findById(id);
        if (description != null && description.isBlank()) {
            throw new BadRequestException("Описание проблемы не может быть пустым");
        }
        if (cityObjectId != null) {
            current.setCityObject(requireObject(cityObjectId));
        }
        if (authorId != null) {
            current.setAuthor(requireAuthor(authorId));
        }
        if (description != null) {
            current.setDescription(description.trim());
        }
        if (status != null) {
            rejectTerminalStatus(status);
            current.setStatus(status);
        }
        if (priority != null) {
            current.setPriority(priority);
        }
        current.setUpdatedAt(Instant.now());
        return current;
    }

    public void delete(Long id) {
        CityReport report = findById(id);
        workOrderRepository.deleteAll(workOrderRepository.findByCityReport_IdOrderByIdAsc(id));
        cityReportRepository.delete(report);
    }

    private void rejectTerminalStatus(ReportStatus status) {
        if (status == ReportStatus.RESOLVED || status == ReportStatus.REJECTED) {
            throw new BadRequestException("Закрыть обращение можно только бизнес-операцией завершения или отклонения");
        }
    }

    private CityObject requireObject(Long id) {
        return cityObjectRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Городской объект не найден"));
    }

    private AppUser requireAuthor(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Автор обращения не найден"));
    }
}
