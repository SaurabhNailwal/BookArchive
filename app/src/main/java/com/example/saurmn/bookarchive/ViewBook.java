package com.example.saurmn.bookarchive;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by SaurabhMN on 4/15/2015.
 */
public class ViewBook extends Activity{

    private long rowId; // selected book's id
    private TextView isbnTextView;
    private TextView titleTextView;
    private TextView editionTextView;
    private TextView yearPublishedTextView;

    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_book); //inflating UI

        isbnTextView = (TextView) findViewById(R.id.isbnTextView);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        editionTextView = (TextView) findViewById(R.id.editionTextView);
        yearPublishedTextView = (TextView) findViewById(R.id.yearPublishedTextView);

        //get the selected books row Id
        Bundle extras = getIntent().getExtras();
        rowId = extras.getLong(BookList.ROW_ID);
    }

    protected void onResume(){
        super.onResume();

        new LoadBookTask().execute(rowId);
    }

    //performs dB query outside GUI thread
    private class LoadBookTask extends AsyncTask<Long, Object, Cursor>{

       DatabaseConnector databaseConnector = new DatabaseConnector(ViewBook.this);

        // perform the dB access
        @Override
        protected Cursor doInBackground(Long... params) {

            databaseConnector.open();
            return databaseConnector.getOneBook(params[0]);
        }

        // use the Cursor returned from the doInBackground method
        protected void onPostExecute(Cursor result){
            super.onPostExecute(result);
            result.moveToFirst(); // move to the first item

            // get the column index for each data item
            int isbnIndex = result.getColumnIndex("isbn");
            int titleIndex = result.getColumnIndex("title");
            int editionIndex = result.getColumnIndex("edition");
            int yearPublishedIndex = result.getColumnIndex("yearPublished");

            //fill the TextViews with the retrieved data
            isbnTextView.setText(result.getString(isbnIndex));
            titleTextView.setText(result.getString(titleIndex));
            editionTextView.setText(result.getString(editionIndex));
            yearPublishedTextView.setText(result.getString(yearPublishedIndex));

            result.close();
            databaseConnector.close();
        }

    }// end of class LoadBookTask

    //create the Activity's menu from a menu resource XML file
    public  boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_book, menu);
        return true;
    }

    //handle choice from options menu
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.editItem:
                //intent to launch the AddEditBook Activity
                Intent addEditBook = new Intent(this, AddEditBook.class);

                //pass the selected book's data
                addEditBook.putExtra(BookList.ROW_ID, rowId);
                addEditBook.putExtra("isbn",isbnTextView.getText());
                addEditBook.putExtra("title",titleTextView.getText());
                addEditBook.putExtra("edition",editionTextView.getText());
                addEditBook.putExtra("yearPublished",yearPublishedTextView.getText());

                startActivity(addEditBook); // start Activity
                return true;
            case R.id.deleteItem:
                deleteBook(); //delete the displayed book
                return true;
            case R.id.specifyBookAuthorsItem:
                // select dialog for selecting authors
                showAuthorsToChoose();
                return true;
            case R.id.returnToMainItem:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteBook(){

        AlertDialog.Builder builder = new AlertDialog.Builder(ViewBook.this);
        builder.setTitle("Are You Sure?");
        builder.setMessage("This will permanently delete the book record");

        builder.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final DatabaseConnector databaseConnector = new DatabaseConnector(ViewBook.this);

                // AsyncTask to delete book in dB using another thread and then call finish after deletion
                AsyncTask<Long, Object, Object> deleteBookTask = new AsyncTask<Long, Object, Object>(){

                    protected Object doInBackground(Long... params)
                    {
                        databaseConnector.deleteBook(params[0]);
                        return null;
                    }

                    protected void onPostExecute(Object result)
                    {
                        finish(); // return to the BookList Activity
                    }
                };

                // execute the AsyncTask to delete contact at rowID
                deleteBookTask.execute(new Long[] { rowId });

            }
        });// end of call to method setPositiveButton

        builder.setNegativeButton("Cancel", null);
        builder.show(); // display the Dialog
    }


    private void showAuthorsToChoose(){

        final DatabaseConnector dbConn = new DatabaseConnector(this);
        final Cursor cursor = dbConn.getAuthorsForBook(rowId);

        final Map<String, Long> authorMap = new HashMap<String, Long>();
        //store cursor values
        if (cursor != null ) {
            if  (cursor.moveToFirst()) {
                do {
                    authorMap.put(cursor.getString(cursor.getColumnIndex("fullName")), cursor.getLong(cursor.getColumnIndex("_id")));
                }while (cursor.moveToNext());
            }
        }

        // get array of world regions
        final String[] authorNames =
                authorMap.keySet().toArray(new String[authorMap.size()]);

        // boolean array representing whether each region is enabled
        boolean[] authorsSelected = new boolean[authorMap.size()];
        final ArrayList<Long> authorsSelectedIds = new ArrayList<Long>();

        // create an AlertDialog Builder and set the dialog's title
        AlertDialog.Builder booksBuilder =
                new AlertDialog.Builder(this);
        booksBuilder.setTitle("Specify the Book's Authors");

        // add bookTitles to the Dialog and set the behavior
        // when one of the items is clicked
        booksBuilder.setMultiChoiceItems(
                authorNames, authorsSelected,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        // include or exclude the clicked book title
                        // depending on whether or not it's checked
                        if (isChecked) {

                            authorsSelectedIds.add(authorMap.get(authorNames[which]));

                        }

                    } // end method onClick
                } // end anonymous inner class
        ); // end call to setMultiChoiceItems

        // resets quiz when user presses the "Reset Quiz" Button
        booksBuilder.setPositiveButton("Submit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        // insert in book author table
                        for (int i = 0; i < authorsSelectedIds.size(); i++) {
                            dbConn.insertBookAuthor(rowId, authorsSelectedIds.get(i));
                        }

                    } // end method onClick
                } // end anonymous inner class
        ); // end call to method setPositiveButton

        // create a dialog from the Builder
        AlertDialog regionsDialog = booksBuilder.create();
        regionsDialog.show(); // display the Dialog

    }
    
}
