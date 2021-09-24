package com.saga.order.config;

import com.saga.commons.dto.OrderRequestDto;
import com.saga.commons.event.OrderStatus;
import com.saga.commons.event.PaymentStatus;
import com.saga.order.entity.PurchaseOrder;
import com.saga.order.repository.OrderRepository;
import com.saga.order.service.OrderStatusPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.transaction.Transactional;
import java.util.function.Consumer;

@Configuration
public class OrderStatusUpdateHandler {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderStatusPublisher orderStatusPublisher;

    @Transactional
    public void updateOrder(int id, Consumer<PurchaseOrder> purchaseOrderConsumer){
        orderRepository.findById(id).ifPresent(purchaseOrderConsumer.andThen(this::updateOrder));
    }

    private void updateOrder(PurchaseOrder purchaseOrder){
        boolean isPaymentComplete = PaymentStatus.PAYMENT_COMPLETED.equals(purchaseOrder.getPaymentStatus());
        OrderStatus orderStatus = isPaymentComplete ? OrderStatus.ORDER_COMPLETED : OrderStatus.ORDER_CANCELLED;
        purchaseOrder.setOrderStatus(orderStatus);
        if(!isPaymentComplete){
            orderStatusPublisher.publishOrderEvent(convertEntityToDto(purchaseOrder),orderStatus);
        }
    }


    public OrderRequestDto convertEntityToDto(PurchaseOrder purchaseOrder) {
        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setOrderId(purchaseOrder.getId());
        orderRequestDto.setUserId(purchaseOrder.getUserId());
        orderRequestDto.setAmount(purchaseOrder.getPrice());
        orderRequestDto.setProductId(purchaseOrder.getProductId());
        return orderRequestDto;
    }
}
