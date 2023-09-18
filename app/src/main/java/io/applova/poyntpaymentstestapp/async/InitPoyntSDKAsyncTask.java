package io.applova.poyntpaymentstestapp.async;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.godaddy.payments.sdk.PaymentsSdk;
import com.godaddy.payments.sdk.models.Credentials;
import com.godaddy.payments.sdk.models.InitListener;
import com.godaddy.payments.sdk.models.PoskitError;

import io.applova.poyntpaymentstestapp.PosKitResponse;

public class InitPoyntSDKAsyncTask extends AsyncTask<String, Context, PosKitResponse> {

    private final Credentials credentials;
    private final AsyncCompleteCallback mCallback;
    private final String businessId;
    private final String storeId;
    private Exception mException;

    public InitPoyntSDKAsyncTask(Credentials credentials,
                                 String businessId,
                                 String storeId,
                                 AsyncCompleteCallback callback) {
        this.credentials = credentials;
        this.businessId = businessId;
        this.storeId = storeId;
        this.mCallback = callback;
    }


    @Override
    protected PosKitResponse doInBackground(String... params) {
        final PoskitError[] poskitError = new PoskitError[1];
        final PaymentsSdk[] paymentsSdkResult = new PaymentsSdk[1];
        PaymentsSdk.init(credentials, businessId, storeId, new InitListener() {
            @Override
            public void onError(@NonNull PoskitError error) {
                poskitError[0] = error;
            }

            @Override
            public void onSuccess(@NonNull PaymentsSdk paymentsSdk) {
                paymentsSdkResult[0] = paymentsSdk;
            }

        });
        return new PosKitResponse(paymentsSdkResult[0], poskitError[0]);
    }

    @Override
    protected void onPostExecute(PosKitResponse response) {
        super.onPostExecute(response);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded(response);
        }
        mCallback.onComplete(response);
    }
}
