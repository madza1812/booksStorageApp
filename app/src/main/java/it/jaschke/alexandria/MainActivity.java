package it.jaschke.alexandria;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import it.jaschke.alexandria.api.Callback;


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, Callback {

    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment navigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;
    public static boolean IS_TABLET = false;
    public static boolean IS_TABLET_AND_LANDSCAPE = false;
    private BroadcastReceiver messageReceiver;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";
    public static final String OLD_BOOK_FOUND = "found_book_old";
    public static final String NO_CONNECTION = "no_internet";
    public static final String NEW_BOOK_NOT_FOUND = "not_found_new_book";

    public static boolean configChange = false;
    public static String lastBookISBN;
    public final String SAVE_LAST_BOOK_ISBN = "isbn_book_last_save";



    public int initialOrientation, currentOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate: Activity = " + MainActivity.this.toString());
        initialOrientation = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        IS_TABLET = isTablet();
        IS_TABLET_AND_LANDSCAPE = isTabletAndLandscape();
        Log.v(TAG, "onCreate TEST: isTab = " + IS_TABLET + ", isTabAndLand = " + IS_TABLET_AND_LANDSCAPE);
        if(IS_TABLET){
            setContentView(R.layout.activity_main_tablet);
        }else {
            setContentView(R.layout.activity_main);
        }

        messageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,filter);

        navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment;
        String tag;

        switch (position){
            default:
            case 0:
                tag = ListOfBooks.TAG;
                nextFragment = fragmentManager.findFragmentByTag(ListOfBooks.TAG);
                if (nextFragment == null)
                    nextFragment = new ListOfBooks();
                break;
            case 1:
                tag = AddBook.TAG;
                nextFragment = fragmentManager.findFragmentByTag(AddBook.TAG);
                if (nextFragment == null)
                    nextFragment = new AddBook();
                break;
            case 2:
                tag = About.TAG;
                nextFragment = fragmentManager.findFragmentByTag(About.TAG);
                if (nextFragment == null)
                    nextFragment = new About();
                break;

        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment, tag)
                .addToBackStack((String) title)
                .commit();
    }

    public void setTitle(int titleId) {
        title = getString(titleId);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        currentOrientation = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        if (initialOrientation != currentOrientation)
            configChange = true;
        Log.v(TAG, "onSaveInstanceState TEST: isTAB = " + IS_TABLET + ", isTABANDLANDSCAPE = " + IS_TABLET_AND_LANDSCAPE
                + " isTabAndLand() = " + isTabletAndLandscape());
        // Screen Rotation from Portrait to Landscape
        if (IS_TABLET && !IS_TABLET_AND_LANDSCAPE) {
            Fragment detailFragment = this.getSupportFragmentManager().findFragmentByTag(BookDetail.TAG);
            Fragment listBookFragment = this.getSupportFragmentManager().findFragmentByTag(ListOfBooks.TAG);
            if (detailFragment != null && detailFragment.isVisible()) {
                Log.v(TAG, "onSaveInstanceState: old Book Detail is found by TAG ! LastBookISBN = NULL is " +
                        (lastBookISBN == null));
                outState.putString(SAVE_LAST_BOOK_ISBN, lastBookISBN);
                this.getSupportFragmentManager().popBackStack();
            }
        }
        // Screen rotation from Landscape to Portrait
        if (IS_TABLET_AND_LANDSCAPE) {
            Fragment detailFragment = this.getSupportFragmentManager().findFragmentByTag(BookDetail.TAG);
            Fragment listBookFragment = this.getSupportFragmentManager().findFragmentByTag(ListOfBooks.TAG);
            if (detailFragment != null && detailFragment.isVisible() &&
                    listBookFragment != null && listBookFragment.isVisible()) {
                Log.v(TAG, "onSaveInstanceState: old Book Detail is found by TAG ! LastBookISBN = NULL is " +
                        (lastBookISBN == null));
                outState.putString(SAVE_LAST_BOOK_ISBN, lastBookISBN);
                this.getSupportFragmentManager().popBackStack();
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null && IS_TABLET) {
            Log.v(TAG, "onRestoreInstanceState (only called on Tablet Mode).");
            if (savedInstanceState.getString(SAVE_LAST_BOOK_ISBN) != null) {
                Log.v(TAG, "onRestoreInstanceSatte: Save Last Book ISBN is NOT NULL");
                Bundle args = new Bundle();
                int id =R.id.container;
                BookDetail bookDetailFrag = new BookDetail();
                lastBookISBN =  savedInstanceState.getString(SAVE_LAST_BOOK_ISBN);

                args.putString(BookDetail.EAN_KEY,lastBookISBN);
                bookDetailFrag.setArguments(args);
                if (IS_TABLET_AND_LANDSCAPE) {
                    Log.v(TAG, "on Rotation Portrait to Landscape: Add new Detail Book Fragment to right_container!");
                    id = R.id.right_container;
                    getSupportFragmentManager().beginTransaction()
                            .replace(id, bookDetailFrag, BookDetail.TAG)
                            .addToBackStack(BookDetail.TAG)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .commit();
                } else if (IS_TABLET && !IS_TABLET_AND_LANDSCAPE) {
                    Log.v(TAG, "on Rotation Landscape to Portrait: replace new Detail Book Fragment to container");
                    getSupportFragmentManager().beginTransaction()
                            .replace(id, bookDetailFrag, BookDetail.TAG)
                            .addToBackStack(BookDetail.TAG)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .commit();
                }

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        super.onDestroy();
    }

    @Override
    public void onItemSelected(String ean) {
        Log.v(TAG, "onItemSelected: start with activity = " + MainActivity.this.toString());
        if (ean!=null) {
            Log.v(TAG, "last isbn = " + lastBookISBN);
            if (IS_TABLET_AND_LANDSCAPE && ean.equals(lastBookISBN)) {
                Log.v(TAG, "onItemselected: TAB&LAND and Same Last ISBN. Not loading the Book Detail.");
                return;
            }
            Log.v(TAG, "onItemSelected: loading new Book Detail");
            lastBookISBN = ean;
            Bundle args = new Bundle();
            args.putString(BookDetail.EAN_KEY, ean);

            BookDetail fragment = new BookDetail();
            fragment.setArguments(args);
            Log.v(TAG, "onItemSelected for new Book Detail Fragment !");

            int id = R.id.container;
            Log.v(TAG, "onItemSelected: container id = " + id);
            //if(findViewById(R.id.right_container) != null){
            if (IS_TABLET_AND_LANDSCAPE || findViewById(R.id.right_container) != null) {
                Log.v(TAG, "onItemSelected: right_container is not null !");
                id = R.id.right_container;
            }
            Log.v(TAG, "onItemSelected: container id = " + id);
            getSupportFragmentManager().beginTransaction()
                    .replace(id, fragment, BookDetail.TAG)
                    .addToBackStack(BookDetail.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    @Override
    public void onResetLASTISBN() {
        lastBookISBN = null;
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(MESSAGE_KEY);
            if(action != null){
                if ((OLD_BOOK_FOUND.equals(action)))
                    Util.displayNeutralAlert(MainActivity.this,
                            getString(R.string.added_book_title),
                            getString(R.string.added_book_message));
                else if (NO_CONNECTION.equals(action))
                    Util.displayNeutralAlert(MainActivity.this,
                            getString(R.string.no_connection_title),
                            getString(R.string.no_connection_message));
                else if (NEW_BOOK_NOT_FOUND.equals(action))
                    Util.displayNeutralAlert(MainActivity.this,
                            getString(R.string.book_not_found_title),
                            getString(R.string.book_not_found_message));
            }
        }
    }

    public void goBack(View view){
        getSupportFragmentManager().popBackStack();
    }

    private boolean isTablet() {
        /*return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;*/
        return (getApplicationContext()
                .getResources()
                .getConfiguration()
                .smallestScreenWidthDp >= 600);
    }

    public boolean isTabletAndLandscape () {
        return (IS_TABLET && (getApplicationContext().getResources().getConfiguration().orientation ==
        Configuration.ORIENTATION_LANDSCAPE));
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()<2){
            Log.v(TAG, "Back Button pressed is captured !");
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle(getString(R.string.exit_app_title))
                    .setMessage(getString(R.string.exit_app_message));
            builder.setPositiveButton(getString(R.string.pos_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(getString(R.string.neg_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
        }
        else {
            Log.v(TAG, "onBAckPressed: fragment count = " + getSupportFragmentManager().getBackStackEntryCount());
            //getSupportFragmentManager().popBackStack();
            super.onBackPressed();
        }
    }
}