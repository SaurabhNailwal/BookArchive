package com.example.saurmn.bookarchive;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Created by SaurabhMN on 4/16/2015.
 */
public class AuthorList extends ListActivity{

    public static final String ROW_ID = "row_id"; // Intent extra key
    public ListView authorListView;
    private CursorAdapter authorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authorListView = getListView();
        authorListView.setOnItemClickListener(viewAuthorListener);

        // map each author's fullName to a TextView in the ListView layout
        String[] from = new String[]{"fullName"};
        int[] to = new int[]{R.id.authorTextView};
        authorAdapter = new SimpleCursorAdapter(
                AuthorList.this,R.layout.author_list_item, null, from, to);
        setListAdapter(authorAdapter);
    }

    protected void onResume()
    {
        super.onResume(); // call super's onResume method

        // create new GetContactsTask and execute it
        new GetAuthorsTask().execute((Object[]) null);
    }

    protected void onStop(){
        Cursor cursor = authorAdapter.getCursor(); // get current Cursor

        if (cursor != null)
            cursor.deactivate();

        authorAdapter.changeCursor(null);
        super.onStop();
    }

    // performs dB query outside GUI thread
    private class GetAuthorsTask extends AsyncTask<Object, Object, Cursor> {

        DatabaseConnector databaseConnector = new DatabaseConnector(AuthorList.this);

        //perform the dB access
        protected Cursor doInBackground(Object... params){
            databaseConnector.open();

            // get a cursor containing all authors
            return databaseConnector.getAllAuthors();
        }

        protected void onPostExecute(Cursor result){

            authorAdapter.changeCursor(result);
            databaseConnector.close();
        }
    }// end of class GetAuthorsTask

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_author_list,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.showBookAuthorItem_a:
                startActivity(new Intent(this, BookArchive.class));
                return true;
            case R.id.showBooksItem_a:
                startActivity(new Intent(this, BookList.class));
                return true;
            case R.id.showAuthorsItem_a:
                startActivity(new Intent(this, AuthorList.class));
                return true;
            case R.id.addBookItem_a:
                startActivity(new Intent(this, AddEditBook.class));
                return true;
            case R.id.addAuthorItem_a:
                startActivity(new Intent(this, AddEditAuthor.class));
                return true;
            default:
                //return true;
                return super.onOptionsItemSelected(item);
        }
    }

    OnItemClickListener viewAuthorListener = new OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            //create intent to launch the ViewAuthor Activity
            Intent viewAuthor = new Intent(AuthorList.this,ViewAuthor.class);

            //passing selected contact's row id as an extra
            viewAuthor.putExtra(ROW_ID, id);
            startActivity(viewAuthor); // start the ViewAuthor Activity
        }
    }; // end of viewAuthorListener

}
