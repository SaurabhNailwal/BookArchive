package com.example.saurmn.bookarchive;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleCursorTreeAdapter;


public class BookArchive extends ExpandableListActivity {
    private DatabaseConnector databaseConnector;
    private Cursor childrenCursor;
    private Cursor parentCursor;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explistview_main);
        databaseConnector = new DatabaseConnector(BookArchive.this);
        databaseConnector.open();
        parentCursor = databaseConnector.getAllBooks();
        startManagingCursor(parentCursor);

        final SimpleCursorTreeAdapter cursorTreeAdapter = new SimpleCursorTreeAdapter(
                getApplicationContext(),
                parentCursor,
                R.layout.listrow_group,
                R.layout.listrow_group,
                new String[]{"title2", "_id"},
                new int[]{R.id.textViewGroup},
                R.layout.listrow_details,
                R.layout.listrow_details,
                new String[]{"fullName","_id"},
                new int[]{R.id.textViewDetail}) {
            @Override
            protected Cursor getChildrenCursor(Cursor groupCursor) {
                databaseConnector.open();
                childrenCursor = databaseConnector.getBookAuthors(groupCursor.getLong(0));
                startManagingCursor(childrenCursor);
                return childrenCursor;
            }
        };

        setListAdapter(cursorTreeAdapter);
        databaseConnector.close();
    }

    public boolean onCreateOptionsMenu(Menu menu){

        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_book_archive,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.showBookAuthorItem:
                startActivity(new Intent(this, BookArchive.class));
                return true;
            case R.id.showBooksItem:
                startActivity(new Intent(this, BookList.class));
                return true;
            case R.id.showAuthorsItem:
                startActivity(new Intent(this, AuthorList.class));
                return true;
            case R.id.addBookItem:
                startActivity(new Intent(this, AddEditBook.class));
                return true;
            case R.id.addAuthorItem:
                startActivity(new Intent(this, AddEditAuthor.class));
                return true;
            default:
                //return true;
                return super.onOptionsItemSelected(item);
        }
    }

}
