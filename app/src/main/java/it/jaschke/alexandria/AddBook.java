package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = AddBook.class.getSimpleName();
    private EditText ean;
    private InputMethodManager inputManager;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT="eanContent";
    private final String SAVED_ISBN = "isbn_saved";
    public static final String SCAN_EAN_KEY = "key_ean_scan";
    public static final String SCAN_RESULT_FORMAT = "EAN_13";

    private final String ISBN_13_START = "978";

    private String isbnString = "";
    private final String DIGIT_REGEX = "[0-9]+";

    public AddBook(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(ean!=null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
        outState.putString(SAVED_ISBN, isbnString);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);
        inputManager = (InputMethodManager) getActivity().getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.v(TAG, "s = " + s);

                if (s.length() > 0 && !s.toString().matches(DIGIT_REGEX)) {
                    Log.v(TAG, "afterTextChanged: ISBN NOT NUMERIC");
                    Util.displayNeutralAlert(getActivity(),
                            getString(R.string.isbn_numeric_error_title),
                            getString(R.string.isbn_numeric_error_message));
                    clearFields();
                    return;
                }
                if (MainActivity.configChange) {
                    Log.v(TAG, "Configuration change detected !");
                    MainActivity.configChange = false;
                    return;
                } else {
                    Log.v(TAG, "Not sreen rotation ! Check if need to fetch book.");
                    if (s.length() == 10 || s.length() == 13) {
                        if (s.length() == 10 && !s.toString().startsWith(ISBN_13_START)) {
                            isbnString = ISBN_13_START + s.toString();
                        } else if (s.length() == 13) {
                            isbnString = s.toString();
                        } else {
                            return;
                        }
                        //Once we have an ISBN, start a book intent
                        Intent bookIntent = new Intent(getActivity(), BookService.class);
                        bookIntent.putExtra(BookService.EAN, isbnString);
                        bookIntent.setAction(BookService.FETCH_BOOK);
                        getActivity().startService(bookIntent);
                        AddBook.this.restartLoader();
                    } else if (s.length() == 0)
                        clearFields();
                }
            }
        });

        ean.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.v(TAG, "Keycode = " +keyCode);
                if (event.getAction() == KeyEvent.ACTION_UP &&
                        keyCode == KeyEvent.KEYCODE_ENTER) {
                    String isbnText = null;
                    EditText et = (EditText) v;
                    if (et != null) {
                        isbnText = (String) et.getText().toString();
                    }
                    handleNextPressed(isbnText);
                    return true;
                }

                return false;
            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.
                //when you're done, remove the toast below.
                /*Context context = getActivity();
                CharSequence text = "This button should let you scan a book for its barcode!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();*/
                Intent scanIntent = new Intent(getActivity(), ScanActivity.class);
                if (scanIntent.resolveActivity(getActivity().getPackageManager()) != null)
                    startActivityForResult(scanIntent, 2);
                else
                    Log.v(TAG, "Something is wrong when start SCAN INTENT ! ");

            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ean.setText("");
                clearFields();
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, isbnString);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
                clearFields();
            }
        });

        if(savedInstanceState!=null){
            isbnString = savedInstanceState.getString(SAVED_ISBN);
            //ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
            if (isbnString!= null) {
                if (isbnString.length() == 10 && !isbnString.startsWith(ISBN_13_START)) {
                    isbnString = ISBN_13_START + isbnString;
                }
                if (isbnString.length() == 13) {
                    AddBook.this.restartLoader();
                }
            }
        }

        if (getActivity() != null ) {
            getActivity().setTitle(R.string.scan);
        }

        return rootView;
    }

    public void handleNextPressed (String text) {
        if (text != null) {
            if (text.length() == 10 || text.length() == 13) {
                if (text.length() == 10 && text.startsWith(ISBN_13_START))
                    Util.displayToast(getActivity(), getString(R.string.isbn_13_digits_error_toast));
                else {
                    // Dismiss the keyboard
                    try {
                        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus()
                                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    } catch (NullPointerException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                Util.displayToast(getActivity(), getString(R.string.isbn_length_error_toast));
            }
        }
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(ean.getText().length()==0){
            return null;
        }
        /*String eanStr= ean.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith("978")){
            eanStr="978"+eanStr;
        }*/
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(isbnString)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            clearFields();
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors != null) {
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        }else {
            ((TextView) rootView.findViewById(R.id.authors)).setText(getString(R.string.no_author));
        }
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields(){
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("MAIN ACTIVITY", "onActivity Result !");
        if (requestCode == 2) {
            String ean = data.getStringExtra(SCAN_EAN_KEY);
            Log.v("MAINACTIVITY", ean);
            ((EditText) rootView.findViewById(R.id.ean)).setText(ean);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause: start");
    }


}
