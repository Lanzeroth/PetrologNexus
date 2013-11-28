package com.petrologautomation.petrolognexus;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class MainActivity extends Activity {

    public static G4Petrolog PetrologSerialCom;

    private static FrameLayout All;

    private Timer UIUpdate;
    private Timer StaticDynaUpdate;
    private Timer SerialComHeartBeat;

    private static wellStatus_post wellStatusPost;
    private static wellRuntime_post wellRuntimePost;
    private static wellHistoricalRuntime_post wellHistoricalRuntimePost;
    public static wellDynagraph_post wellDynagraphPost;
    private static wellSettings_post wellSettingsPost;
    private static wellFillage_post wellFillagePost;

    private static wellSettings_edit wellSettingsEdit;

    public static help Help;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    public static boolean Connected = false;
    public static final int REQUEST_ENABLE_BT = 1;
    public static final String UUID_BLUE_RADIOS = "00001101-0000-1000-8000-00805F9B34FB";
    private String wellName;

    public static Menu MyMenu;

    private Boolean petrologFound;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("PN - BT","BroadcastReceiver Action = "+action);

            /* Bluetooth disconnect */
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                Toast.makeText(MainActivity.this, "Petrolog disconnected", Toast.LENGTH_SHORT).show();
                disconnect();
            }

            /* Bluetooth Discovery Started */
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                petrologFound = false;
                removeAllMenuItems();
                setProgressBarIndeterminateVisibility(true);
            }

            /* Bluetooth Discovery finds a device */
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                try {
                    if (device.getName().contains("Petrolog")) {
                        device.fetchUuidsWithSdp();
                        Toast.makeText(MainActivity.this, "Petrolog found: Connecting", Toast.LENGTH_SHORT).show();
                        petrologFound = true;
                        mBluetoothAdapter.cancelDiscovery();
                        new AsyncBluetoothConnect().execute(device);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error, try again", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            /* Bluetooth Discovery Finished */
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                if (!petrologFound){
                    setMenuIconsDisconnected();
                    /* Enable BT connect menu button */
                    MyMenu.getItem(4).setEnabled(true);

                    Toast.makeText(MainActivity.this, "Discovery finished: Petrolog not found, try again", Toast.LENGTH_SHORT)
                            .show();
                }
                setProgressBarIndeterminateVisibility(false);
            }
        }
    };

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        /* Progress spinner on menu */
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_main);

        Help = new help(this);

    }

    @Override
    protected void onPause (){
        super.onPause();
        Log.i ("PN - onPause", "Called!!");
        prepareForExit();
    }

    @Override
    protected void onResume (){
        super.onResume();
        myInit();
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.i ("PN - onDestroy", "Called!!");
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MyMenu = menu;

        // Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mainmenu, menu);

        return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MyMenu = menu;

        if (mBluetoothSocket == null){
            /* Menu icons */
            setMenuIconsDisconnected();
            /* help */
            Help.setDisconnected();
        }
        else {
            /* Menu icons */
            setMenuIconsConnected();
            /* help */
            Help.setConnectedStopped();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.connect:
                /* Disable BT connect menu button */
                MyMenu.getItem(4).setEnabled(false);

                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    // Device does not support Bluetooth
                    Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_SHORT)
                            .show();
                }
                else {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                    else {
                        Toast.makeText(this,"Bluetooth active",Toast.LENGTH_SHORT).show();
                        mBluetoothAdapter.startDiscovery();
                    }
                }
                break;

            case R.id.disconnect:
                disconnect();
                break;

            case R.id.clean:
                wellDynagraphPost.clean();
                break;

            case R.id.help:

                LinearLayout help = (LinearLayout)findViewById(R.id.Help);

                if(help.getVisibility()==View.VISIBLE){
                    help.setVisibility(View.INVISIBLE);
                }
                else{
                    help.setVisibility(View.VISIBLE);
                }


                break;

            case R.id.settings:
                wellSettingsEdit.popup();
                break;

            case R.id.start_well:
                PetrologSerialCom.start();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult (int request, int result, Intent data ) {
        if (request == REQUEST_ENABLE_BT )
            if (result == RESULT_OK) {
                mBluetoothAdapter.startDiscovery();
            }
            else
                Toast.makeText(this,"Bluetooth activation failed",Toast.LENGTH_SHORT).show();
    }

    public class AsyncBluetoothConnect extends AsyncTask<BluetoothDevice, Void, Boolean> {
        BluetoothDevice Device;
        FrameLayout Wait = (FrameLayout) findViewById(R.id.wait);

        protected void onPreExecute() {
            Wait.setVisibility(View.VISIBLE);
        }

        protected Boolean doInBackground(BluetoothDevice... device) {
            Device = device[0];

            try {
                Log.i("PN - BT",""+device[0].getName());
                mBluetoothSocket = device[0].createInsecureRfcommSocketToServiceRecord
                        (UUID.fromString(UUID_BLUE_RADIOS));

                /* Blocking !!!*/
                mBluetoothSocket.connect();

                /* get bluetooth name */
                BlueRadios bluetooth = new BlueRadios(mBluetoothSocket);
                if(bluetooth.CommandMode()){
                    wellName = bluetooth.Read(0);
                    if(wellName.equals("Error")){
                        wellName = Device.getName();
                    }
                }
                else{
                    wellName = Device.getName();
                }
                if(!bluetooth.DataMode()){
                    return false;
                }
                /* Release!*/
                /* Init G4 Com */
                PetrologSerialCom = new G4Petrolog(mBluetoothSocket);
                /* Ask Petrolog last 30 days of history */
                PetrologSerialCom.requestPetrologHistory();
                return true;
            } catch (IOException e) {
                return false;
            } catch (NullPointerException e) {
                Log.i("PN - BT","No device found?");
                return false;
            }

        }

        protected void onPostExecute(Boolean ok) {
            /* Enable BT connect menu button */
            MyMenu.getItem(4).setEnabled(true); //TODO

            if (ok) {
                /* Menu icons */
                setMenuIconsConnected();
                /* help */
                Help.setConnectedStopped();
                /* Post Petrolog last 30 days of history */
                wellHistoricalRuntimePost.post();
                /* Action bar title (Well Name) */
                ActionBar bar = getActionBar();
                bar.setTitle(getString(R.string.app_title) + " - " + wellName);
                /* Run Serial Heart Beat only if BT connection established */
                Connected = true;
                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT)
                        .show();

            }
            else {
                setMenuIconsDisconnected();
                //help
                Help.setDisconnected();
                Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_SHORT)
                        .show();
            }

            Wait.setVisibility(View.INVISIBLE);


        }

    }

    private void myInit (){

        // Force call to onPrepareOptionsMenu()
        invalidateOptionsMenu();

        // Remove notification bar
        getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE);

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filter);

        /* Timer to update info from Petrolog */
        if (SerialComHeartBeat == null){
            SerialComHeartBeat = new Timer();
        }
        SerialComHeartBeat.schedule(new TimerTask() {
            @Override
            public void run() {
            // Serial
            if (Connected) {
                PetrologSerialCom.HeartBeat();
            }
            }
        }, 0, 200);

        /* Settings PopUp */
        wellSettingsEdit = new wellSettings_edit(this);

        /* Timer to Update UI */
        All = (FrameLayout)findViewById(R.id.Main);
        wellStatusPost = new wellStatus_post(this);
        wellSettingsPost = new wellSettings_post(this);
        wellRuntimePost = new wellRuntime_post(this);
        wellDynagraphPost = new wellDynagraph_post(this);
        wellHistoricalRuntimePost = new wellHistoricalRuntime_post(this);
        wellFillagePost = new wellFillage_post(this);

        UIUpdate = new Timer();
        UIUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
            // Serial
            if (Connected) {
                All.post(new Runnable() {
                    @Override
                    public void run() {
                        wellStatusPost.post();
                        wellSettingsPost.post();
                        wellRuntimePost.post();
                        wellFillagePost.post();
                    }
                });
            }
            }
        }, 0, 400);

        /* Timer to Update Dyna */
        if (StaticDynaUpdate == null){
            StaticDynaUpdate = new Timer();
        }
        StaticDynaUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
            // Serial
            if (Connected) {
                All.post(new Runnable() {
                    @Override
                    public void run() {
                        wellDynagraphPost.post();
                    }
                });
            }
            }
        }, 0, 200);


    }

    private void prepareForExit (){

        try {
            /* Close Cx */
            PetrologSerialCom.Disconnect();
            Connected = false;

            /* Close BT */
            mBluetoothSocket.close();
            mBluetoothSocket = null;
            unregisterReceiver(mReceiver);

            /* Stop Timers */
            UIUpdate.cancel();
            UIUpdate.purge();
            UIUpdate = null;
            StaticDynaUpdate.cancel();
            StaticDynaUpdate.purge();
            StaticDynaUpdate = null;
            SerialComHeartBeat.cancel();
            SerialComHeartBeat.purge();
            SerialComHeartBeat = null;

            /* Clean UI */
            cleanUI();
            /* Prepare Menu */
            setMenuIconsDisconnected();

            /* Help */
            Help.setDisconnected();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private void disconnect (){

        try {
            /* Close Cx */
            PetrologSerialCom.Disconnect();
            Connected = false;

            /* Close BT */
            mBluetoothSocket.close();
            mBluetoothSocket = null;

            /* Clean UI */
            cleanUI();
            /* Prepare Menu */
            setMenuIconsDisconnected();
            /* Help */
            Help.setDisconnected();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private void setMenuIconsConnected(){
        MyMenu.getItem(4).setVisible(false); //Connect
        MyMenu.getItem(5).setVisible(true); //Disconnect
        MyMenu.getItem(1).setVisible(true); //Settings
        MyMenu.getItem(2).setVisible(true); //Clean
        MyMenu.getItem(0).setVisible(false); //Run
        MyMenu.getItem(3).setVisible(true); //Help

    }

    private void setMenuIconsDisconnected(){
        MyMenu.getItem(4).setVisible(true);  //Connect
        MyMenu.getItem(5).setVisible(false); //Disconnect
        MyMenu.getItem(1).setVisible(false); //Settings
        MyMenu.getItem(2).setVisible(false); //Clean
        MyMenu.getItem(0).setVisible(false); //Run
        MyMenu.getItem(3).setVisible(true); //Help

    }

    private void removeAllMenuItems(){
        MyMenu.getItem(4).setVisible(false); //Connect
        MyMenu.getItem(5).setVisible(false); //Disconnect
        MyMenu.getItem(1).setVisible(false); //Settings
        MyMenu.getItem(2).setVisible(false); //Clean
        MyMenu.getItem(0).setVisible(false); //Run
        MyMenu.getItem(3).setVisible(false); //Help

    }

    private void cleanUI(){

        wellDynagraphPost.clean();
        wellHistoricalRuntimePost.clean();

        /* Last Update to display N/A (all variables are cleared on Disconnect@G4Petrolog) */
        All.post(new Runnable() {
            @Override
            public void run() {
                wellStatusPost.post();
                wellSettingsPost.post();
                wellRuntimePost.post();
                wellFillagePost.post();
            }
        });
    }


} /* Class */




