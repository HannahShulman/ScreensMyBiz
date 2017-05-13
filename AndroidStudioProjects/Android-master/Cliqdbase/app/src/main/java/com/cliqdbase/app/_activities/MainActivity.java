package com.cliqdbase.app._activities;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.cliqdbase.app.R;
import com.cliqdbase.app._fragments.MainFragment;
import com.cliqdbase.app.constants.IntentConstants;
import com.cliqdbase.app._fragments.ChatMainFragment;
import com.cliqdbase.app._fragments.GmapsSearchFragment;
import com.cliqdbase.app._fragments.ProfileFragment;
import com.cliqdbase.app._fragments.SearchFilterFragment;
import com.cliqdbase.app._fragments.VenueChatMainFragment;

import java.util.Stack;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    String[] drawerStringArray;

    private ActionBarDrawerToggle mDrawerToggle;

    private Stack<Integer> positionStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        positionStack = new Stack<>();

        // Navigation drawer initialization
        drawerLayout = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        drawerList = (ListView) findViewById(R.id.main_activity_drawer_list);

        drawerStringArray = getResources().getStringArray(R.array.main_activity_drawer_string_array);

        drawerList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drawerStringArray));
        drawerList.setOnItemClickListener(this);


        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() == null)
            return;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.app_name,
                R.string.app_name
                );

        drawerLayout.setDrawerListener(mDrawerToggle);


        final FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = fragmentManager.findFragmentById(R.id.main_activity_content_frame);
                toolbar.setTitle(fragment.getTag());
            }
        });

        fragmentManager.beginTransaction()
                .replace(R.id.main_activity_content_frame, new MainFragment(), "Cliqdbase")
                .commit();
        positionStack.push(-1);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {       // When an item in the navigation drawer is clicked
        Fragment fragment = null;
        String tag = "";

        if (position == positionStack.peek())     // There is no change in the drawer position - No need to do anything
            return;
        positionStack.push(position);
        switch(position) {
            case 0:
                fragment = new ChatMainFragment();
                tag = "Chats";
                break;
            case 1:
                fragment = new ProfileFragment();
                tag = "Profile";

                Bundle args = new Bundle();
                args.putInt(IntentConstants.INTENT_EXTRA_USER_ID, -1);
                fragment.setArguments(args);

                break;
            case 2:
                fragment = new GmapsSearchFragment();
                tag = "Location Test";
                break;
            case 3:
                fragment = new VenueChatMainFragment();
                tag = "Venue Chat";
                break;
            case 4:
                fragment = new SearchFilterFragment();
                tag = "Search Filter";
                break;
        }

        if (fragment != null) {
            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_activity_content_frame, fragment, tag)
                    .addToBackStack(null)
                    .commit();

            // Highlight the selected item, update the title, and close the drawer
            drawerList.setItemChecked(position, true);
            setTitle(drawerStringArray[position]);

            drawerLayout.closeDrawer(drawerList);

            invalidateOptionsMenu();
        }

    }

    @Override
    public void onBackPressed() {
        if (positionStack.peek() != -1)
            positionStack.pop();
        super.onBackPressed();
    }
}
