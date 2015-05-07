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
public class BookList extends ListActivity{

    public static final String ROW_ID = "row_id"; // Intent extra key
    public ListView bookListView;
    private CursorAdapter bookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookListView = getListView();
        bookListView.setOnItemClickListener(viewBookListener);

        // map each book's title to a TextView in the ListView layout
        String[] from = new String[]{"title2"};
        int[] to = new int[]{R.id.bookTextView};
        bookAdapter = new SimpleCursorAdapter(
                BookList.this,R.layout.book_list_item, null, from, to);
        setListAdapter(bookAdapter);
    }

    protected void onResume()
    {
        super.onResume(); // call super's onResume method
        // create new GetContactsTask and execute it
        new GetBooksTask().execute((Object[]) null);
    }

    protected void onStop(){
        Cursor cursor = bookAdapter.getCursor(); // get current Cursor

        if (cursor != null)
            cursor.deactivate();

        bookAdapter.changeCursor(null);
        super.onStop();
    }

    // performs dB query outside GUI thread
    private class GetBooksTask extends AsyncTask<Object, Object, Cursor>{

        DatabaseConnector databaseConnector = new DatabaseConnector(BookList.this);

        //perform the dB access
        protected Cursor doInBackground(Object... params){
           databaseConnector.open();

           // get a cursor containing all books
           return databaseConnector.getAllBooks();
        }

        protected void onPostExecute(Cursor result){

            bookAdapter.changeCursor(result);
            databaseConnector.close();
        }
    }// end of class GetBooksTask

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_book_list,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.showBookAuthorItem_b:
                startActivity(new Intent(this, BookArchive.class));
                return true;
            case R.id.showBooksItem_b:
                startActivity(new Intent(this, BookList.class));
                return true;
            case R.id.showAuthorsItem_b:
                startActivity(new Intent(this, AuthorList.class));
                return true;
            case R.id.addBookItem_b:
                startActivity(new Intent(this, AddEditBook.class));
                return true;
            case R.id.addAuthorItem_b:
                startActivity(new Intent(this, AddEditAuthor.class));
                return true;
            default:
                //return true;
                return super.onOptionsItemSelected(item);
        }
    }

    OnItemClickListener viewBookListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            //create intent to launch the ViewBook Activity
            Intent viewBook = new Intent(BookList.this,ViewBook.class);

            //passing selected contact's row id as an extra
            viewBook.putExtra(ROW_ID, id);
            startActivity(viewBook); // start the viewBook Activity
        }
    }; // end of viewBoookListener


}
