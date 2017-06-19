package net.rhuanbarros.construfacilnovo2.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Oclemmy on 5/2/2016 for ProgrammingWizards Channel and http://www.Camposha.com.
 */

public class DBAdapter {
    Context c;
    SQLiteDatabase db;
    DatabaseHelper helper;

    public DBAdapter(Context c) {
        this.c = c;
        helper=new DatabaseHelper(c);
    }

    //OPEN DB
    public void openDB()
    {
        try
        {
            db=helper.getWritableDatabase();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    //CLOSE
    public void closeDB()
    {
        try
        {
            helper.close();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    //RETRIEVE DATA AND FILTER
    public Cursor retrieve(String searchTerm)
    {
        String[] columns={"_id","Nome"};
        Cursor c=null;
        if(searchTerm != null && searchTerm.length()>0)
        {
            String sql="SELECT * FROM Material WHERE Nome LIKE '%"+searchTerm+"%'";
            c=db.rawQuery(sql,null);
            return c;
        }
        //c=db.query("Material",columns,null,null,null,null,null);
        return null;
    }
}