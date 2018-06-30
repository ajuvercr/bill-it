package seacoalCo.bill_it.logics;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;

public class FileHandler {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static <T> T loadFile(File f, T defaultObject) {
        checkFile(f);

        try {
            FileInputStream fileIn = new FileInputStream(f);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            T out = (T) in.readObject();
            in.close();
            fileIn.close();
            return out;
        } catch (Exception i) {
            //i.printStackTrace();
        }

        return defaultObject;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void saveFile(File f, Object o) {
        checkFile(f);
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(f);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(o);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void checkFile(File f) {

        try {
            if (!f.getParentFile().getParentFile().exists()) {
                try {
                    Files.createDirectory(f.getParentFile().getParentFile().toPath());
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }catch (NullPointerException e) {
            //e.printStackTrace();
        }

        if(!f.getParentFile().exists()){
            try {
                Files.createDirectory(f.getParentFile().toPath());
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        if(!f.exists()) {
            try {
                Files.createFile(f.toPath());
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }
}
