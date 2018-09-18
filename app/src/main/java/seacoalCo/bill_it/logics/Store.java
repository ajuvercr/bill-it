package seacoalCo.bill_it.logics;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import seacoalCo.bill_it.logics.group.Group;
import seacoalCo.bill_it.logics.user.User;


/// This is used to communicate between firebase db, local files etc
/// It has an internal state of groups and users so it doesn't have to fetch everything
/// Works with callback if the current state doesn't have a precise object you want
/// This callback is always called, even when it is just the same info
/// This callback will have the most up-to-date data
/// This callback is potentially called a second time with data from firebase
/// whenInternet has all things that need to be done when there is internet
///
/// Only send callback with succes == false when there is no way to get the requested data
public class Store implements Runnable {
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    private static Store store;

    public static final String GROUPSFOLDER = "groups";
    public static final String USERSFOLDER = "users";

    private HashMap<String, Group> groups;
    private HashMap<String, User> users;
    private Deque<Runnable> whenInternet;
    private Context context;
    private PrintWriter out = null;
    private BufferedReader in = null;

    private Handler uiHandler;

    public static void init(Context c, Handler h) {
        if(store == null)
            store = new Store(c, h);
    }

    public static Store getInstance() {
        return store;
    }

    private Store(Context c, Handler handler) {
        groups = new HashMap<>();
        users = new HashMap<>();
        whenInternet = new ArrayDeque<>();
        context = c;
        uiHandler = handler;

        File f = context.getFilesDir();
        File fs = new File(f, "users");
        fs.mkdir();
        fs = new File(f, "groups");
        fs.mkdir();
    }

    private void connect() {
        try{
            Socket socket = new Socket("10.0.2.2", 8080);
            out = new PrintWriter(socket.getOutputStream(),
                    true);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
        } catch (UnknownHostException e) {
            Log.d("USSR", "couldn't connect");
            Log.d("USSR", e.getMessage());
        } catch  (IOException e) {
            Log.d("USSR", "No I/O");
            Log.d("USSR", e.getMessage());
        }
    }

    private void saveAllInstance() {
        for(Group g : groups.values()) {
            save(g);
        }
        for(User u : users.values()) {
            save(u);
        }
    }

    // use with caution
    // saves ALL instate
    public static void saveAll() {
        if(store == null) {
            throw new RuntimeException("STORE NOT INITIATED, PLEASE CALL STORE.INIT FIRST");
        }
        store.saveAllInstance();
    }

    private static String getPath(String... fileNames) {
        String names = Arrays.stream(fileNames).collect(Collectors.joining("/"));
        return context.getFilesDir() +"/"+ names;
    }

    public static User getInStateUser(String userId) {
        if(userId.hashCode() == User.SPLITALL.getId().hashCode()) {
            return User.SPLITALL;
        }
        return users.get(userId);
    }

    public static void getUser(String userId, Callback<User> cb) {
        new LoadRacer<User>(cb, users, User.class, USERSFOLDER, userId);
    }

    public static Group getInStateGroup(String groupId) {
        return groups.get(groupId);
    }

    @Nullable
    public static Group getGroupByName(String name) {
        for(Group g: groups.values()) {
            if(g.getName().equals(name)) {
                return g;
            }
        }
        return null;
    }

    public static Collection<Group> getGroups() {
        return groups.values();
    }

    public static void getGroup(String groupId, Callback<Group> cb) {
        new LoadRacer<Group>(cb, groups, Group.class,GROUPSFOLDER,  groupId);
    }

    public static void save(Savable obj) {
        // start task to save offline
        new SaveOffline((f, v) -> Log.d("USSR", "Offline save: "+f), obj).execute(obj.collection(), obj.getId());
        // start task to save online
        //new SaveOnline((f, v) -> Log.d("USSR", "Online save: "+f), obj).execute(obj.collection(), obj.getId());
    }

