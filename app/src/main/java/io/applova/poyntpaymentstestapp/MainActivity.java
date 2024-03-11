package io.applova.poyntpaymentstestapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.godaddy.payments.sdk.PaymentsSdk;
import com.godaddy.payments.sdk.common.cancellable.Cancellable;
import com.godaddy.payments.sdk.device.PosDevice;
import com.godaddy.payments.sdk.device.listeners.DeviceConnectionListener;
import com.godaddy.payments.sdk.device.listeners.DeviceUpdateListener;
import com.godaddy.payments.sdk.device.listeners.ScanResult;
import com.godaddy.payments.sdk.models.Credentials;
import com.godaddy.payments.sdk.models.InitListener;
import com.godaddy.payments.sdk.models.PoskitError;
import com.godaddy.payments.sdk.models.payment.PaymentMethod;
import com.godaddy.payments.sdk.models.payment.PaymentRequest;
import com.godaddy.payments.sdk.models.payment.TransactionEvent;
import com.godaddy.payments.sdk.models.payment.TransactionResult;
import com.godaddy.payments.sdk.payment.listeners.TransactionStatusListener;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.List;

import io.applova.poyntpaymentstestapp.adapter.ItemAdapter;
import io.applova.poyntpaymentstestapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'poyntpaymentstestapp' library on application startup.
    static {
        System.loadLibrary("poyntpaymentstestapp");
    }

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private String CLIENT_ID = "7efb9f59-d401-4952-a37e-972d2e6d164c";
    private String CLIENT_SECRET = "sQLNpqUC9hmm93W1OqvP7SU8XBUQkCNN";
    private String BUSINESS_ID = "8765ac99-8f42-48e3-85c6-a9eafc6e4cb9";
    private String STORE_ID = "d6e76a2c-f5ca-430a-b3d0-5e207c98bb2c";
    private PaymentsSdk paymentsSdk;
    private PoskitError poskitError;
    private Context context;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private Dialog dialog;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loadingView.setVisibility(View.VISIBLE);
        binding.errorView.setVisibility(View.GONE);
        binding.paymentsView.setVisibility(View.GONE);

        launchConfigsDialog(true);
    }

    private void initPoyntSDK() {
        Credentials credentials = new Credentials.ClientIdSecret(CLIENT_ID, CLIENT_SECRET);
        PaymentsSdk.init(credentials, BUSINESS_ID, STORE_ID, new InitListener() {
            @Override
            public void onError(@NonNull PoskitError error) {
                runOnUiThread(() -> setPoskitError(error));
            }

            @Override
            public void onSuccess(@NonNull PaymentsSdk paymentsSdk) {
                runOnUiThread(() -> setPaymentsSdk(paymentsSdk));
            }
        });
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            initPoyntSDK();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initPoyntSDK();
            } else {
                Toast.makeText(this, "Location permission is required to scan for bluetooth devices", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setPaymentsSdk(PaymentsSdk paymentsSdk) {
        this.paymentsSdk = paymentsSdk;
        if (paymentsSdk.deviceInterface().isDevicePreviouslyConnected()) {
            Toast.makeText(context, "Previously connected device found. Attempting to reconnect.", Toast.LENGTH_LONG).show();
            paymentsSdk.deviceInterface().connectLastKnownDevice(new DeviceConnectionListener() {
                @Override
                public void onConnected(@NonNull PosDevice posDevice) {
                    Toast.makeText(context, "Connected to " + posDevice.name(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onDisconnected() {
                    Toast.makeText(context, "Disconnected", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(@NonNull PoskitError poskitError) {
                    Toast.makeText(context, "Error: " + poskitError.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "onError: ", poskitError.getThrowable());
                }

                @Override
                public void onUsbConnected(@NonNull PosDevice posDevice) {
                    Toast.makeText(context, "USB Connected", Toast.LENGTH_LONG).show();
                }
            });
        }

        binding.loadingView.setVisibility(View.GONE);
        binding.errorView.setVisibility(View.GONE);
        binding.paymentsView.setVisibility(View.VISIBLE);

        setButtonActions();
    }

    public void setPoskitError(PoskitError poskitError) {
        this.poskitError = poskitError;

        binding.loadingView.setVisibility(View.GONE);
        binding.errorView.setVisibility(View.VISIBLE);
        binding.paymentsView.setVisibility(View.GONE);

        binding.errorMessage.setText(poskitError.getMessage());
    }

    public void showDialogWithRecycleViewLoading(Context context) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_layout);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            windowParams.copyFrom(dialog.getWindow().getAttributes());
            windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(windowParams);
        }
        ImageButton closeBtn = dialog.findViewById(R.id.closeButton);
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void showDialogWithRecycleView(Context context, List<? extends PosDevice> items, boolean isLoaded) {
        if (dialog == null) {
            dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_layout);
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
                windowParams.copyFrom(dialog.getWindow().getAttributes());
                windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(windowParams);
            }
        }

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new ItemAdapter(items, this::onPosDeviceSelected));

        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        if (!isLoaded) {
            dialog.show();
        }
    }

    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void onPosDeviceSelected(PosDevice posDevice) {
        dialog.dismiss();
        paymentsSdk.deviceInterface().connect(posDevice, new DeviceConnectionListener() {
            @Override
            public void onConnected(@NonNull PosDevice posDevice) {
                Toast.makeText(context, "Connected to " + posDevice.name(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDisconnected() {
                Toast.makeText(context, "Disconnected", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(@NonNull PoskitError poskitError) {
                Toast.makeText(context, "Error: " + poskitError.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "onError: ", poskitError.getThrowable());
            }

            @Override
            public void onUsbConnected(@NonNull PosDevice posDevice) {
                Toast.makeText(context, "USB Connected", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void launchConfigsDialog(boolean isInitialLaunch) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_input_layout);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
            windowParams.copyFrom(dialog.getWindow().getAttributes());
            windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(windowParams);
        }
        EditText businessIdText = dialog.findViewById(R.id.businessIdText);
        EditText storeIdText = dialog.findViewById(R.id.storeIdText);
        EditText clientIdText = dialog.findViewById(R.id.clientIdText);
        EditText clientSecretText = dialog.findViewById(R.id.clientSecretText);
        Button saveConfigsBtn = dialog.findViewById(R.id.saveConfigsBtn);
        ImageButton closeButton = dialog.findViewById(R.id.closeButton);

        businessIdText.setText(BUSINESS_ID);
        storeIdText.setText(STORE_ID);
        clientIdText.setText(CLIENT_ID);
        clientSecretText.setText(CLIENT_SECRET);

        saveConfigsBtn.setOnClickListener(v -> {
            BUSINESS_ID = businessIdText.getText().toString();
            STORE_ID = storeIdText.getText().toString();
            CLIENT_ID = clientIdText.getText().toString();
            CLIENT_SECRET = clientSecretText.getText().toString();
            paymentsSdk = null;
            requestLocationPermission();
            dialog.dismiss();
        });
        if (isInitialLaunch) {
            closeButton.setVisibility(View.GONE);
            saveConfigsBtn.setText("Save Configs and Start SDK");
        } else {
            closeButton.setVisibility(View.VISIBLE);
        }
        closeButton.setOnClickListener(v1 -> dialog.dismiss());
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void hideButtonsOnPay(boolean isPaying) {
        if (isPaying) {
            binding.paymentsView.setVisibility(View.GONE);
        } else {
            binding.paymentsView.setVisibility(View.VISIBLE);
        }
    }

    private void setButtonActions() {
        binding.configsBtn.setOnClickListener(v -> launchConfigsDialog(false));

        paymentsSdk.deviceInterface().registerDeviceConnectionListener(new DeviceConnectionListener() {
            @Override
            public void onConnected(@NonNull PosDevice posDevice) {
                binding.deviceName.setText(posDevice.name());
                binding.deviceChargingStatus.setText(posDevice.isCharging() ? "Charging" : "Not Charging");
                binding.deviceBattery.setText(posDevice.batteryPercentage() + "%");
                binding.deviceSerial.setText(posDevice.serialNumber());
                binding.connectedDeviceDetailsWrapper.setVisibility(View.VISIBLE);
                binding.deviceConnectionBtn.setVisibility(View.GONE);
            }

            @Override
            public void onDisconnected() {
                binding.connectedDeviceDetailsWrapper.setVisibility(View.GONE);
                binding.deviceConnectionBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(@NonNull PoskitError poskitError) {
                binding.connectedDeviceDetailsWrapper.setVisibility(View.GONE);
                binding.deviceConnectionBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onUsbConnected(@NonNull PosDevice posDevice) {
                binding.connectedDeviceDetailsWrapper.setVisibility(View.GONE);
                binding.deviceConnectionBtn.setVisibility(View.VISIBLE);
            }
        });

        binding.deviceConnectionBtn.setOnClickListener(v -> paymentsSdk.deviceInterface().launchDeviceConnection());

        binding.payBtn.setOnClickListener(view -> {
            hideButtonsOnPay(true);
            String amount = binding.amountText.getText().toString();
            String tipAmount = binding.tipAmountText.getText().toString();

//            Payment payment = new Payment();
//            payment.setAmount(new BigDecimal(amount).longValue());
//            payment.setCurrency("USD");
//            payment.setCaptureWithCard(true);
//            payment.setDisableCash(true);
//            payment.setDisableTip(true);
//            payment.setSkipReceiptScreen(true);
//            payment.setSkipSignatureScreen(true);
//            payment.setManualEntry(true);

            long paymentAmount = new BigDecimal(amount).longValue();
            long paymentTipAmount = new BigDecimal(0).longValue();
            if (!tipAmount.isEmpty()) {
                paymentTipAmount = new BigDecimal(tipAmount).longValue();
            }
            PaymentRequest paymentRequest = new PaymentRequest(paymentAmount, paymentTipAmount, PaymentMethod.LCR);
            paymentsSdk.transactionInterface().processTransaction(paymentRequest, new TransactionStatusListener() {
                @Override
                public void onResult(@NonNull TransactionResult transactionResult) {
                    Toast.makeText(context, "Transaction Success!", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "onResult: " + transactionResult.toString());
                    Log.i(TAG, "onResult: " + gson.toJson(transactionResult));
                    hideButtonsOnPay(false);
                }

                @Override
                public void onEvent(@NonNull TransactionEvent transactionEvent) {

                }

                @Override
                public void onError(@NonNull PoskitError poskitError) {
                    Toast.makeText(context, "Transaction Error: " + poskitError.getMessage(), Toast.LENGTH_LONG).show();
                    hideButtonsOnPay(false);
                }

                @Override
                public void onCancel() {
                    Toast.makeText(context, "Transaction Cancelled", Toast.LENGTH_LONG).show();
                    hideButtonsOnPay(false);
                }
            });
        });
    }
}