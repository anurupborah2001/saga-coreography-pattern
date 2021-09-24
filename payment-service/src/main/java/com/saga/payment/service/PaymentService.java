package com.saga.payment.service;

import com.saga.commons.dto.OrderRequestDto;
import com.saga.commons.dto.PaymentRequestDto;
import com.saga.commons.event.OrderEvent;
import com.saga.commons.event.PaymentEvent;
import com.saga.commons.event.PaymentStatus;
import com.saga.payment.entity.UserBalance;
import com.saga.payment.entity.UserTransaction;
import com.saga.payment.repository.UserBalanceRepository;
import com.saga.payment.repository.UserTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PaymentService {

    @Autowired
    private UserBalanceRepository userBalanceRepository;
    @Autowired
    private UserTransactionRepository userTransactionRepository;

    @PostConstruct
    public void initUserBalanceInDB() {
        userBalanceRepository.saveAll(Stream.of(new UserBalance(101, 5000),
                new UserBalance(102, 3000),
                new UserBalance(103, 4200),
                new UserBalance(104, 20000),
                new UserBalance(105, 999)).collect(Collectors.toList()));
    }

    @Transactional
    public PaymentEvent newOrderEvent(OrderEvent orderEvent) {
        OrderRequestDto orderRequestDto = orderEvent.getOrderRequestDto();
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(
                orderRequestDto.getOrderId(),
                orderRequestDto.getUserId(),
                orderRequestDto.getAmount()
        );
        return userBalanceRepository.findById(orderRequestDto.getUserId())
                .filter(userBalance -> userBalance.getAmount() > orderRequestDto.getAmount())
                .map(userBalance -> {
                    userBalance.setAmount(
                            userBalance.getAmount() - orderRequestDto.getAmount()
                    );
                    userTransactionRepository.save(new UserTransaction(
                            orderRequestDto.getOrderId(),
                            orderRequestDto.getUserId(),
                            orderRequestDto.getAmount()
                    ));
                    return new PaymentEvent(paymentRequestDto, PaymentStatus.PAYMENT_COMPLETED);
                }).orElse(new PaymentEvent(paymentRequestDto, PaymentStatus.PAYMENT_FAILED));
    }

    @Transactional
    public void cancelOrderEvent(OrderEvent orderEvent) {
        userTransactionRepository.findById(orderEvent.getOrderRequestDto().getOrderId())
                .ifPresent(userTransaction -> {
                    userTransactionRepository.delete(userTransaction);
                    userTransactionRepository.findById(userTransaction.getUserId())
                            .ifPresent(userBalance -> {
                                userBalance.setAmount(userBalance.getAmount() + userTransaction.getAmount());
                            });
                });
    }
}
