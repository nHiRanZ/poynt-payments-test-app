package io.applova.poyntpaymentstestapp.async;

import androidx.annotation.Nullable;

public interface AsyncCompleteCallback {
    void onDataLoaded(Object result);
    void onError(Exception e);
    void onComplete(@Nullable Object result);
}
