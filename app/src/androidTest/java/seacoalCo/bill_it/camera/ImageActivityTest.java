package seacoalCo.bill_it.camera;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import seacoalCo.bill_it.parser.Parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ImageActivityTest {

    // Context of the app under test.
    // For the Text recognition API
    private Context appContext;
    // Context of the test
    // For the image sample resources
    private Context context;
    private AssetManager assetManager;


    @Before
    public void setup() {
        appContext = InstrumentationRegistry.getTargetContext();
        context = InstrumentationRegistry.getContext();
        assetManager = context.getAssets();
    }

    @Test
    public void testAldi1() throws Exception {
        // Use this with the debugger to get text data to paste in a new file
        String pString = pictureToString("aldi1.jpg");
        String tString = textToString("aldi1.txt");
        assertEquals("OCR result differs from previous saved version", tString, pString);
    }

    class ItemList {
        ArrayList<ItemDraft> items;

        public ItemList(ArrayList<ItemDraft> items) {
            this.items = items;
        }

        // Needed to copy the debugger string representation to the clipboard
        public String toString() {
            StringBuilder sb = new StringBuilder();
            items.forEach(i -> sb.append(i.toString()).append("\n"));
            return sb.toString();
        }
    }

    public String pictureToString(String imgPath) throws IOException {

        InputStream istr = assetManager.open(imgPath);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);

        // Use a correct path
        assertTrue(bitmap != null);

        ArrayList<ItemDraft> items;

        // Text(blocks) uit de afbeelding halen
        TextRecognizer textRecognizer = new TextRecognizer.Builder(appContext).build();
        Frame.Builder builder = new Frame.Builder().setBitmap(bitmap);
        SparseArray<TextBlock> blocks = textRecognizer.detect(builder.build());
        items = Parser.parseBlocksToItems(blocks);
        return (new ItemList(items)).toString();
    }

    public String textToString(String txtPath) throws IOException {

        InputStream is = assetManager.open(txtPath);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String ls = "\n";
        String line;
        while (( line = br.readLine() ) != null) {
            sb.append( line );
            sb.append( ls );
        }

        return sb.toString();
    }
}