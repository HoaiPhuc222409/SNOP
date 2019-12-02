package com.example.snop;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.snop.adapters.AdapterUsers;
import com.example.snop.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {

    RecyclerView userList;
    AdapterUsers adapterUsers;
    List<ModelUser> UserList;

    FirebaseAuth mAuth;


    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        userList = view.findViewById(R.id.user_list);
        userList.setHasFixedSize(true);
        userList.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAuth = FirebaseAuth.getInstance();

        //send Friend request




        ////////////////////////////////////////////////

        UserList = new ArrayList<>();

        getAllUsers();


        return view;
    }


    private void getAllUsers() {
        //get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //get path of database name
        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference("Users");

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    if (!modelUser.getUid().equals(fUser.getUid())) {
                        UserList.add(modelUser);
                    }

                    adapterUsers = new AdapterUsers(getActivity(), UserList);
                    userList.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

       MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query.trim())){
                    searchUsers(query);
                }else{
                    getAllUsers();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if(!TextUtils.isEmpty(query.trim())){
                    searchUsers(query);
                }else{
                    getAllUsers();
                }

                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void searchUsers(final String query){
        //get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //get path of database name
        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference("Users");

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    if (!modelUser.getUid().equals(fUser.getUid())) {

                        if(modelUser.getName().toLowerCase().contains(query.toLowerCase())||
                        modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            UserList.add(modelUser);
                        }

                    }

                    adapterUsers = new AdapterUsers(getActivity(), UserList);
                    adapterUsers.notifyDataSetChanged();
                    userList.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            mAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {

        } else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}
