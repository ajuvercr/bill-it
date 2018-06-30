package seacoalCo.bill_it.parser;

import android.util.SparseArray;

import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.util.ArrayList;
import java.util.List;

import seacoalCo.bill_it.camera.ItemDraft;

public class Parser {
    public static ArrayList<ItemDraft> parseBlocksToItems(SparseArray<TextBlock> blocks) {
        ArrayList<ArrayList<Line>> matrix = BlockParser.parseBlocks(blocks);
        return LineParser.parseLines(matrix);
    }

    public static ArrayList<ItemDraft> parseVisionToItems(FirebaseVisionText text) {
        ArrayList<ArrayList<FirebaseVisionText.Line>> matrix = BlockParser.parseVisionBlocks(text.getBlocks());
        return LineParser.parseVisionLines(matrix);
    }

    public static ArrayList<ItemDraft> parseBlocksToItems(List<FirebaseVisionText.Block> blocks) {
        ArrayList<ArrayList<FirebaseVisionText.Line>> matrix = BlockParser.parseVisionBlocks(blocks);
        return LineParser.parseVisionLines(matrix);
    }
}
