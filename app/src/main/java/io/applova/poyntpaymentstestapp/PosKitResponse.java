package io.applova.poyntpaymentstestapp;

import com.godaddy.payments.sdk.PaymentsSdk;
import com.godaddy.payments.sdk.models.PoskitError;

public class PosKitResponse {
    private PaymentsSdk paymentsSdk;
    private PoskitError poskitError;

    public PosKitResponse(PaymentsSdk paymentsSdk, PoskitError poskitError) {
        this.paymentsSdk = paymentsSdk;
        this.poskitError = poskitError;
    }

    public PaymentsSdk getPaymentsSdk() {
        return paymentsSdk;
    }

    public void setPaymentsSdk(PaymentsSdk paymentsSdk) {
        this.paymentsSdk = paymentsSdk;
    }

    public PoskitError getPoskitError() {
        return poskitError;
    }

    public void setPoskitError(PoskitError poskitError) {
        this.poskitError = poskitError;
    }
}
