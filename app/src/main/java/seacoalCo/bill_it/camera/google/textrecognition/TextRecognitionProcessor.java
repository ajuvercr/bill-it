// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package seacoalCo.bill_it.camera.google.textrecognition;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import seacoalCo.bill_it.camera.ItemDraft;
import seacoalCo.bill_it.camera.google.FrameMetadata;
import seacoalCo.bill_it.camera.google.GraphicOverlay;
import seacoalCo.bill_it.camera.google.OcrCaptureActivity;
import seacoalCo.bill_it.camera.google.VisionProcessorBase;
import seacoalCo.bill_it.parser.Parser;

/** Processor for the text recognition demo. */
public class TextRecognitionProcessor extends VisionProcessorBase<FirebaseVisionText> {

  private static final String TAG = "TextRecognitionProcessor";

  private final FirebaseVisionTextDetector detector;
  protected final OcrCaptureActivity ocr;

  private boolean detectingItems = false;

  public TextRecognitionProcessor(OcrCaptureActivity ocr) {
    this.ocr = ocr;
    detector = FirebaseVision.getInstance().getVisionTextDetector();
  }

  @Override
  public void stop() {
    try {
      detector.close();
    } catch (IOException e) {
      Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
    }
  }

  @Override
  protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
    return detector.detectInImage(image);
  }

  @Override
  protected void onSuccess(
      @NonNull FirebaseVisionText results,
      @NonNull FrameMetadata frameMetadata,
      @NonNull GraphicOverlay graphicOverlay) {

    //detectItems(results);

    graphicOverlay.clear();
    List<FirebaseVisionText.Block> blocks = results.getBlocks();
    for (int i = 0; i < blocks.size(); i++) {
      List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
      for (int j = 0; j < lines.size(); j++) {
        List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
        for (int k = 0; k < elements.size(); k++) {
          GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay, elements.get(k));
          graphicOverlay.add(textGraphic);

        }
      }
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.w(TAG, "Text detection failed." + e);
  }

  private void detectItems(FirebaseVisionText text) {
    if (! detectingItems) {
      setDectingItems(true);
      new ItemDetectionTask(this, text).execute();
    }
  }

  protected void setDectingItems(boolean bool) {
      this.detectingItems = bool;
  }

  private static class ItemDetectionTask extends AsyncTask<Void, Void, Void> {

      private final FirebaseVisionText text;
      private final WeakReference<TextRecognitionProcessor> processor;

      private List<FirebaseVisionText.Block> blocks;
      private List<ItemDraft> items = null;
      private boolean itemsDetected = false;

      ItemDetectionTask(TextRecognitionProcessor processor, FirebaseVisionText text) {
          this.processor = new WeakReference<>(processor);
          this.text = text;
      }

      private double sumOfBlock(FirebaseVisionText.Block block) {
          return block.getLines().stream()
                  .map(FirebaseVisionText.Line::getElements)
                  .mapToDouble(l -> l.stream().mapToDouble(e -> {
                      try {
                          return NumberFormat.getInstance().parse(e.getText()).doubleValue();
                      } catch (ParseException e1) {
                          return 0.0;
                      }
                  }).sum())
                  .sum();
      }

      // Return bool of whether items contains a valid set of items, with the correct sum
      // argument sum should not be zero
      private void parseValidItems(int i, double sum) {
          if (itemsDetected) return;
          List<FirebaseVisionText.Block> output = new ArrayList<>();
          List<FirebaseVisionText.Block> blocks = text.getBlocks();

          FirebaseVisionText.Block prices = blocks.get(i);
          Rect priceRect = prices.getBoundingBox();
          Rect boundingBox = new Rect(priceRect);

          // Set boundingBox left to 0 by adding right to width
          // Set a little taller
          boundingBox.offset(boundingBox.right, 20);

          items = Parser.parseBlocksToItems(blocks.stream()
                  .filter(b -> boundingBox.contains(b.getBoundingBox()))
                  .collect(Collectors.toList()));
          itemsDetected = ItemDraft.sum(items) == sum;
      }

      @Override
      protected Void doInBackground(Void... voids) {
          // Detection works by summing up the prices in one block
          // and comparing to the sum Value a few blocks after it.
          // This method is only reliable from a certain minimum of items, e.g. 3

          // Go through all the block, pick a tentative amount block
          // map to floats, sum it up and compare with the floatified block, up to 3 block down
          // If a match, pass
          List<FirebaseVisionText.Block> blocks = text.getBlocks();
          FirebaseVisionText.Block pricesBlock;
          FirebaseVisionText.Block sumBlock;
          double sumPrices;
          double sumSum;
          int lookahead = 3;
          for (int i = 0; i < blocks.size() - lookahead; i++) {
              pricesBlock = blocks.get(i);
              sumPrices = sumOfBlock(pricesBlock);
              if (sumPrices == 0.0) continue;
              for (int j = i; j < lookahead; j++) {
                  sumBlock = blocks.get(j);
                  sumSum = sumOfBlock(sumBlock);
                  if (sumSum != 0.0 && sumPrices == sumSum) {
                      Log.i("OCR", "Sum detected of: " + sumSum);
                      parseValidItems(i, sumSum);
                      return null;
                  }
              }
          }
          itemsDetected = false;
          return null;
      }

      @Override
      protected void onPostExecute(Void result) {
        if (itemsDetected) {
            if (processor.get() != null) {
                processor.get().ocr.itemsDetected(items);
            }
        } else {
            if (processor.get() != null) {
                processor.get().setDectingItems(false);
            }
        }
      }
  }
}
