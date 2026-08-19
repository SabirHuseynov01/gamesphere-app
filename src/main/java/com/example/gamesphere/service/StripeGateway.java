package com.example.gamesphere.service;

import com.example.gamesphere.config.StripeProperties;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.User;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.LineItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class StripeGateway {

    private final StripeProperties stripeProperties;

    public Session createCheckoutSession(Order order, User user) throws StripeException {
        long amountInMinorUnit = order.getTotalAmount()
                .movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();

        LineItem.PriceData.ProductData productData =
                LineItem.PriceData.ProductData.builder()
                        .setName("Gamesphere order " + order.getOrderNumber())
                        .build();

        LineItem.PriceData priceData = LineItem.PriceData.builder()
                .setCurrency(stripeProperties.getCurrency().toLowerCase())
                .setUnitAmount(amountInMinorUnit)
                .setProductData(productData)
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeProperties.getSuccessUrl())
                .setCancelUrl(stripeProperties.getCancelUrl())
                .setCustomerEmail(user.getEmail())
                .setClientReferenceId(order.getOrderNumber())
                .putMetadata("orderId", order.getId().toString())
                .putMetadata("userId", user.getId().toString())
                .addLineItem(LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(priceData)
                        .build())
                .build();

        StripeClient client = new StripeClient(stripeProperties.getSecretKey());
        return client.v1().checkout().sessions().create(params);
    }
}

