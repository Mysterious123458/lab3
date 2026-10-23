package com.vladt.lab3.service;

import com.vladt.lab3.exception.BadRequestException;
import com.vladt.lab3.exception.NotFoundException;
import com.vladt.lab3.model.AppUser;
import com.vladt.lab3.repository.CityReportRepository;
import com.vladt.lab3.repository.UserRepository;
import com.vladt.lab3.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CityReportRepository cityReportRepository;
    private final WorkOrderRepository workOrderRepository;

    public UserService(UserRepository userRepository,
                       CityReportRepository cityReportRepository,
                       WorkOrderRepository workOrderRepository) {
        this.userRepository = userRepository;
        this.cityReportRepository = cityReportRepository;
        this.workOrderRepository = workOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<AppUser> findAll() {
        return userRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public AppUser findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public AppUser create(String fullName, String email) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BadRequestException("Пользователь с таким email уже есть");
        }
        AppUser user = new AppUser();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setCreatedAt(Instant.now());
        return userRepository.save(user);
    }

    public AppUser update(Long id, String fullName, String email) {
        AppUser current = findById(id);
        if (fullName != null && fullName.isBlank()) {
            throw new BadRequestException("Имя пользователя не может быть пустым");
        }
        if (email != null && email.isBlank()) {
            throw new BadRequestException("Email не может быть пустым");
        }
        String nextEmail = email == null ? current.getEmail() : normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(nextEmail, id)) {
            throw new BadRequestException("Пользователь с таким email уже есть");
        }
        if (fullName != null) {
            current.setFullName(fullName.trim());
        }
        current.setEmail(nextEmail);
        return current;
    }

    public void delete(Long id) {
        AppUser user = findById(id);
        cityReportRepository.findByAuthor_IdOrderByIdAsc(id).forEach(report ->
                workOrderRepository.deleteAll(workOrderRepository.findByCityReport_IdOrderByIdAsc(report.getId())));
        cityReportRepository.deleteAll(cityReportRepository.findByAuthor_IdOrderByIdAsc(id));
        userRepository.delete(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
