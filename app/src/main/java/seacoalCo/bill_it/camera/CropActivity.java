package seacoalCo.bill_it.camera;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.parser.Parser;
import seacoalCo.bill_it.utility_classes.TutorialBuilder;

public class CropActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    CropImageView cropView;
    Bitmap bitmap;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        Toolbar toolbar = findViewById(R.id.done_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        this.cropView = findViewById(R.id.crop_view);

        try {
            bitmap = BitmapFactory.decodeStream(getApplicationContext().openFileInput("image"));
            cropView.setImageBitmap(bitmap);
        } catch (FileNotFoundException ex) {
            String message = "Whoops, something went wrong";
            Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            toast.show();
        }

        if (sharedPreferences.getBoolean(getString(R.string.CROPTUTORIAL), true) && savedInstanceState == null) {
            AlertDialog alertDialog = TutorialBuilder.buildTutorial(R.string.CROPTUTORIAL, getString(R.string.crop_message), this);
            alertDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_done, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_done:
                onCrop();
                return true;
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void onCrop() {
        //Create a new progress dialog
        progressDialog = new ProgressDialog(this);
        //Set the progress dialog to display a horizontal progress bar
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //Set the dialog title to 'Loading...'
        progressDialog.setTitle("Loading...");
        //Set the dialog message to 'Loading application View, please wait...'
        progressDialog.setMessage("Loading items, please wait...");
        //This dialog can't be canceled by pressing the back key
        progressDialog.setCancelable(false);
        //This dialog isn't indeterminate
        progressDialog.setIndeterminate(true);
        //Display the progress dialog
        progressDialog.show();

        Bitmap cropped = cropView.getCroppedImage();

        // Text(blocks) uit de afbeelding halen
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(cropped);

        FirebaseVisionTextDetector detector = FirebaseVision.getInstance()
                .getVisionTextDetector();
        detector.detectInImage(image)
                .addOnSuccessListener(firebaseVisionText -> {
                    ArrayList<ItemDraft> items = Parser.parseBlocksToItems(firebaseVisionText.getBlocks());
                    Intent imageActivity = new Intent(this, ImageActivity.class);
                    imageActivity.putParcelableArrayListExtra("items", items);
                    progressDialog.dismiss();
                    this.startActivity(imageActivity);
                    finish();
                })
                .addOnFailureListener(
                        e -> {
                            Log.i("OCR", "Failed to detect");
                            finish();
                        });
    }
}
