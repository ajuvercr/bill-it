package seacoalCo.bill_it.parser;


import com.google.android.gms.vision.text.Line;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.util.ArrayList;

import seacoalCo.bill_it.camera.ItemDraft;

public class LineParser {

    static ArrayList<ItemDraft> parseVisionLines(ArrayList<ArrayList<FirebaseVisionText.Line>> matrix) {
        ArrayList<ItemDraft> output = new ArrayList<>();
        // Itemdrafts maken uit de rijen van de matrix
        for (ArrayList<FirebaseVisionText.Line> a : matrix) {
            ItemDraft newItem = new ItemDraft();
            for (FirebaseVisionText.Line l : a) {
                newItem.splitAndAdd(l.getText());
            }
            newItem.finish();
            output.add(newItem);
        }
        return output;
    }

    static ArrayList<ItemDraft> parseLines(ArrayList<ArrayList<Line>> matrix) {
        ArrayList<ItemDraft> output = new ArrayList<>();
        // Itemdrafts maken uit de rijen van de matrix
        for (ArrayList<Line> a : matrix) {
            ItemDraft newItem = new ItemDraft();
            for (Line l : a) {
                newItem.splitAndAdd(l.getValue());
            }
            newItem.finish();
            output.add(newItem);
        }
        return output;
    }
}
