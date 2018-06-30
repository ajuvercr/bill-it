package seacoalCo.bill_it;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import seacoalCo.bill_it.camera.google.OcrCaptureActivity;
import seacoalCo.bill_it.groups.UserAdapter;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.group.Group;
import seacoalCo.bill_it.mail.Mail;
import seacoalCo.bill_it.utility_classes.TutorialBuilder;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OverViewActivity.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OverViewActivity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OverViewActivity extends Fragment {

    private interface SetButton {
        void change(int count);
    }

    private UserAdapter usersA;
    private RecyclerView usersRv;
    private Group g;
    private EditText txtName;
    private SharedPreferences preferences;
    private Context mListener;

    private SetButton changeButton = (e) -> {};
    private Button sendToAll;

    public OverViewActivity() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        Store.save(g);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Friends.
     */
    // TODO: Rename and change types and number of parameters
    public static OverViewActivity newInstance() {
        OverViewActivity fragment = new OverViewActivity();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void update() {
        g = Group.getCurrentGroup();
        if(usersA != null)
            usersA.notifyDataSetChanged();

        if(txtName != null) {
            txtName.setText(g.getName());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_group_overview, container, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        g = Group.getCurrentGroup();

        sendToAll = view.findViewById(R.id.send);
        sendToAll.setOnClickListener((v)->sendEmailtoAll());

        usersRv = view.findViewById(R.id.users_rv);
        usersA = new UserAdapter(
                new ArrayList<>(g.getUserIds())
        );

        usersA.setAddFriends(
                () -> {
                    Intent i = new Intent(getActivity().getApplicationContext(), AddFriendsActivity.class);
                    startActivityForResult(i, 22);
                }
        );

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        usersRv.setLayoutManager(layoutManager);
        usersRv.setAdapter(usersA);

        ItemTouchHelper swipeDeleteHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Behaviour definiëren.
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int deletedPos = viewHolder.getAdapterPosition();
                String userId = usersA.getItem(deletedPos);
                if(userId.equals(UserAdapter.addMemberString)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity().getApplicationContext());
                    builder.setTitle("Nope");
                    builder.setMessage("Can't delete Add Member option");
                    builder.setNeutralButton("*Sad face*", (dialog, which) -> dialog.dismiss());
                }else if (userId.equals(Group.getCurrentGroup().getOwnerId()) ||
                        userId.equals(preferences.getString(getString(R.string.user_id), ""))) {
                    usersA.deleteUserFromGroup(deletedPos);
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity().getApplicationContext());
                    builder.setTitle("Nope");
                    builder.setMessage(R.string.remove_from_group_error);
                    builder.setNeutralButton("*Sad face*", (dialog, which) -> dialog.dismiss());
                    builder.create();
                }
            }
        });

        swipeDeleteHelper.attachToRecyclerView(usersRv);

        txtName = view.findViewById(R.id.addGroupName);
        txtName.setText(g.getName());

        txtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                g.setName(txtName.getText().toString());
            }
        });

        view.findViewById(R.id.fab).setOnClickListener((e) -> {
            Intent intent = new Intent(getActivity(), OcrCaptureActivity.class);
            getActivity().startActivity(intent);
        });

        view.findViewById(R.id.add_item).setOnClickListener((e) ->{
            AddItemFragment dialog = new AddItemFragment();
            dialog.setOnEnded(() -> changeButton.change(g.getItems().size()));
            dialog.show(getActivity().getSupportFragmentManager(), "NoticeDialogFragment");
        });

        view.findViewById(R.id.divide_items).setOnClickListener((e) -> {
            if(g.getItems().isEmpty()) {

            }else {
                Intent intent = new Intent(getActivity(), SplitActivity.class);
                getActivity().startActivity(intent);
            }
        });
        changeButton = (e) -> ((Button) view.findViewById(R.id.divide_items)).setText(getResources().getQuantityString(R.plurals.divide_items, e, e));

        changeButton.change(g.getItems().size());

        if (PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(getString(R.string.group_tutorial), true) && savedInstanceState == null) {
            AlertDialog alertDialog = TutorialBuilder.buildTutorial(R.string.group_tutorial, getString(R.string.group_tutorial_message), getContext());
            alertDialog.show();
        }

        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ArrayList<String> uis = new ArrayList<>(g.getUserIds());
        usersA.setContent(uis);
        usersA.notifyDataSetChanged();
        usersRv.setAdapter(usersA);
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {

            Group group = Group.getCurrentGroup();

            // list of expenses the user consumed, or payed
            for (String userid:group.getUserIds()) {
                group.equalize(userid);
            }
            usersA.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onResume() {
        if(g != null)
            changeButton.change(g.getItems().size());
        update();
        super.onResume();
    }

    public void sendEmailtoAll(){
        Collection<String>userIds =g.getUserIds();
        String[] emails = new String[userIds.size()];
        int i = 0;
        for (String userId:userIds) {
            emails[i] = Store.getInStateUser(userId).getEmail();
            i++;
        }
        Mail mail = new Mail(emails,
                "Bill repartition from Bill-it", "Hello,\n" +
                "According to Bill-it:\n"
                + generateContent(userIds) + "\nBest Regards.");
        for(String userid:userIds){
            g.equalize(userid);
        }
        startActivity(mail.getIntentForSending());
    }

    private String generateContent(Collection<String>userids){
        String content = "";
        HashMap<String, Integer> credits = g.getCredits();
        for(String userid: userids){
            String line = "- " + Store.getInStateUser(userid).getName() + " has to ";
            double credit = credits.getOrDefault(userid,0) / 100.0;
            if(credit < 0){
                line += "pay €" + String.valueOf(-credit);
            }
            else if(credit > 0){
                line += "recieve €" + String.valueOf(credit);
            }else{
                line += "do nothing";
            }
            content += line +"\n";
        }
        return content;
    }
}
