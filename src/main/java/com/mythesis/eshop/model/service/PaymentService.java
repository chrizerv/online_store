package com.mythesis.eshop.model.service;

import com.mythesis.eshop.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Autowired
    private UserService userService;

    public void pay(Long userId, Double amount) {
        User user = userService.retrieveById(userId);

        if (user.getBalance() < amount) {
            throw new IllegalStateException("Not enough balance");
        }
        Double newBalance = user.getBalance() - amount;
        user.setBalance(newBalance);

        userService.update(user.getId(), user);
    }

}
