package com.example.saurmn.bookarchive;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by SaurabhMN on 4/15/2015.
 */
public class AddEditBook extends Activity{
    private long rowId; // id of book being edited

    //EditTexts for book information
    private EditText isbnEditText;
    private EditText titleEditText;
    private EditText editionEditText;
    private EditText yearPublished;

    //called when the Activity is first started
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit_book); // inflating UI

        isbnEditText = (EditText)findViewById(R.id.isbnEditText);
        titleEditText  = (EditText)findViewById(R.id.titleEditText);
        editionEditText  = (EditText)findViewById(R.id.editionEditText);
        yearPublished  = (EditText)findViewById(R.id.yearPublishedEditText);

        Bundle extras = getIntent().getExtras();// Bundle of extras

        //if extras are present then populate
        if(extras != null){
            rowId = extras.getLong("row_id");
            isbnEditText.setText(extras.getString("isbn"));
            titleEditText.setText(extras.getString("title"));
            editionEditText.setText(extras.getString("edition"));
            yearPublished.setText(extras.getString("yearPublished"));
        }

        Button saveBookButton = (Button) findViewById(R.id.saveBookButton);
        saveBookButton.setOnClickListener(saveBookButtonClicked);
    }

    OnClickListener saveBookButtonClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isbnEditText.getText().length() != 0 && titleEditText.getText().length() != 0 && editionEditText.getText().length() != 0 && yearPublished.getText().length() != 0){
                AsyncTask<Object, Object, Object> saveBookTask = new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... params) {
                        saveBook(); // save the book details to dB
                        return null;
                    }

                    protected void onPostExecute(Object result){
                        finish(); // return to previous Activity
                    }
                };// end of AsyncTask

                //saving the book details to dB using separate thread
                saveBookTask.execute((Object[]) null);

            }else{
                //no value for either 'isbn' or 'title' or 'edition' or 'yearPublished'
                AlertDialog.Builder builder = new AlertDialog.Builder(AddEditBook.this);

                builder.setTitle("Book Details Missing");
                builder.setMessage("Book details cannot be blank. Please enter a value");
                builder.setPositiveButton("OK", null);
                builder.show(); // display the Dialog
            }

        }
    };// end of OnClickListener saveBookButtonClicked

    private void saveBook(){
        //connector to interact with SQLite dB
        DatabaseConnector databaseConnector = new DatabaseConnector(this);

        if(getIntent().getExtras() == null){
            //insert a new record
            databaseConnector.insertBook(isbnEditText.getText().toString(),
                    titleEditText.getText().toString(),
                    editionEditText.getText().toString(),
                    yearPublished.getText().toString());

        }else{
            //update an existing record
            databaseConnector.updateBook(rowId,
                    isbnEditText.getText().toString(),
                    titleEditText.getText().toString(),
                    editionEditText.getText().toString(),
                    yearPublished.getText().toString());
        }
    }// end of saveBook() method
}
