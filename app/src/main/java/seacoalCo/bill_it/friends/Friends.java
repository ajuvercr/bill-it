package seacoalCo.bill_it.friends;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.logics.user.User;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Friends.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Friends#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Friends extends Fragment {
    private RecyclerView mFriendsView;
    private FriendAdapter mFriendAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    private OnFragmentInteractionListener mListener;

    public Friends() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Friends.
     */
    // TODO: Rename and change types and number of parameters
    public static Friends newInstance() {
        Friends fragment = new Friends();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //hub = (Hub) getArguments().getSerializable(ARG_HUB);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsView = view.findViewById(R.id.friendsView);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mFriendsView.setLayoutManager(mLayoutManager);

        mFriendAdapter = new FriendAdapter(User.getLoggedInUser().getFriends(), getContext());
        mFriendsView.setAdapter(mFriendAdapter);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onResume() {
        Log.d("USSR", "I resumed, friends");
        super.onResume();
        mFriendAdapter.modifyData(User.getLoggedInUser().getFriends());
        mFriendAdapter.notifyDataSetChanged();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
