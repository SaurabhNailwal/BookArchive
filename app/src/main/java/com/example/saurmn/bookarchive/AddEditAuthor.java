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
public class AddEditAuthor extends Activity {
    private long rowId; // id of author being edited

    //EditTexts for author information
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    
    // called when the Activity is first started
    public void onCreate(Bundle savedInstanceState){
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit_author); // inflating UI
        
        firstNameEditText = (EditText) findViewById(R.id.firstNameEditText);
        lastNameEditText = (EditText) findViewById(R.id.lastNameEditText);

        Bundle extras = getIntent().getExtras(); // Bundle of extras

        //if extras are present then populate
        if (extras != null){
            rowId = extras.getLong("row_id");
            firstNameEditText.setText(extras.getString("firstName"));
            lastNameEditText.setText(extras.getString("lastName"));
        }

        Button saveAuthorButton = (Button) findViewById(R.id.saveAuthorButton);
        saveAuthorButton.setOnClickListener(saveAuthorButtonClicked);
    }

    OnClickListener saveAuthorButtonClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(firstNameEditText.getText().length() != 0 && lastNameEditText.length() != 0){
                AsyncTask <Object, Object, Object> saveAuthorTask = new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        saveAuthor(); // save the author details to dB
                        return null;
                    }


                    protected void onPostExecute(Object result){
                        finish(); // return to previous Activity
                    }
                };// end of AsyncTask

                //saving the author details to dB using separate thread
                saveAuthorTask.execute((Object[]) null);

            }else{
                //no value for either 'first name' or 'last name'
                AlertDialog.Builder builder = new AlertDialog.Builder(AddEditAuthor.this);

                builder.setTitle("Author Details Missing");
                builder.setMessage("Author details cannot be blank. Please enter a value");
                builder.setPositiveButton("OK", null);
                builder.show();// display the Dialog
            }
        }
    };// end of OnClickListener saveAuthorButtonClicked

    //saving the author information to dB
    private void saveAuthor(){

        //connector to interact with SQLite dB
        DatabaseConnector databaseConnector = new DatabaseConnector(this);

        if(getIntent().getExtras() == null){

            //insert a new record
            databaseConnector.insertAuthor(
                    firstNameEditText.getText().toString(),
                    lastNameEditText.getText().toString());

        }else{

            //update an existing record
            databaseConnector.updateAuthor(rowId,
                    firstNameEditText.getText().toString(),
                    lastNameEditText.getText().toString());

        }
    }// end of saveAuthor() method

}
