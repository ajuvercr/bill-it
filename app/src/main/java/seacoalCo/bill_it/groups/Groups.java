package seacoalCo.bill_it.groups;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.logics.user.User;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Groups.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Groups#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Groups extends Fragment {
    private RecyclerView mGroupsView;
    private GroupAdapter mGroupAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    private OnFragmentInteractionListener mListener;

    public Groups() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Groups.
     */
    // TODO: Rename and change types and number of parameters
    public static Groups newInstance() {
        Groups fragment = new Groups();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGroupAdapter.modifyData(User.getLoggedInUser().getGroups());
        mGroupAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        mGroupsView = view.findViewById(R.id.groupsView);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mGroupsView.setLayoutManager(mLayoutManager);

        mGroupAdapter = new GroupAdapter(User.getLoggedInUser().getGroups(), getContext());
        mGroupsView.setAdapter(mGroupAdapter);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mGroupAdapter != null) {
            mGroupAdapter.modifyData(User.getLoggedInUser().getGroups());
            mGroupAdapter.notifyDataSetChanged();
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
