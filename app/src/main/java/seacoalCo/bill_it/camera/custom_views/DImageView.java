package seacoalCo.bill_it.camera.custom_views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;


/**
 * Created by tibod on 26/02/2018.
 */

public class DImageView extends AppCompatImageView {

    private SparseArray<TextBlock> selected = new SparseArray<>();
    private SparseArray<TextBlock> unselected;
    private Paint selectedC;
    private Paint unselectedC;
    private double scale;
    private Bitmap bm;


    public DImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        selectedC = new Paint(Color.BLUE);
        selectedC.setStrokeWidth(5);
        selectedC.setStyle(Paint.Style.STROKE);
        selectedC.setColor(Color.BLUE);

        unselectedC = new Paint();
        unselectedC.setColor(Color.WHITE);
        unselectedC.setStrokeWidth(5);
        unselectedC.setStyle(Paint.Style.STROKE);
    }
    /*
    @Override
    protected void onDraw(Canvas canvas) {
        drawBoxes(canvas);

        super.onDraw(canvas);
    }*/

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);

        this.bm = bm;

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getContext()).build();
        Frame.Builder builder = new Frame.Builder().setBitmap(bm);
        unselected = textRecognizer.detect(builder.build());

         Bitmap tempBitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(tempBitmap);

         canvas.drawBitmap(bm, 0, 0, null);

         this.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        scale = Math.max(bm.getHeight() / this.getHeight(), bm.getWidth() / this.getWidth());
    }

    public void onTap(int x, int y) {
        boolean found = false;
        Log.d("touch", "touchy at " + (x * scale) + ", " + (y * scale) + ", scale: " + scale);
        int i = 0;
        while (i < unselected.size() && !found) {
            TextBlock block = unselected.valueAt(i);
            Log.d(block.getValue(), block.getBoundingBox().flattenToString());
            if (block.getBoundingBox().contains((int) (x * scale), (int) (y * scale))) {
                Log.d("success", "Contains position");
                int key = unselected.keyAt(i);
                unselected.removeAt(i);
                selected.append(key, block);
                found = true;
            }
            i++;
        }

        i = 0;
        while (i < selected.size() && !found) {
            TextBlock block = selected.valueAt(i);
            //Log.d(block.getValue(), block.getBoundingBox().flattenToString());
            if (block.getBoundingBox().contains((int) (x * scale), (int) (y * scale))) {
                int key = selected.keyAt(i);
                selected.removeAt(i);
                unselected.append(key, block);
                found = true;
            }
            i++;
        }
    }

    private void drawBoxes(Canvas canvas) {
        //Create a new image bitmap and attach a brand new canvas to it
        Bitmap tempBitmap = Bitmap.createBitmap(bm.getWidth(), bm   .getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);

        //Draw the image bitmap into the canvas
        tempCanvas.drawBitmap(bm, 0, 0, null);


        for(int i = 0; i < unselected.size(); i++) {
            TextBlock block = unselected.valueAt(i);
            Rect rect = block.getBoundingBox();
            Log.d("scaled rect", (float) (rect.left / scale) + ", " + (float) (rect.top / scale) + ", " + (float) (rect.right / scale) + ", " + (float) (rect.bottom / scale));
            canvas.drawRect((float) (rect.left / scale), (float) (rect.top / scale), (float) (rect.right / scale), (float) (rect.bottom / scale), unselectedC);
        }

        for(int i = 0; i < selected.size(); i++) {
            TextBlock block = selected.valueAt(i);
            Rect rect = block.getBoundingBox();
            canvas.drawRect((float) (rect.left / scale), (float) (rect.top / scale), (float) (rect.right / scale), (float) (rect.bottom / scale), selectedC);
        }

        //Attach the canvas to the ImageView
        //this.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
    }
}
