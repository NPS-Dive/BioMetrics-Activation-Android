package com.example.biometrics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Button biometricBTN;
    private Executor executor;
    private BiometricPrompt prompt;
    private BiometricPrompt.PromptInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        BiometricManager biometricManager = BiometricManager.from(this);

        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                executor = ContextCompat.getMainExecutor(this);
                prompt = new BiometricPrompt(MainActivity.this, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (errorCode == 11) { //hardware is found, but user has not set her/his biometrics
                            sendUserToSettings();
                        }
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Log.i(TAG, "onAuthenticationSucceeded: ");
                        Toast.makeText(MainActivity.this, "Succeeded!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        //hardware is working, biometrics were set by user, but inputs are not right!
                        Log.i(TAG, "onAuthenticationFailed: ");
                        Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
                info = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Biometric Authentication")
                        .setSubtitle("Login with your Biometric data")
                        .setNegativeButtonText("Use Password")
                        .build();

                biometricBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prompt.authenticate(info);
                    }
                });
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.i(TAG, "onCreate: BIOMETRIC_ERROR_NO_HARDWARE");
                Toast.makeText(MainActivity.this, "there is no biometric hardware!", Toast.LENGTH_SHORT).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                //there is biometric hardware, but not functioning
                Log.i(TAG, "onCreate: BIOMETRIC_ERROR_HW_UNAVAILABLE");
                Toast.makeText(MainActivity.this, "hardware not available", Toast.LENGTH_SHORT).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                //biometric device is working, but no biometric data has been saved before
                Log.i(TAG, "onCreate: BIOMETRIC_ERROR_NONE_ENROLLED");
                sendUserToSettings();
                break;

        }

    }

    private void sendUserToSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
            intent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
            startActivity(intent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            startActivity(new Intent(Settings.ACTION_FINGERPRINT_ENROLL));
        } else{
            startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
        }
    }

    private void init() {
        biometricBTN = findViewById(R.id.biometricBTN);
    }
}