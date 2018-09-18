package seacoalCo.bill_it.logics.user;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import seacoalCo.bill_it.logics.Savable;
import seacoalCo.bill_it.logics.Store;

public class User implements Savable {

    public static final User SPLITALL = new User() {
        private static final long serialVersionUID = 5996765665284145106L;

        @Override
        public String collection() {
            return "users";
        }

        @Override
        public String getId() {
            return "SPLITALL";                      // universal SPLITALL id
        }

        @Override
        public String getName() {
            return "Divide";
        }

        @Override
        public String getEmail() {
            return "";
        }
    };

    private static User currentUser;
    public static User getCurrentUser() {
        return currentUser;
    }
    private static final long serialVersionUID = 7977742117081942833L;
    private static User loggedInUser = null;
    public static User getLoggedInUser() { return loggedInUser; }
    public static void setLoggedInUser(User u) {
        loggedInUser = u;
    }

    protected String name;
    protected String email;
    protected String id;
    protected List<String> friends, groups;

    public User(String name, String email, String id) {
        if (id == null) {
            id = Store.randomAlphaNumeric(10);
        }
        this.name = name;
        this.email = email;
        this.friends = new ArrayList<>();
        this.friends.add(id);
        this.groups = new ArrayList<>();
        this.id = id;
        Store.save(this);
    }

    public User(JSONObject obj) throws JSONException {
        name = obj.getString("name");
        email = obj.getString("email");

        JSONArray fs = obj.getJSONArray("friends");
        friends = new ArrayList<>();
        for (int i = 0; i < fs.length(); i++) {
            friends.add( fs.getString(i));
        }

        JSONArray gs = obj.getJSONArray("groups");
        groups = new ArrayList<>();
        for (int i = 0; i < gs.length(); i++) {
            groups.add( gs.getString(i));
        }
    }

    public User() {
        friends = new ArrayList<>();
        groups = new ArrayList<>();
    }

    public void setCurrent() {
        currentUser = this;
    }

    public String getId() {
        return id;
    }

    public String collection() {
        return "users";
    }

    public List<String> getGroups() {
        return groups;
    }

    public String getName() {
        return name;
    }
    public String getEmail() { return email; }

    public List<String> getFriends() {
        return friends;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void addFriend(String friendId) {
        boolean good = true;
        for (String f : friends) {
            if (f.equals(friendId))
                good = false;
        }
        if(good)
            friends.add(friendId);
        Store.save(this);
    }

    public void addGroup(String groupId) {
        boolean good = true;
        for (String g : groups) {
            if (g.equals(groupId))
                good = false;
        }
        if(good)
            groups.add(groupId);
        Store.save(this);
    }

    public String toString() {
        String out = getName() + " " + getEmail() +" "+getId()+"\nGroups";
        for(String g: groups) {
            out += "\n\t"+g;
        }
        out += "\nFriends";
        for(String f: friends) {
            out += "\n\t"+f;
        }
        return out;
    }
}
