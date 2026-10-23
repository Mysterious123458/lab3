package com.vladt.lab3.service;

import com.vladt.lab3.exception.BadRequestException;
import com.vladt.lab3.model.ObjectType;
import com.vladt.lab3.model.OrderStatus;
import com.vladt.lab3.model.WorkCrew;
import com.vladt.lab3.repository.WorkOrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrewLoadPolicy {

    public static final List<OrderStatus> ACTIVE = List.of(OrderStatus.ASSIGNED, OrderStatus.IN_PROGRESS);

    private final WorkOrderRepository workOrderRepository;

    public CrewLoadPolicy(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    public void ensureCanTake(WorkCrew crew, ObjectType objectType, OrderStatus status, Long ignoreOrderId) {
        if (!crew.getSupportedTypes().contains(objectType)) {
            throw new BadRequestException("Бригада не работает с типом объекта " + objectType);
        }
        if (!status.isActive()) {
            return;
        }
        long activeOrders = workOrderRepository.findByWorkCrew_IdOrderByIdAsc(crew.getId()).stream()
                .filter(order -> ignoreOrderId == null || !ignoreOrderId.equals(order.getId()))
                .filter(order -> order.getStatus().isActive())
                .count();
        if (activeOrders >= crew.getActiveWorkLimit()) {
            throw new BadRequestException("У бригады исчерпан лимит активных нарядов");
        }
    }
}
