package com.randaelektronik.sipapplicationone;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.text.ParseException;

public class MainActivity extends AppCompatActivity {
    public SipManager sipManager = null;
    public SipProfile sipProfile = null;
    public SipAudioCall call = null;
    public String sipAddress = null;

    String username = "7002";
String domain = "192.162.1.43";
String password = "123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (sipManager == null) {
            sipManager = SipManager.newInstance(this);
        }

        SipProfile.Builder builder = null;
        try {
            builder = new SipProfile.Builder(username, domain);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        builder.setPassword(password);
        sipProfile = builder.build();



        Intent intent = new Intent();
        intent.setAction("android.randaelektronik.sipapplicationone");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, Intent.FILL_IN_DATA);
        try {
            sipManager.open(sipProfile, pendingIntent, null);



        sipManager.setRegistrationListener(sipProfile.getUriString(), new SipRegistrationListener() {

            public void onRegistering(String localProfileUri) {
                updateStatus("Registering with SIP Server...");
            }

            public void onRegistrationDone(String localProfileUri, long expiryTime) {
                updateStatus("Ready");
            }

            public void onRegistrationFailed(String localProfileUri, int errorCode,
                                             String errorMessage) {
                updateStatus("Registration failed.  Please check settings.");
            }








                });
        } catch (SipException e) {
            updateStatus("Connection Error.");
        }
        initiateCall();
    }



    public void updateStatus(final String status) {
        // Be a good citizen.  Make sure UI changes fire on the UI thread.
        this.runOnUiThread(new Runnable() {
            public void run() {
                TextView labelView = (TextView) findViewById(R.id.sipLabel);
                labelView.setText(status);
            }
        });
    }

    /**
     * Updates the status box with the SIP address of the current call.
     * @param call The current, active call.
     */
    public void updateStatus(SipAudioCall call) {
        String useName = call.getPeerProfile().getDisplayName();
        if(useName == null) {
            useName = call.getPeerProfile().getUserName();
        }
        updateStatus(useName + "@" + call.getPeerProfile().getSipDomain());
    }

    @SuppressLint("LongLogTag")
    public void closeLocalProfile() {
        if (sipManager == null) {
            return;
        }
        try {
            if (sipProfile != null) {
                sipManager.close(sipProfile.getUriString());
            }
        } catch (Exception ee) {
            Log.d("WalkieTalkieActivityonDestroy", "Failed to close local profile.", ee);
        }
    }
    public void initiateCall() {

        updateStatus(sipAddress);

        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                // Much of the client's interaction with the SIP Stack will
                // happen via listeners.  Even making an outgoing call, don't
                // forget to set up a listener to set things up once the call is established.
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    call.startAudio();
                    call.setSpeakerMode(true);
                    call.toggleMute();
                    updateStatus(call);
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    updateStatus("Ready.");
                }
            };

            call = sipManager.makeAudioCall(sipProfile.getUriString(), sipAddress, listener, 30);

        }
        catch (Exception e) {
            Log.i("tt", "Error when trying to close manager.", e);
            if (sipProfile != null) {
                try {
                    sipManager.close(sipProfile.getUriString());
                } catch (Exception ee) {
                    Log.i("tt",
                            "Error when trying to close manager.", ee);
                    ee.printStackTrace();
                }
            }
            if (call != null) {
                call.close();
            }
        }
    }

}
