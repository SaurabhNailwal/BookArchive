package com.example.saurmn.bookarchive;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;


/**
 * Created by SaurabhMN on 4/18/2015.
 */
public class DatabaseConnector{

    //database name
    private static final String DATABASE_NAME = "BookAuthor";
    private SQLiteDatabase database;
    private DatabaseOpenHelper databaseOpenHelper;

    //constructor
    public DatabaseConnector(Context context){
        // create a new DatabaseOpenHelper
        int dbVersion = 1;
        databaseOpenHelper = new DatabaseOpenHelper(context, DATABASE_NAME, null, dbVersion);
    }

    // open database connection
    //public void open() throws SQLException{
    public void open() throws SQLException{
        database = databaseOpenHelper.getWritableDatabase();
    }

    //close database connection
    public void close(){
        if(database != null){
            database.close();
        }
    }

    // insert a new book in the 'book' table in the database
    public void insertBook(String isbn, String title, String edition, String yearPublished){
        ContentValues newBook = new ContentValues();
        newBook.put("isbn",isbn);
        newBook.put("title", title);
        newBook.put("edition", edition);
        newBook.put("yearPublished", yearPublished);

        open(); // opening the database
        database.insert("book", null, newBook);
        close(); // closing the database
    }

    //update an existing book in the 'book' table in the database
    public void updateBook(long id, String isbn, String title, String edition, String yearPublished){
        ContentValues editBook = new ContentValues();
        editBook.put("isbn", isbn);
        editBook.put("title", title);
        editBook.put("edition", edition);
        editBook.put("yearPublished", yearPublished);

        open();
        database.update("book",editBook, "_id=" + id, null);
        close();
    }

    //get all books using a cursor
    public Cursor getAllBooks(){
        return database.rawQuery("SELECT _id, title  as title2 FROM book ORDER BY title", null);
    }


     //get one specific book
    public Cursor getOneBook(long id){
        return database.query("book", null, "_id="+id, null, null, null, null);
    }

    //delete the specified book
    public void deleteBook(long id){
        open();
        database.delete("book", "_id="+id, null);
        close();
    }

    // insert a new author in the 'author' table in the database
    public void insertAuthor(String firstName, String lastName){
        ContentValues newAuthor = new ContentValues();
        newAuthor.put("firstName", firstName);
        newAuthor.put("lastName", lastName);

        open();
        database.insert("author", null, newAuthor);
        close();
    }

    //update an existing author in the 'author' table in the database
    public void updateAuthor(long id, String firstName, String lastName){
        ContentValues editAuthor = new ContentValues();
        editAuthor.put("firstName", firstName);
        editAuthor.put("lastName", lastName);

        open();
        database.update("author", editAuthor, "_id="+id, null);
        close();
    }

    //get all authors returning two columns: _id and fullname
    public Cursor getAllAuthors(){
        return database.rawQuery("SELECT _id, lastName ||',' || ' ' ||  firstName AS fullName FROM author ORDER BY lastName", null);
    }

    //get one specific author
    public Cursor getOneAuthor(long id){
        return database.query("author", null, "_id="+id, null, null, null, null);
    }

    //delete the specified author
    public void deleteAuthor(long id){
        open();
        database.delete("author", "_id="+id, null);
        close();
    }

    //return a Cursor with assigned book authors for expanded list
    public Cursor getBookAuthors(long bookID){
        return database.rawQuery("SELECT _id, firstName || ' ' || lastName AS fullName FROM author " +
                "WHERE _id IN (SELECT author_id FROM book_author " +
                "WHERE book_id = " + bookID + " ) " +
                "ORDER BY firstName", null);
    }


    // return a Cursor with all potential authors information for a particular book ID in the database
    public Cursor getAuthorsForBook(long bookID){
        return database.rawQuery("SELECT _id, firstName || ' ' || lastName AS fullName from author " +
                "WHERE _id NOT IN (SELECT author_id FROM book_author " +
                "WHERE book_id = " + bookID + ") " +
                "ORDER BY firstName", null);
    }// end method getAuthorsForBook

    // return a Cursor that contains all books for a particular author ID in the database
    public Cursor getBooksForAuthor(long authorID){
        return database.rawQuery("SELECT _id, title FROM book " +
                "WHERE _id NOT IN (SELECT book_id FROM book_author " +
                "WHERE author_id = " + authorID + ") " +
                "ORDER BY title", null);

    }

    // inserts a new book_author in the database
    public void insertBookAuthor(long bookID, long authorID){
        ContentValues newBookAuthor = new ContentValues();
        newBookAuthor.put("book_id", bookID);
        newBookAuthor.put("author_id", authorID);

        open(); // open the database
        database.insert("book_author", null, newBookAuthor);
        close(); // close the database
    } // end method insertBookAuthor


    public void deleteBookAuthor(long bookID, long authorID){
        open(); // open the database
        database.delete("book_author", "book_id=" + bookID + " and author_id=" + authorID , null);
        close(); // close the database
    } // end method deleteAuthor


    private class DatabaseOpenHelper extends SQLiteOpenHelper{

        public DatabaseOpenHelper(Context context, String name, CursorFactory factory,int version){
            super(context, name, factory, version);
        }


        @Override
        public void onCreate(SQLiteDatabase db){

            // query to create a new table named 'book'
            String createQuery = "CREATE TABLE book"+
                    "(_id integer primary key autoincrement, "+
                    "isbn TEXT, "+
                    "title TEXT, "+
                    "edition TEXT, "+
                    "yearPublished integer);";

            db.execSQL(createQuery);// execute the query

            // query to create a new table named 'author'
            createQuery = "CREATE TABLE author"+
                    "(_id integer primary key autoincrement, "+
                    "firstName TEXT, "+
                    "lastName TEXT);";

            db.execSQL(createQuery);// execute the query

            // query to create a new table named 'book_author'
            createQuery = "CREATE TABLE book_author"+
                    "(_id integer primary key autoincrement, "+
                    "book_id integer references book(_id) on delete cascade, "+
                    "author_id integer references author(_id) on delete cascade);";

            db.execSQL(createQuery);// execute the query

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

    }


}
