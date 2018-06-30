package seacoalCo.bill_it.parser;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.util.ArrayList;
import java.util.List;

public class BlockParser {
    public static ArrayList<ArrayList<FirebaseVisionText.Line>> parseVisionBlocks(List<FirebaseVisionText.Block> blocks) {

        ArrayList<ArrayList<FirebaseVisionText.Line>> matrix = new ArrayList<>();
        List<FirebaseVisionText.Line> lines = new ArrayList<>();

        // Blocks omzetten in lines
        for (FirebaseVisionText.Block block : blocks) {
            for (FirebaseVisionText.Line line : block.getLines()) {
                lines.add(line);
            }
        }
        lines.forEach(line -> Log.d("props", line.getText()));
        // Lines proberen plaatsen in de ruimte m.b.v. een matrix
        for (FirebaseVisionText.Line l : lines) {
            if (!matrix.isEmpty()) {
                int i = 0;
                boolean placed = false;
                while (i < matrix.size() && !placed) {
                    if (Math.abs(l.getBoundingBox().centerY() - matrix.get(i).get(0).getBoundingBox().centerY())
                            < l.getBoundingBox().height() * 0.8) {
                        int j = 0;
                        boolean sorted = false;
                        while (j < matrix.get(i).size() && !sorted) {
                            if (l.getBoundingBox().centerX() < matrix.get(i).get(j).getBoundingBox().centerX()) {
                                matrix.get(i).add(j, l);
                                sorted = true;
                            }
                            j++;
                        }
                        if (!sorted) {
                            matrix.get(i).add(l);
                        }
                        placed = true;
                    }
                    i++;
                }
                if (!placed) {
                    ArrayList<FirebaseVisionText.Line> temp = new ArrayList<>();
                    temp.add(l);
                    matrix.add(temp);
                }
            } else {
                ArrayList<FirebaseVisionText.Line> temp = new ArrayList<>();
                temp.add(l);
                matrix.add(temp);
            }
        }
        return matrix;
    }

    static ArrayList<ArrayList<Line>> parseBlocks(SparseArray<TextBlock> blocks) {

        ArrayList<ArrayList<Line>> matrix = new ArrayList<>();
        List<Line> lines = new ArrayList<>();

        // Blocks omzetten in lines
        int size = blocks.size();
        for (int i = 0; i < size; i++) {
            TextBlock block = blocks.get(blocks.keyAt(i));
            lines.addAll((ArrayList<Line>) block.getComponents());
        }

        // Lines proberen plaatsen in de ruimte m.b.v. een matrix
        for (Line l : lines) {
            if (!matrix.isEmpty()) {
                int i = 0;
                boolean placed = false;
                while (i < matrix.size() && !placed) {
                    if (Math.abs(l.getBoundingBox().centerY() - matrix.get(i).get(0).getBoundingBox().centerY())
                            < l.getBoundingBox().height() * 0.8) {
                        int j = 0;
                        boolean sorted = false;
                        while (j < matrix.get(i).size() && !sorted) {
                            if (l.getBoundingBox().centerX() < matrix.get(i).get(j).getBoundingBox().centerX()) {
                                matrix.get(i).add(j, l);
                                sorted = true;
                            }
                            j++;
                        }
                        if (!sorted) {
                            matrix.get(i).add(l);
                        }
                        placed = true;
                    }
                    i++;
                }
                if (!placed) {
                    ArrayList<Line> temp = new ArrayList<>();
                    temp.add(l);
                    matrix.add(temp);
                }
            } else {
                ArrayList<Line> temp = new ArrayList<>();
                temp.add(l);
                matrix.add(temp);
            }
        }
        return matrix;
    }
}