    @Override
    public void run() {
        while(true) { // perfect programming style ofcourse
            connect();

            try {
                while(true) {
                    String line = in.readLine();
                    JSONObject obj = new JSONObject(line);
                    switch (obj.getString("type")) {
                        case "user":
                            User u = new User(obj);
                            users.put(u.getId(), u);
                            break;
                        case "group":
                            Group g = new Group(obj);
                            groups.put(g.getId(), g);
                            break;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public interface Callback<T> {
        void call(boolean succes, T item);
    }

    private static boolean isConnected() {
        return false;
        //NetworkInfo ni = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        //return ni != null && ni.isConnected();
    }

    private static abstract class MyTask<T> extends AsyncTask<String, Void, T> {
        private Callback<T> cb;
        protected MyTask(Callback<T> call) {
            super();
            cb = call;
        }

        @Override
        protected void onPostExecute(T item) {
            super.onPostExecute(item);
            cb.call(item != null, item);
        }
    }

    // If online load failes, you don't want to call the callback
    // saves object in the map
    private static class LoadRacer<T> implements Callback<T> {
        private List<AsyncTask<String, Void, T>> tasks;
        private Callback<T> cb;
        private boolean hadSucces;
        private String[] params;
        private Map<String, T> map;

        public LoadRacer(Callback<T> cb, Map<String, T> localMap, Class<? extends T> t, String... params) {
            tasks = new ArrayList<>();
            this.params = params;
            map = localMap;

            T out = localMap.get(params[1]);
            if(out != null) {
                cb.call(true, out);
                hadSucces = true;
            }else {
                hadSucces = false;
            }

            this.cb = cb;

            tasks.add(new LoadOffline<>(this));
            //tasks.add(new LoadOnline<T>(this, t));

            tasks.forEach(ta -> ta.execute(params));
        }

        @Override
        public void call(boolean succes, T item) {
            if(succes) {
                map.put(params[1], item);
                cb.call(true, item);
                hadSucces = true;
            }else{
                boolean wasLastTask =
                        tasks.stream().allMatch(t -> t.getStatus().equals(AsyncTask.Status.FINISHED));
                // no succes, if it was last task, return no succes

                cb.call(false, null);
            }
        }
    }

    private static class LoadOffline<T> extends MyTask<T> {

        protected LoadOffline(Callback<T> call) {
            super(call);
        }

        @Override
        protected T doInBackground(String... strings) {
            T out = null;
            try {
                File f = new File(getPath(strings));
                try (FileInputStream fin = new FileInputStream(f)) {
                    ObjectInputStream ois = new ObjectInputStream(fin);
                    out = (T) ois.readObject();
                } catch (IOException e) {
                    Log.d("USSR", "OFfline fail "+e.toString());
                    //System.err.println("could not close file");
                    //e.printStackTrace();
                    // unhandled exception
                }
            }catch (ClassNotFoundException fs) {
                Log.d("USSR", "Classnotfound: "+fs.getMessage());
                //System.err.println("could not find class");
                //fs.printStackTrace();
                // unhandled exception
            }catch (Exception e) {
                Log.d("USSR", "EXCEPTION "+e.getMessage());
                //e.printStackTrace();
            }

            if(out == null) {
                Log.d("USSR", "Failed loading offline");
                cancel(true);
            }

            return out;
        }
    }

    private static class LoadOnline<T> extends AsyncTask<String, Void, T> {
        Class<? extends T> type;
        Callback<T> c;
        protected LoadOnline(Callback<T> call, Class<? extends T> t) {
            super();
            c = call;
            type = t;
        }

        @Override
        protected T doInBackground(String... strings) {
            c.call(false, null);

            return null;
        }
    }

    private static abstract class Save extends MyTask<Void> {
        protected Serializable o;
        protected Save(Callback<Void> call, Serializable o) {
            super(call);
            this.o = o;
        }
    }

    private static class SaveOffline extends Save {
        protected SaveOffline(Callback<Void> call, Serializable o) {
            super(call, o);
        }

        @Override
        protected Void doInBackground(String... strings) {
            File f = new File(getPath(strings));
            try(FileOutputStream fos = new FileOutputStream(f)) {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(o);
            } catch (Exception e) {
                Log.d("USSR", "Failed saving "+e.getMessage());
                cancel(false);
                // Unhandled exception
            }
            return null;
        }
    }

    private static class SaveOnline extends Save {

        protected SaveOnline(Callback<Void> call, Serializable o) {
            super(call, o);
        }

        @Override
        protected Void doInBackground(String... strings) {
            return null;
        }
    }
}
