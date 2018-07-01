package seacoalCo.bill_it.friends;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.firestore.FirebaseFirestore;

import net.glxn.qrgen.android.QRCode;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.user.User;
import seacoalCo.bill_it.utility_classes.IdGenerator;

public class AddFriend extends AppCompatActivity {

    private TextView txtName;
    private TextView txtEmail;
    private EditText idField;

    private SharedPreferences preferences;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private BarcodeDetector detector;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtName = findViewById(R.id.addFriendName);
        txtEmail = findViewById(R.id.addFriendEmail);
        idField = findViewById(R.id.idField);
        Button confirmButton = findViewById(R.id.addFriendConfirm);

        confirmButton.setOnClickListener((v) -> {
                User.getLoggedInUser().addFriend(
                        new User(txtName.getText().toString(), txtEmail.getText().toString(), IdGenerator.getNewId()).getId()
                );
                finish();
            }
        );

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String id = preferences.getString(getString(R.string.user_id), "");

        TextView idView = findViewById(R.id.idView);
        idView.setText(id);

        detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();

        if(!detector.isOperational()) {
            detector = null;
        }

        Bitmap myBitmap = QRCode.from(id).bitmap();
        ImageView myImage = (ImageView) findViewById(R.id.qrView);
        myImage.setImageBitmap(Bitmap.createScaledBitmap(myBitmap, 500,500, false));
    }

    public void onDifference(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.friends_explanation));
        builder.setNeutralButton("I see", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    public void onClip(View v) {
        ClipboardManager clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clip.setPrimaryClip(ClipData.newPlainText("User id", preferences.getString(getString(R.string.user_id), "")));
        Toast toast = Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void onScan(View v) {
        if(detector == null) {
            Toast.makeText(this, "We can't", Toast.LENGTH_LONG);
            return;
        }
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_PERMISSION_CODE);
        } else {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new
                        Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            SparseArray<Barcode> barcodes = detector.detect(new Frame.Builder().setBitmap(photo).build());
            switch (barcodes.size()) {
                case 0:
                    Toast.makeText(this, "No qr-code found", Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    Log.d("BARCODE", barcodes.valueAt(0).rawValue);
                    addOnlineFriend(barcodes.valueAt(0).rawValue);
                    break;
                default:
                    Toast.makeText(this, "Ambigues qr-code found", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    public void onOnline(View v) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setTitle("Whoops");
            builder.setMessage("You're going to need internet access to log in");
            builder.setNeutralButton("Oh  :(", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        }
        else {
            addOnlineFriend(idField.getText().toString());
        }
    }

    private void addOnlineFriend(String id) {
        Store.getUser(id, (s, t) -> {
            if(s) {
                User t2 = User.getLoggedInUser();
                t.addFriend(t2.getId());
                t2.addFriend(t.getId());
                Store.save(t);
                Store.save(t2);

                Toast toast = Toast.makeText(getApplicationContext(), "Friend added", Toast.LENGTH_SHORT);
                toast.show();
            }else{
                Toast toast = Toast.makeText(getApplicationContext(), "This user doesn't seem to exist", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
