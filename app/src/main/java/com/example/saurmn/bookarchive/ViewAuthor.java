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
public class ViewAuthor extends Activity {

    private long rowId; // selected author's id
    private TextView firstNameTextView;
    private TextView lastNameTextView;

    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_author); //inflating UI

        firstNameTextView = (TextView) findViewById(R.id.firstNameTextView);
        lastNameTextView = (TextView) findViewById(R.id.lastNameTextView);

        //get the selected books row Id
        Bundle extras = getIntent().getExtras();
        rowId = extras.getLong(BookList.ROW_ID);
    }

    protected void onResume(){
        super.onResume();
        new LoadAuthorTask().execute(rowId);
    }

    //performs dB query outside GUI thread
    private class LoadAuthorTask extends AsyncTask<Long, Object, Cursor> {

        DatabaseConnector databaseConnector = new DatabaseConnector(ViewAuthor.this);

        // perform the dB access
        @Override
        protected Cursor doInBackground(Long... params) {

            databaseConnector.open();
            return databaseConnector.getOneAuthor(params[0]);
        }

        // use the Cursor returned from the doInBackground method
        protected void onPostExecute(Cursor result){
            super.onPostExecute(result);
            result.moveToFirst(); // move to the first item

            // get the column index for each data item
            int firstNameIndex = result.getColumnIndex("firstName");
            int lastNameIndex = result.getColumnIndex("lastName");

            //fill the TextViews with the retrieved data
            firstNameTextView.setText(result.getString(firstNameIndex));
            lastNameTextView.setText(result.getString(lastNameIndex));

            result.close();
            databaseConnector.close();
        }

    }// end of class LoadAuthorTask

    //create the Activity's menu from a menu resource XML file
    public  boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_author, menu);
        return true;
    }

    //handle choice from options menu
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.editItem:
                //intent to launch the AddEditBook Activity
                Intent addEditAuthor = new Intent(this, AddEditAuthor.class);

                //pass the selected author's data
                addEditAuthor.putExtra(BookList.ROW_ID, rowId);
                addEditAuthor.putExtra("firstName", firstNameTextView.getText());
                addEditAuthor.putExtra("lastName", lastNameTextView.getText());

                startActivity(addEditAuthor); // start Activity
                return true;

            case R.id.deleteItem:
                deleteAuthor(); //delete the displayed author
                return true;

            case R.id.specifyAuthorBooksItem:
                // select dialog for selecting books
                showBooksToChoose();
                return true;

            case R.id.returnToMainItem:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAuthor(){

        AlertDialog.Builder builder = new AlertDialog.Builder(ViewAuthor.this);
        builder.setTitle("Are You Sure?");
        builder.setMessage("This will permanently delete the author record");

        builder.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final DatabaseConnector databaseConnector = new DatabaseConnector(ViewAuthor.this);

                        // AsyncTask to delete author in dB using another thread and then call finish after deletion
                        AsyncTask<Long, Object, Object> deleteAuthorTask = new AsyncTask<Long, Object, Object>() {

                            protected Object doInBackground(Long... params) {
                                databaseConnector.deleteAuthor(params[0]);
                                return null;
                            }

                            protected void onPostExecute(Object result) {
                                finish(); // return to the BookList Activity
                            }
                        };

                        // execute the AsyncTask to delete contact at rowID
                        deleteAuthorTask.execute(new Long[]{rowId});
                    }
                });// end of call to method setPositiveButton

        builder.setNegativeButton("Cancel", null);
        builder.show(); // display the Dialog
    }

    private void showBooksToChoose(){

        final DatabaseConnector dbConn = new DatabaseConnector(this);
        final Cursor cursor = dbConn.getBooksForAuthor(rowId);

        final Map<String, Long> bookTitleMap = new HashMap<String, Long>();
        //store cursor values
        if (cursor != null ) {
            if  (cursor.moveToFirst()) {
                do {
                    bookTitleMap.put(cursor.getString(cursor.getColumnIndex("title")),cursor.getLong(cursor.getColumnIndex("_id")));
                }while (cursor.moveToNext());
            }
        }

        // get array of world regions
        final String[] bookTitles =
                bookTitleMap.keySet().toArray(new String[bookTitleMap.size()]);

        // boolean array representing whether each region is enabled
        boolean[] booksSelected = new boolean[bookTitleMap.size()];
        final ArrayList<Long> booksSelectedIds = new ArrayList<Long>();

        // create an AlertDialog Builder and set the dialog's title
        AlertDialog.Builder authorsBuilder =
                new AlertDialog.Builder(this);
        authorsBuilder.setTitle("Specify the Author's Books");

        // add bookTitles to the Dialog and set the behavior
        // when one of the items is clicked
        authorsBuilder.setMultiChoiceItems(
                bookTitles, booksSelected,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        // include or exclude the clicked book title
                        // depending on whether or not it's checked
                        if (isChecked) {
                            booksSelectedIds.add(bookTitleMap.get(bookTitles[which]));
                        }
                    } // end method onClick
                } // end anonymous inner class
        ); // end call to setMultiChoiceItems

        // resets quiz when user presses the "Reset Quiz" Button
        authorsBuilder.setPositiveButton("Submit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        // insert in book author table
                        for (int i = 0; i < booksSelectedIds.size(); i++) {
                            dbConn.insertBookAuthor(booksSelectedIds.get(i), rowId);
                        }

                    } // end method onClick
                } // end anonymous inner class
        ); // end call to method setPositiveButton

        // create a dialog from the Builder
        AlertDialog regionsDialog = authorsBuilder.create();
        regionsDialog.show(); // display the Dialog

    }
}
